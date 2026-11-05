package de.mcqw.fabric.mixin;

import de.mcqw.mc.staff.StaffSystems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Mobs cannot target cloaked players. */
@Mixin(Mob.class)
abstract class MobMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void mcqw$cloak(LivingEntity target, CallbackInfo callback) {
        if (StaffSystems.isCloaked(target)) callback.cancel();
    }
}
