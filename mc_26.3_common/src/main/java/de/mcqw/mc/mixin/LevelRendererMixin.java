package de.mcqw.mc.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mcqw.mc.client.render.ShieldRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the shield bubbles to the entities of a frame, on both loaders. */
@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {
    @Inject(method = "submitEntities", at = @At("TAIL"))
    private void mcqw$submitShields(PoseStack poseStack, LevelRenderState state, SubmitNodeCollector output, CallbackInfo ci) {
        ShieldRenderer.submit(poseStack, output, state.cameraRenderState.pos);
    }
}
