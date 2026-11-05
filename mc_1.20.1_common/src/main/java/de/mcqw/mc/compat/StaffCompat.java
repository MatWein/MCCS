package de.mcqw.mc.compat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Calls that differ between Minecraft versions, for the shared code. */
public final class StaffCompat {
    private StaffCompat() {}

    public static void hurt(Entity target, DamageSource source, float damage) {
        target.hurt(source, damage);
    }

    public static void setInvulnerableTime(Entity entity, int ticks) {
        entity.invulnerableTime = ticks;
    }

    /** Undoes the knockback of a hit. */
    public static void restoreMotion(Entity entity, Vec3 motion) {
        entity.setDeltaMovement(motion);
        entity.hurtMarked = false;
    }

    public static void slow(LivingEntity entity, int ticks, int amplifier) {
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, amplifier, false, false, false));
    }

    public static void ignite(Entity entity, float seconds) {
        entity.setSecondsOnFire(Math.round(seconds));
    }
}
