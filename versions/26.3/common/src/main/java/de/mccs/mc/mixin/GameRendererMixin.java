package de.mccs.mc.mixin;

import de.mccs.mc.client.StaffClient;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** No first person hand while cloaked. */
@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void mccs$hideHand(CallbackInfo callback) {
        if (StaffClient.hidesOwnHand()) callback.cancel();
    }
}
