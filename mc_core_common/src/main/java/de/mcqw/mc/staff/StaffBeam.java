package de.mcqw.mc.staff;

import de.mcqw.core.config.BeamConfig;
import de.mcqw.core.config.ServerConfig;
import de.mcqw.core.staff.BeamMath;
import de.mcqw.mc.compat.StaffCompat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The staff's lightning attack, as long as the attack key is held: it hits what the player aims at and jumps on to nearby
 * hostile mobs. It needs the staff in the main hand and does nothing while riding.
 */
public final class StaffBeam {
    /** Tells everyone seeing the player about the lightning; set by the loader. */
    public interface Sender {
        void send(ServerPlayer shooter, boolean firing, int[] targets);
    }

    public static Sender sender = (shooter, firing, targets) -> {};

    /** Clients that start seeing the player in the middle of the attack, or miss its end, catch up with this. */
    private static final int RESEND_INTERVAL = 10;
    private static final int CRACKLE_INTERVAL = 8;
    private static final float CRACKLE_PITCH = 1.6f;
    private static final float CRACKLE_PITCH_RANGE = 0.5f;

    private static final class State {
        private Entity main;
        private int ticksOnMain;
        private int age;
        private int[] sent = new int[0];
    }

    private static final Map<ServerPlayer, State> FIRING = new WeakHashMap<>();

    private StaffBeam() {}

    static void start(ServerPlayer player) {
        ItemStack staff = StaffSystems.mainStaff(player);
        if (staff.isEmpty() || FIRING.containsKey(player)) return;
        if (ServerConfig.STAFF.consumeEnergy() && StaffSystems.stored(staff) <= 0) {
            StaffSounds.denied(player);
            return;
        }
        FIRING.put(player, new State());
    }

    static void stop(ServerPlayer player) {
        if (FIRING.remove(player) != null) sender.send(player, false, new int[0]);
    }

    static void tick(MinecraftServer server) {
        if (FIRING.isEmpty()) return;
        for (Map.Entry<ServerPlayer, State> entry : new ArrayList<>(FIRING.entrySet())) {
            ServerPlayer player = entry.getKey();
            ItemStack staff = StaffSystems.mainStaff(player);
            if (player.isRemoved() || !player.isAlive() || player.isSpectator() || staff.isEmpty()) {
                stop(player);
            } else if (!tick(player, staff, entry.getValue())) {
                StaffSounds.denied(player);
                stop(player);
            }
        }
    }

    /** @return false when the energy ran out */
    private static boolean tick(ServerPlayer player, ItemStack staff, State state) {
        BeamConfig config = ServerConfig.BEAM;
        StaffSystems.endCloak(player);
        if (ServerConfig.STAFF.consumeEnergy() && state.age % StaffSystems.DRAIN_INTERVAL == 0
                && !StaffSystems.consume(staff, config.drain() * (long) StaffSystems.DRAIN_INTERVAL)) {
            return false;
        }
        Entity main = BeamAim.target(player, config.range(), config.aimTolerance());
        state.ticksOnMain = main != null && main == state.main ? state.ticksOnMain + 1 : 0;
        state.main = main;
        List<Entity> targets = targets(main, config);
        int interval = BeamMath.hitInterval(config.hitsPerSecond());
        if (state.age % interval == 0) hit(player, targets, state.ticksOnMain, interval, config);
        if (state.age % CRACKLE_INTERVAL == 0) {
            StaffSounds.beamCrackle(player, CRACKLE_PITCH + player.getRandom().nextFloat() * CRACKLE_PITCH_RANGE);
        }
        int[] ids = targets.stream().mapToInt(Entity::getId).toArray();
        if (state.age % RESEND_INTERVAL == 0 || !Arrays.equals(ids, state.sent)) {
            sender.send(player, true, ids);
            state.sent = ids;
        }
        state.age++;
        return true;
    }

    /** The main target and the mobs the lightning jumps to, in order. */
    private static List<Entity> targets(Entity main, BeamConfig config) {
        List<Entity> targets = new ArrayList<>();
        if (main == null) return targets;
        targets.add(main);
        Set<Entity> used = new HashSet<>(targets);
        Entity current = main;
        for (int i = 0; i < config.chainTargets(); i++) {
            double reach = config.chainRange() * (current.isInWaterOrRain() ? config.wetChainRangeFactor() : 1.0);
            Entity next = nearestEnemy(current, reach, used);
            if (next == null) break;
            targets.add(next);
            used.add(next);
            current = next;
        }
        return targets;
    }

    private static Entity nearestEnemy(Entity from, double reach, Set<Entity> used) {
        Vec3 center = BeamAim.center(from);
        AABB area = from.getBoundingBox().inflate(reach);
        Entity nearest = null;
        double best = reach * reach;
        for (LivingEntity candidate : from.level().getEntitiesOfClass(LivingEntity.class, area, e -> e instanceof Enemy && e.isAlive() && !used.contains(e))) {
            double distance = BeamAim.center(candidate).distanceToSqr(center);
            if (distance <= best && BeamAim.canSee(from, center, BeamAim.center(candidate))) {
                nearest = candidate;
                best = distance;
            }
        }
        return nearest;
    }

    private static void hit(ServerPlayer player, List<Entity> targets, int ticksOnMain, int interval, BeamConfig config) {
        double charge = BeamMath.chargeFactor(ticksOnMain, config.chargeSeconds(), config.chargeStart(), config.chargeMax());
        DamageSource source = player.damageSources().playerAttack(player);
        for (int i = 0; i < targets.size(); i++) {
            Entity target = targets.get(i);
            boolean wet = target.isInWaterOrRain();
            float damage = BeamMath.hitDamage(config.damagePerSecond(), interval, charge, i, config.chainFalloff(), wet ? config.wetDamageFactor() : 1.0);
            // several hits per second: neither stopped by the damage cooldown nor pushing the target away
            Vec3 motion = target.getDeltaMovement();
            StaffCompat.hurt(target, source, damage);
            StaffCompat.restoreMotion(target, motion);
            if (target instanceof LivingEntity living) {
                if (config.slowTicks() > 0) StaffCompat.slow(living, config.slowTicks(), config.slowLevel() - 1);
                if (config.fireSeconds() > 0 && !wet) StaffCompat.ignite(living, (float) config.fireSeconds());
            }
        }
    }
}
