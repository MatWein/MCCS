package de.mccs.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * Translucent custom geometry is normally drawn before water and clouds. Our additive effects write no depth (so that what is
 * behind them stays visible), so water and clouds would be drawn over them as if they were in front. This later phase, which
 * vanilla uses for particles, runs after both (see LevelRendererMixin).
 */
final class AfterWater {
    private AfterWater() {}

    static void submit(PoseStack poseStack, SubmitNodeCollector collector, RenderType type, SubmitNodeCollector.CustomGeometryRenderer geometry) {
        if (collector instanceof SubmitNodeStorage storage) {
            storage.order(0).afterTerrain.submit(new CustomFeatureRenderer.Submit(poseStack.last().copy(), type, geometry));
        } else {
            collector.submitCustomGeometry(poseStack, type, geometry);
        }
    }
}
