package de.mccs.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mccs.core.render.LightningBolt;
import de.mccs.core.render.VertexSink;
import de.mccs.mc.Mccs;
import de.mccs.mc.client.BeamClient;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Draws the staff's lightning, in camera-relative coordinates like the bubbles (see {@link BubbleRenderer}). */
final class BeamRenderer {
    private static final ResourceLocation TEXTURE = Mccs.id("textures/entity/beam.png");

    private BeamRenderer() {}

    static void draw(Camera camera, PoseStack.Pose pose, MultiBufferSource buffers, Vec3 cameraPosition, float partialTick) {
        BeamClient.forEachPath(camera, cameraPosition, partialTick, (path, scale) -> {
            VertexSink sink = RenderSinks.sink(pose, buffers.getBuffer(ModRenderTypes.beam(TEXTURE)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            LightningBolt.emitGlow(path, sink, scale);
            LightningBolt.emitCore(path, sink, scale);
        });
    }
}
