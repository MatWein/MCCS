package de.mcqw.mc.compat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Calls that differ between Minecraft versions, for the shared code. */
public final class StaffCompat {
    private StaffCompat() {}

    /** A hit that ignores the damage cooldown; returns whether it did something. */
    public static boolean hurt(Entity target, DamageSource source, float damage) {
        target.setInvulnerableTime(0);
        return target.level() instanceof ServerLevel level && target.hurtServer(level, source, damage);
    }

    public static void setInvulnerableTime(Entity entity, int ticks) {
        entity.setInvulnerableTime(ticks);
    }

    /** Undoes the knockback of a hit. */
    public static void restoreMotion(Entity entity, Vec3 motion) {
        entity.setDeltaMovement(motion);
        entity.needsSync = false;
    }

    /** Sets the motion, also for players, whose client moves them. */
    public static void launch(Entity entity, Vec3 motion) {
        entity.setDeltaMovement(motion);
        entity.needsSync = true;
    }

    public static SoundEvent explodeSound() {
        return SoundEvents.GENERIC_EXPLODE.value();
    }

    public static void cooldown(Player player, ItemStack stack, int ticks) {
        player.getCooldowns().addCooldown(stack, ticks);
    }

    public static void slow(LivingEntity entity, int ticks, int amplifier) {
        entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, amplifier, false, false, false));
    }

    public static void ignite(Entity entity, float seconds) {
        entity.igniteForSeconds(seconds);
    }
}
