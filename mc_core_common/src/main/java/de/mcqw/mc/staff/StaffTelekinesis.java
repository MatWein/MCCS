package de.mcqw.mc.staff;

import de.mcqw.core.config.ServerConfig;
import de.mcqw.core.config.TelekinesisConfig;
import de.mcqw.core.staff.TelekinesisMath;
import de.mcqw.mc.compat.StaffCompat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The staff's telekinesis: a press lifts what the player aims at, the next one lets it go, and the attack key throws it. It needs the
 * staff in the main hand and does nothing while riding.
 */
public final class StaffTelekinesis {
    private static final int RESEND_INTERVAL = 10;
    private static final int ITEM_PICKUP_DELAY = 10;
    /** Time a thrown target can hit something and be hurt by it. */
    private static final int THROW_TICKS = 40;
    private static final double MIN_IMPACT_SPEED = 0.5;

    private static final class Hold {
        private final Entity target;
        private int age;

        private Hold(Entity target) {
            this.target = target;
        }
    }

    private static final class Thrown {
        private final ServerPlayer thrower;
        private double lastSpeed;
        private int age;

        private Thrown(ServerPlayer thrower, double speed) {
            this.thrower = thrower;
            this.lastSpeed = speed;
        }
    }

    private static final Map<ServerPlayer, Hold> HOLDS = new HashMap<>();
    private static final Map<Entity, Thrown> THROWN = new HashMap<>();

    private StaffTelekinesis() {}

    static boolean isHolding(ServerPlayer player) {
        return HOLDS.containsKey(player);
    }

    /** The key was pressed. */
    static void toggle(ServerPlayer player) {
        if (HOLDS.containsKey(player)) {
            release(player);
            StaffSounds.telekinesisReleased(player);
            return;
        }
        ItemStack staff = StaffSystems.mainStaff(player);
        if (staff.isEmpty()) return;
        TelekinesisConfig config = ServerConfig.TELEKINESIS;
        boolean paid = ServerConfig.STAFF.consumeEnergy();
        Entity target = BeamAim.nearest(player, config.range(), config.aimTolerance(), entity -> canLift(entity, config));
        if (target == null || paid && StaffSystems.stored(staff) <= 0) {
            StaffSounds.denied(player);
            return;
        }
        StaffBeam.stop(player);
        StaffSystems.endCloak(player);
        target.stopRiding();
        HOLDS.put(player, new Hold(target));
        StaffSounds.telekinesisLifted(player);
    }

    private static boolean canLift(Entity entity, TelekinesisConfig config) {
        if (entity.getBbWidth() > config.maxSize() || entity.getBbHeight() > config.maxSize()) return false;
        if (entity instanceof Player && !config.liftPlayers()) return false;
        if (entity instanceof ItemEntity) return entity.isAlive();
        return BeamAim.canTarget(entity);
    }

    /** The attack key was pressed while holding a target. */
    static void throwHeld(ServerPlayer player) {
        Hold hold = HOLDS.get(player);
        if (hold == null) return;
        TelekinesisConfig config = ServerConfig.TELEKINESIS;
        ItemStack staff = StaffSystems.mainStaff(player);
        if (ServerConfig.STAFF.consumeEnergy() && !staff.isEmpty() && StaffSystems.stored(staff) < config.throwEnergy()) {
            StaffSounds.denied(player);
            release(player);
            return;
        }
        if (ServerConfig.STAFF.consumeEnergy() && !staff.isEmpty()) StaffSystems.consume(staff, config.throwEnergy());
        release(player);
        Vec3 motion = player.getViewVector(1f).scale(config.throwSpeed());
        StaffCompat.launch(hold.target, motion);
        if (hold.target instanceof LivingEntity) THROWN.put(hold.target, new Thrown(player, motion.length()));
        StaffSounds.telekinesisThrown(player);
    }

    private static void release(ServerPlayer player) {
        if (HOLDS.remove(player) != null) StaffBeam.sender.send(player, false, false, new int[0]);
    }

    static void tick(MinecraftServer server) {
        for (Map.Entry<ServerPlayer, Hold> entry : new ArrayList<>(HOLDS.entrySet())) {
            ServerPlayer player = entry.getKey();
            Hold hold = entry.getValue();
            ItemStack staff = StaffSystems.mainStaff(player);
            if (player.isRemoved() || !player.isAlive() || player.isSpectator() || staff.isEmpty()
                    || !hold.target.isAlive() || hold.target.level() != player.level()) {
                release(player);
            } else if (!hold(player, staff, hold)) {
                StaffSounds.denied(player);
                release(player);
            }
        }
        tickThrown();
    }

    /** @return false when the energy ran out */
    private static boolean hold(ServerPlayer player, ItemStack staff, Hold hold) {
        TelekinesisConfig config = ServerConfig.TELEKINESIS;
        if (ServerConfig.STAFF.consumeEnergy() && hold.age % StaffSystems.DRAIN_INTERVAL == 0
                && !StaffSystems.consume(staff, config.drain() * (long) StaffSystems.DRAIN_INTERVAL)) {
            return false;
        }
        StaffSystems.endCloak(player);
        Entity target = hold.target;
        Vec3 place = player.getEyePosition(1f).add(player.getViewVector(1f).scale(config.holdDistance()));
        Vec3 offset = place.subtract(BeamAim.center(target));
        double x = TelekinesisMath.pull(offset.x);
        double y = TelekinesisMath.pull(offset.y);
        double z = TelekinesisMath.pull(offset.z);
        StaffCompat.launch(target, new Vec3(x, y, z).scale(TelekinesisMath.limit(x, y, z)));
        target.resetFallDistance();
        if (target instanceof Mob mob) mob.getNavigation().stop();
        if (target instanceof ItemEntity item) item.setPickUpDelay(ITEM_PICKUP_DELAY);
        if (hold.age % RESEND_INTERVAL == 0) StaffBeam.sender.send(player, true, true, new int[]{target.getId()});
        hold.age++;
        return true;
    }

    private static void tickThrown() {
        if (THROWN.isEmpty()) return;
        TelekinesisConfig config = ServerConfig.TELEKINESIS;
        THROWN.entrySet().removeIf(entry -> {
            Entity entity = entry.getKey();
            Thrown thrown = entry.getValue();
            if (entity.isRemoved() || !entity.isAlive() || ++thrown.age > THROW_TICKS) return true;
            if (thrown.age > 1 && (entity.horizontalCollision || entity.verticalCollision)) {
                float damage = TelekinesisMath.impact(thrown.lastSpeed, MIN_IMPACT_SPEED, config.impactDamage(), config.maxImpactDamage());
                if (damage > 0) {
                    DamageSource source = entity.damageSources().playerAttack(thrown.thrower);
                    StaffCompat.hurt(entity, source, damage);
                }
                return true;
            }
            thrown.lastSpeed = entity.getDeltaMovement().length();
            return false;
        });
    }
}
