package de.mcqw.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mcqw.core.render.EnergyBubble;
import de.mcqw.core.render.EnergyOrb;
import de.mcqw.mc.Mcqw;
import de.mcqw.mc.entity.EnergyBallBase;
import de.mcqw.mc.entity.EnergyBallEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;

/** The energy ball, in the look of the shield bubble. */
public final class EnergyBallRenderer extends EntityRenderer<EnergyBallEntity, EntityRenderState> {
    private static final Identifier TEXTURE = Mcqw.id("textures/entity/shield.png");
    private static final int FULL_BLOCK_LIGHT = 15;

    public EnergyBallRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    protected int getBlockLightLevel(EnergyBallEntity entity, BlockPos pos) {
        return FULL_BLOCK_LIGHT;
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0, EnergyBallBase.SIZE / 2, 0);
        draw(EnergyOrb.SHELL, EnergyOrb.SHELL_INTENSITY, poseStack, collector, state.ageInTicks);
        draw(EnergyOrb.CORE, EnergyOrb.CORE_INTENSITY, poseStack, collector, state.ageInTicks);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void draw(EnergyBubble.Shape shape, float intensity, PoseStack poseStack, SubmitNodeCollector collector, float time) {
        EnergyBubble.Grid grid = EnergyBubble.compute(shape, RenderSinks.view(poseStack.last()), EnergyOrb.WOBBLE, time);
        for (EnergyBubble.Layer layer : EnergyBubble.LAYERS) {
            RenderType type = ModRenderTypes.energyBubble(TEXTURE, time * layer.uDrift(), time * layer.vDrift());
            collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> EnergyBubble.emitLayer(grid, layer,
                    RenderSinks.sink(pose, buffer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY), intensity));
        }
    }
}
