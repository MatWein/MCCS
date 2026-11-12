package de.mccs.mc.mixin;

import de.mccs.mc.staff.StaffSystems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    /** Cloaked players are invisible to mobs. */
    @Inject(method = "getVisibilityPercent", at = @At("RETURN"), cancellable = true)
    private void mccs$cloak(ServerLevel level, Entity looking, CallbackInfoReturnable<Double> result) {
        if (StaffSystems.isCloaked((LivingEntity) (Object) this)) result.setReturnValue(0.0);
    }

    /** A shield stops a fall before vanilla plays its sounds. */
    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void mccs$shieldFall(double distance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> result) {
        if (StaffSystems.absorbFall((LivingEntity) (Object) this, distance, multiplier)) result.setReturnValue(false);
    }
}
