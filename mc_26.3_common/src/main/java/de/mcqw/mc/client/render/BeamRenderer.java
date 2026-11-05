package de.mcqw.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mcqw.core.render.LightningBolt;
import de.mcqw.core.render.VertexSink;
import de.mcqw.mc.Mcqw;
import de.mcqw.mc.client.BeamClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;

/** Submits the staff's lightning, in camera-relative coordinates, together with the entities. */
public final class BeamRenderer {
    private static final Identifier TEXTURE = Mcqw.id("textures/entity/beam.png");

    private BeamRenderer() {}

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, Vec3 cameraPosition) {
        Minecraft minecraft = Minecraft.getInstance();
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        BeamClient.forEachPath(minecraft.gameRenderer.mainCamera(), cameraPosition, partialTick, (path, scale) ->
                AfterWater.submit(poseStack, collector, ModRenderTypes.beam(TEXTURE), (pose, buffer) -> {
                    VertexSink sink = RenderSinks.sink(pose, buffer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
                    LightningBolt.emitGlow(path, sink, scale);
                    LightningBolt.emitCore(path, sink, scale);
                }));
    }
}
