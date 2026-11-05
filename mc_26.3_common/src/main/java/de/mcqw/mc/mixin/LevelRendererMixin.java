package de.mcqw.mc.mixin;

import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.blaze3d.vertex.PoseStack;
import de.mcqw.mc.client.render.BeamRenderer;
import de.mcqw.mc.client.render.BubbleRenderer;
import de.mcqw.mc.client.render.CrystalRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the shield and cloak bubbles and the lightning to the entities of a frame, on both loaders. */
@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {
    /**
     * The phase after the translucent terrain moves behind clouds and weather: our additive effects write no depth, so clouds
     * drawn later would cover them. Vanilla's particles in the same phase are still hidden by clouds in front of them.
     */
    @Redirect(method = "executeClassicTransparency", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeTranslucentAfterTerrain(Lcom/mojang/renderpearl/api/commands/RenderPass;)V"))
    private void mcqw$notYet(FeatureRenderDispatcher.PreparedFrame frame, RenderPass renderPass) {}

    @Inject(method = "executeClassicTransparency", at = @At("TAIL"))
    private void mcqw$afterClouds(ChunkSectionsToRender chunkSections, FeatureRenderDispatcher.PreparedFrame frame, RenderPass renderPass, CallbackInfo ci) {
        frame.executeTranslucentAfterTerrain(renderPass);
    }

    @Inject(method = "submitEntities", at = @At("TAIL"))
    private void mcqw$submitShields(PoseStack poseStack, LevelRenderState state, SubmitNodeCollector output, CallbackInfo ci) {
        BubbleRenderer.submit(poseStack, output, state.cameraRenderState.pos);
        BeamRenderer.submit(poseStack, output, state.cameraRenderState.pos);
        CrystalRenderer.submit(poseStack, output, state.cameraRenderState.pos);
    }
}
