package de.mccs.mc.staff;

import de.mccs.core.config.ServerConfig;
import de.mccs.core.config.StaffConfig;
import de.mccs.core.staff.BubbleAnimation;
import de.mccs.core.staff.StaffAction;
import de.mccs.core.staff.StaffEnergy;
import de.mccs.mc.item.CrystalStaffItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * The staff's shield and cloak. Their state lives on the item stack, which Minecraft syncs to everyone seeing the
 * player. They only work while the staff is held and the player is not riding anything (horse, boat, jumper, ...);
 * otherwise the staff has no effect at all: they pause and resume afterwards.
 */
public final class StaffSystems {
    /** Energy is taken in steps, because each change of the stack goes over the network. */
    static final int DRAIN_INTERVAL = 10;
    /** Fire and lava hit every tick; the sound does not repeat that fast. */
    private static final int HIT_SOUND_INTERVAL = 10;
    private static final double CLOAK_FORGET_RANGE = 48;
    /** The first fall distance that hurts, as in vanilla's fall damage. */
    private static final double SAFE_FALL_DISTANCE = 3;

    /** Player tick of the last damage. */
    private static final Map<ServerPlayer, Integer> LAST_HIT = new WeakHashMap<>();
    private static final Map<ServerPlayer, Integer> LAST_HIT_SOUND = new WeakHashMap<>();
    /** Ticks of the cloak's fade, as on the clients: from then on the body is invisible. */
    private static final Map<ServerPlayer, Integer> CLOAK_FADE = new WeakHashMap<>();

    private StaffSystems() {}

    /** The staff in the player's hands (main hand first), or an empty stack; riding players count as having none. */
    public static ItemStack heldStaff(LivingEntity entity) {
        if (entity.isPassenger()) return ItemStack.EMPTY;
        if (isStaff(entity.getMainHandItem())) return entity.getMainHandItem();
        return isStaff(entity.getOffhandItem()) ? entity.getOffhandItem() : ItemStack.EMPTY;
    }

    /** The held staff with its shield switched on, or an empty stack. */
    public static ItemStack activeShield(LivingEntity entity) {
        return active(entity, StaffAction.SHIELD);
    }

    /** The held staff with its cloak switched on, or an empty stack. */
    public static ItemStack activeCloak(LivingEntity entity) {
        return active(entity, StaffAction.CLOAK);
    }

    /** The held staff with its light switched on, or an empty stack. */
    public static ItemStack activeLights(LivingEntity entity) {
        return active(entity, StaffAction.LIGHTS);
    }

    private static ItemStack active(LivingEntity entity, StaffAction system) {
        if (entity.isPassenger()) return ItemStack.EMPTY;
        if (isOn(entity.getMainHandItem(), system)) return entity.getMainHandItem();
        return isOn(entity.getOffhandItem(), system) ? entity.getOffhandItem() : ItemStack.EMPTY;
    }

    private static boolean isOn(ItemStack stack, StaffAction system) {
        if (!isStaff(stack)) return false;
        return switch (system) {
            case SHIELD -> StaffData.shieldOn(stack);
            case CLOAK -> StaffData.cloakOn(stack);
            case LIGHTS -> StaffData.lightsOn(stack);
            default -> false;
        };
    }

    /** Mobs neither see nor target a cloaked player. */
    public static boolean isCloaked(Entity entity) {
        return entity instanceof LivingEntity living && !activeCloak(living).isEmpty();
    }

    public static int stored(ItemStack stack) {
        StaffConfig config = ServerConfig.STAFF;
        return StaffEnergy.stored(StaffData.savedEnergy(stack), config.capacity(), config.startCharge());
    }

    /** What shield and cloak use in FE/tick where the player is. */
    public static int drain(LivingEntity player, boolean underFire) {
        StaffConfig config = ServerConfig.STAFF;
        long drain = 0;
        if (!activeShield(player).isEmpty()) drain += config.shieldDrain(player.isUnderWater(), player.isInLava(), underFire);
        boolean cloaked = !activeCloak(player).isEmpty();
        if (cloaked) drain += config.cloakDrain(player.isUnderWater(), player.isInLava(), underFire);
        if (!cloaked && !activeLights(player).isEmpty()) drain += config.lightDrain();
        return (int) Math.min(Integer.MAX_VALUE, drain);
    }

    /** The key for a system was pressed. */
    public static void perform(ServerPlayer player, StaffAction system) {
        if (system == StaffAction.BEAM_START) {
            // the attack key throws what the telekinesis holds, and fires the lightning otherwise
            if (StaffTelekinesis.isHolding(player)) StaffTelekinesis.throwHeld(player);
            else StaffBeam.start(player);
            return;
        }
        if (system == StaffAction.TELEKINESIS) {
            StaffTelekinesis.toggle(player);
            return;
        }
        if (system == StaffAction.BEAM_STOP) {
            StaffBeam.stop(player);
            return;
        }
        if (system == StaffAction.TELEPORT) {
            StaffTeleport.perform(player);
            return;
        }
        if (system == StaffAction.BLOCK_DAMAGE) {
            ItemStack held = heldStaff(player);
            if (held.isEmpty()) return;
            boolean on = !StaffData.blockDamage(held);
            StaffData.setBlockDamage(held, on);
            StaffSounds.blockDamageToggled(player, on);
            return;
        }
        ItemStack active = active(player, system);
        if (!active.isEmpty()) {
            setOn(player, active, system, false);
            return;
        }
        ItemStack staff = heldStaff(player);
        if (staff.isEmpty()) return;
        if (ServerConfig.STAFF.consumeEnergy() && stored(staff) <= 0) {
            StaffSounds.denied(player);
            return;
        }
        setOn(player, staff, system, true);
    }

    private static void setOn(ServerPlayer player, ItemStack staff, StaffAction system, boolean on) {
        switch (system) {
            case SHIELD -> {
                StaffData.setShieldOn(staff, on);
                StaffSounds.shieldToggled(player, on);
            }
            case CLOAK -> {
                StaffData.setCloakOn(staff, on);
                StaffSounds.cloakToggled(player, on);
                if (on) loseMobPursuers(player);
            }
            case LIGHTS -> {
                StaffData.setLightsOn(staff, on);
                StaffSounds.lightsToggled(player, on);
            }
            default -> throw new IllegalArgumentException("Not a system: " + system);
        }
    }

    /** Attacking with the staff gives a cloaked player away. */
    public static void onAttack(Entity attacker) {
        if (attacker instanceof ServerPlayer player) endCloak(player);
    }

    /** Ends the cloak of the staff in the main hand. */
    static void endCloak(ServerPlayer player) {
        if (isOn(player.getMainHandItem(), StaffAction.CLOAK) && !player.isPassenger()) {
            setOn(player, player.getMainHandItem(), StaffAction.CLOAK, false);
        }
    }

    /** The staff in the main hand, or an empty stack; riding players count as having none. */
    public static ItemStack mainStaff(LivingEntity entity) {
        return !entity.isPassenger() && isStaff(entity.getMainHandItem()) ? entity.getMainHandItem() : ItemStack.EMPTY;
    }

    private static void loseMobPursuers(ServerPlayer player) {
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(CLOAK_FORGET_RANGE))) {
            if (mob.getTarget() == player) mob.setTarget(null);
        }
    }

    public static void tick(MinecraftServer server) {
        StaffBeam.tick(server);
        StaffTelekinesis.tick(server);
        StaffLights.tick(server);
        boolean drainNow = server.getTickCount() % DRAIN_INTERVAL == 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            normalize(player.getMainHandItem());
            normalize(player.getOffhandItem());
            updateInvisibility(player);
            if (!ServerConfig.STAFF.consumeEnergy()) continue;
            ItemStack staff = activeShield(player);
            if (staff.isEmpty()) staff = activeCloak(player);
            if (staff.isEmpty()) staff = activeLights(player);
            if (staff.isEmpty()) continue;
            if (drainNow) consume(staff, drain(player, isUnderFire(player)) * (long) DRAIN_INTERVAL);
            if (stored(staff) <= 0) powerFailure(player);
        }
    }

    /** Vanilla's invisibility hides the body and the shadow from everyone and mobs notice less. */
    private static void updateInvisibility(ServerPlayer player) {
        boolean cloaked = !activeCloak(player).isEmpty();
        Integer fade = CLOAK_FADE.get(player);
        if (fade == null && !cloaked) return;
        int ticks = Math.max(0, (fade == null ? 0 : fade) + (cloaked ? 1 : -1));
        ticks = Math.min(ticks, 2 * BubbleAnimation.CLOAK_HIDE_TICKS);
        if (ticks > BubbleAnimation.CLOAK_HIDE_TICKS) {
            player.setInvisible(true);
        } else if (fade != null && fade > BubbleAnimation.CLOAK_HIDE_TICKS) {
            player.setInvisible(player.hasEffect(MobEffects.INVISIBILITY));
        }
        if (ticks == 0) CLOAK_FADE.remove(player);
        else CLOAK_FADE.put(player, ticks);
    }

    private static boolean isUnderFire(ServerPlayer player) {
        Integer hit = LAST_HIT.get(player);
        return hit != null && player.tickCount - hit <= ServerConfig.STAFF.underFireTicks();
    }

    /** Writes a new or over-full staff's real energy onto the stack, so it matches what everybody reads from it. */
    private static void normalize(ItemStack stack) {
        if (!isStaff(stack)) return;
        int stored = stored(stack);
        if (StaffData.savedEnergy(stack) != stored) StaffData.setSavedEnergy(stack, stored);
    }

    /**
     * Any damage to a player with an active shield, except what ignores invulnerability (/kill, the void). If the
     * energy for a hit is missing, the shield collapses and the hit goes through.
     *
     * @return true if the damage is cancelled
     */
    public static boolean absorb(Entity entity, DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayer player) || player.getAbilities().invulnerable) return false;
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        LAST_HIT.put(player, player.tickCount);
        ItemStack shield = activeShield(player);
        if (shield.isEmpty()) return false;
        if (ServerConfig.STAFF.consumeEnergy() && !consume(shield, StaffEnergy.hitCost(amount, ServerConfig.STAFF.shieldHitCost()))) {
            StaffData.setShieldOn(shield, false);
            StaffSounds.shieldCollapsed(player);
            powerFailure(player);
            return false;
        }
        StaffData.setShieldHits(shield, StaffData.shieldHits(shield) + 1);
        Integer lastSound = LAST_HIT_SOUND.get(player);
        if (lastSound == null || player.tickCount - lastSound >= HIT_SOUND_INTERVAL || player.tickCount < lastSound) {
            LAST_HIT_SOUND.put(player, player.tickCount);
            StaffSounds.shieldHit(player);
        }
        return true;
    }

    /**
     * A fall: vanilla plays the fall sounds before it deals the damage, so a shield has to stop it earlier.
     *
     * @return true if the whole fall damage including its sounds is cancelled
     */
    public static boolean absorbFall(Entity entity, double distance, float multiplier) {
        double damage = Math.ceil((distance - SAFE_FALL_DISTANCE) * multiplier);
        if (damage <= 0) return false;
        // the client runs the fall on its own too and plays the sounds locally
        if (entity.level().isClientSide()) return entity instanceof LivingEntity living && !activeShield(living).isEmpty();
        return absorb(entity, entity.damageSources().fall(), (float) damage);
    }

    /** Without energy everything fails. */
    private static void powerFailure(ServerPlayer player) {
        boolean anyOn = false;
        for (ItemStack stack : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
            if (!isStaff(stack)) continue;
            anyOn |= StaffData.shieldOn(stack) || StaffData.cloakOn(stack) || StaffData.lightsOn(stack);
            StaffData.setShieldOn(stack, false);
            StaffData.setCloakOn(stack, false);
            StaffData.setLightsOn(stack, false);
        }
        if (anyOn) StaffSounds.shieldCollapsed(player);
        StaffSounds.powerFailure(player);
    }

    /** Takes the amount; if it is not available the staff is emptied and false is returned. */
    static boolean consume(ItemStack staff, long amount) {
        int stored = stored(staff);
        boolean enough = stored >= amount;
        StaffData.setSavedEnergy(staff, enough ? (int) (stored - amount) : 0);
        return enough;
    }

    private static boolean isStaff(ItemStack stack) {
        return stack.getItem() instanceof CrystalStaffItem;
    }
}
