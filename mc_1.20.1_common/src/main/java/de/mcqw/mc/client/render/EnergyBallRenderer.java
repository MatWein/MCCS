package de.mcqw.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mcqw.core.render.EnergyBubble;
import de.mcqw.core.render.EnergyOrb;
import de.mcqw.mc.Mcqw;
import de.mcqw.mc.entity.EnergyBallBase;
import de.mcqw.mc.entity.EnergyBallEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/** The energy ball, in the look of the shield bubble. */
public final class EnergyBallRenderer extends EntityRenderer<EnergyBallEntity> {
    private static final ResourceLocation TEXTURE = Mcqw.id("textures/entity/shield.png");
    private static final int FULL_BLOCK_LIGHT = 15;

    public EnergyBallRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0;
    }

    @Override
    public ResourceLocation getTextureLocation(EnergyBallEntity entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(EnergyBallEntity entity, BlockPos pos) {
        return FULL_BLOCK_LIGHT;
    }

    @Override
    public void render(EnergyBallEntity ball, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        float time = ball.tickCount + partialTick;
        poseStack.pushPose();
        poseStack.translate(0, EnergyBallBase.SIZE / 2, 0);
        draw(EnergyOrb.SHELL, EnergyOrb.SHELL_INTENSITY, poseStack.last(), buffers, time);
        draw(EnergyOrb.CORE, EnergyOrb.CORE_INTENSITY, poseStack.last(), buffers, time);
        poseStack.popPose();
        super.render(ball, yaw, partialTick, poseStack, buffers, light);
    }

    private static void draw(EnergyBubble.Shape shape, float intensity, PoseStack.Pose pose, MultiBufferSource buffers, float time) {
        EnergyBubble.Grid grid = EnergyBubble.compute(shape, RenderSinks.view(pose), EnergyOrb.WOBBLE, time);
        for (EnergyBubble.Layer layer : EnergyBubble.LAYERS) {
            RenderType type = ModRenderTypes.energyBubble(TEXTURE, time * layer.uDrift(), time * layer.vDrift());
            EnergyBubble.emitLayer(grid, layer, RenderSinks.sink(pose, buffers.getBuffer(type), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY), intensity);
        }
    }
}
