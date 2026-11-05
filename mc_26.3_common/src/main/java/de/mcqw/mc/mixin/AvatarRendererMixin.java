package de.mcqw.mc.mixin;

import de.mcqw.mc.client.StaffClient;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The cloaked player's outline shows only the body: no armor, no items. */
@Mixin(AvatarRenderer.class)
abstract class AvatarRendererMixin {
    @Inject(method = "shouldRenderLayers", at = @At("HEAD"), cancellable = true)
    private void mcqw$noGhostLayers(AvatarRenderState state, CallbackInfoReturnable<Boolean> result) {
        if (StaffClient.isOwnGhost(state.id)) result.setReturnValue(false);
    }
}
