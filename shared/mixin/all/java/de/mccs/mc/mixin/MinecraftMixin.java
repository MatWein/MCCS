package de.mccs.mc.mixin;

import de.mccs.mc.client.BeamClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The attack key fires the lightning while the staff is in the main hand: no mining, no melee (Forge 1.20.1: InputEvent.InteractionKeyMappingTriggered). */
@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void mccs$noAttack(CallbackInfoReturnable<Boolean> result) {
        if (BeamClient.takesAttackKey()) result.setReturnValue(false);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void mccs$noMining(boolean leftClick, CallbackInfo callback) {
        if (leftClick && BeamClient.takesAttackKey()) callback.cancel();
    }
}
