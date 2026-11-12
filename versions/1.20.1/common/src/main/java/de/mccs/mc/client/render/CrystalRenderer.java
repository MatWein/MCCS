package de.mccs.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mccs.core.render.CrystalGlow;
import de.mccs.core.render.LightningBolt;
import de.mccs.core.render.VertexSink;
import de.mccs.mc.Mccs;
import de.mccs.mc.client.StaffEffects;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Draws what the staffs in the players' hands show: the crystal's light and the ball being charged. */
final class CrystalRenderer {
    private static final ResourceLocation GLOW = Mccs.id("textures/entity/crystal_glow.png");
    private static final ResourceLocation ARC = Mccs.id("textures/entity/beam.png");

    private CrystalRenderer() {}

    static void draw(Camera camera, PoseStack.Pose base, MultiBufferSource buffers, Vec3 cameraPosition, float partialTick) {
        StaffEffects.collect(camera, cameraPosition, partialTick, new StaffEffects.Output() {
            @Override
            public void halo(CrystalGlow.Halo halo, float[] center, float[] right, float[] up, float scale) {
                CrystalGlow.emitHalo(halo, center, right, up, scale, sink(GLOW));
            }

            @Override
            public void arc(float[][] path, float scale) {
                LightningBolt.emitCore(path, sink(ARC), scale);
            }

            @Override
            public void orb(float[] center, float size, float charge, float time) {
                PoseStack poseStack = new PoseStack();
                poseStack.last().pose().set(base.pose());
                poseStack.last().normal().set(base.normal());
                poseStack.translate(center[0], center[1], center[2]);
                poseStack.scale(size, size, size);
                EnergyBallRenderer.drawOrb(poseStack.last(), buffers, time, charge);
            }

            private VertexSink sink(ResourceLocation texture) {
                return RenderSinks.sink(base, buffers.getBuffer(ModRenderTypes.beam(texture)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            }
        });
    }
}
