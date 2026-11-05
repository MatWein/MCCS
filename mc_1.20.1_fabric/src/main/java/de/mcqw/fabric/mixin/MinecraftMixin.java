package de.mcqw.fabric.mixin;

import de.mcqw.mc.client.BeamClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The attack key fires the lightning while the staff is in the main hand: no mining, no melee (Forge: InputEvent.InteractionKeyMappingTriggered). */
@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void mcqw$noAttack(CallbackInfoReturnable<Boolean> result) {
        if (BeamClient.takesAttackKey()) result.setReturnValue(false);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void mcqw$noMining(boolean leftClick, CallbackInfo callback) {
        if (leftClick && BeamClient.takesAttackKey()) callback.cancel();
    }
}
