package de.mccs.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mccs.core.config.ServerConfig;
import de.mccs.core.render.EnergyBubble;
import de.mccs.core.render.EnergyOrb;
import de.mccs.core.staff.BallMath;
import de.mccs.mc.Mccs;
import de.mccs.mc.entity.EnergyBallBase;
import de.mccs.mc.entity.EnergyBallEntity;
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
public final class EnergyBallRenderer extends EntityRenderer<EnergyBallEntity, EnergyBallRenderer.State> {
    private static final Identifier TEXTURE = Mccs.id("textures/entity/shield.png");
    private static final int FULL_BLOCK_LIGHT = 15;

    public static final class State extends EntityRenderState {
        float size = 1f;
        float charge;
    }

    public EnergyBallRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EnergyBallEntity ball, State state, float partialTick) {
        super.extractRenderState(ball, state, partialTick);
        state.size = (float) BallMath.charged(1.0, ServerConfig.BALL.chargedSizeFactor(), ball.charge());
        state.charge = ball.charge();
    }

    @Override
    protected int getBlockLightLevel(EnergyBallEntity entity, BlockPos pos) {
        return FULL_BLOCK_LIGHT;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0, EnergyBallBase.SIZE / 2, 0);
        poseStack.scale(state.size, state.size, state.size);
        drawOrb(poseStack, collector, state.ageInTicks, state.charge);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    /** The ball at the pose; also used while it is being charged at the staff. */
    static void drawOrb(PoseStack poseStack, SubmitNodeCollector collector, float time, float charge) {
        float[] color = EnergyOrb.color(charge);
        draw(EnergyOrb.SHELL, EnergyOrb.SHELL_INTENSITY, poseStack, collector, time, color);
        draw(EnergyOrb.CORE, EnergyOrb.CORE_INTENSITY, poseStack, collector, time, color);
    }

    private static void draw(EnergyBubble.Shape shape, float intensity, PoseStack poseStack, SubmitNodeCollector collector, float time, float[] color) {
        EnergyBubble.Grid grid = EnergyBubble.compute(shape, RenderSinks.view(poseStack.last()), EnergyOrb.WOBBLE, time);
        for (EnergyBubble.Layer layer : EnergyBubble.LAYERS) {
            RenderType type = ModRenderTypes.energyBubble(TEXTURE, time * layer.uDrift(), time * layer.vDrift());
            AfterWater.submit(poseStack, collector, type, (pose, buffer) -> EnergyBubble.emitLayer(grid, layer,
                    RenderSinks.sink(pose, buffer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY), intensity, color[0], color[1], color[2]));
        }
    }
}
