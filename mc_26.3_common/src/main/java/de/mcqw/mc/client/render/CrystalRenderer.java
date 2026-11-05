package de.mcqw.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mcqw.core.render.CrystalGlow;
import de.mcqw.core.render.LightningBolt;
import de.mcqw.mc.Mcqw;
import de.mcqw.mc.client.StaffEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;

/** Submits what the staffs in the players' hands show: the crystal's light and the ball being charged. */
public final class CrystalRenderer {
    private static final Identifier GLOW = Mcqw.id("textures/entity/crystal_glow.png");
    private static final Identifier ARC = Mcqw.id("textures/entity/beam.png");

    private CrystalRenderer() {}

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, Vec3 cameraPosition) {
        Minecraft minecraft = Minecraft.getInstance();
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        StaffEffects.collect(minecraft.gameRenderer.mainCamera(), cameraPosition, partialTick, new StaffEffects.Output() {
            @Override
            public void halo(CrystalGlow.Halo halo, float[] center, float[] right, float[] up, float scale) {
                AfterWater.submit(poseStack, collector, ModRenderTypes.beam(GLOW), (pose, buffer) -> CrystalGlow.emitHalo(halo, center, right, up, scale,
                        RenderSinks.sink(pose, buffer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY)));
            }

            @Override
            public void arc(float[][] path, float scale) {
                AfterWater.submit(poseStack, collector, ModRenderTypes.beam(ARC), (pose, buffer) -> LightningBolt.emitCore(path,
                        RenderSinks.sink(pose, buffer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY), scale));
            }

            @Override
            public void orb(float[] center, float size, float charge, float time) {
                poseStack.pushPose();
                poseStack.translate(center[0], center[1], center[2]);
                poseStack.scale(size, size, size);
                EnergyBallRenderer.drawOrb(poseStack, collector, time, charge);
                poseStack.popPose();
            }
        });
    }
}
