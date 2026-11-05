package de.mcqw.mc.compat;

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
        target.invulnerableTime = 0;
        return target.hurt(source, damage);
    }

    public static void setInvulnerableTime(Entity entity, int ticks) {
        entity.invulnerableTime = ticks;
    }

    /** Undoes the knockback of a hit. */
    public static void restoreMotion(Entity entity, Vec3 motion) {
        entity.setDeltaMovement(motion);
        entity.hurtMarked = false;
    }

    /** Sets the motion, also for players, whose client moves them. */
    public static void launch(Entity entity, Vec3 motion) {
        entity.setDeltaMovement(motion);
        entity.hurtMarked = true;
    }

    public static SoundEvent explodeSound() {
        return SoundEvents.GENERIC_EXPLODE;
    }

    public static void cooldown(Player player, ItemStack stack, int ticks) {
        player.getCooldowns().addCooldown(stack.getItem(), ticks);
    }

    public static void slow(LivingEntity entity, int ticks, int amplifier) {
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, amplifier, false, false, false));
    }

    public static void ignite(Entity entity, float seconds) {
        entity.setSecondsOnFire(Math.round(seconds));
    }
}
