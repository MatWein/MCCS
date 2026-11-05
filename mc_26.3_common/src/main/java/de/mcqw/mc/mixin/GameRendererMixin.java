package de.mcqw.mc.mixin;

import de.mcqw.mc.client.StaffClient;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** No first person hand while cloaked. */
@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void mcqw$hideHand(CallbackInfo callback) {
        if (StaffClient.hidesOwnHand()) callback.cancel();
    }
}
