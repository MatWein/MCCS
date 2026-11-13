package de.mccs.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mccs.core.render.EnergyBubble;
import de.mccs.mc.Mccs;
import de.mccs.mc.client.StaffClient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Draws the shield and cloak bubbles, the energy balls and the lightning of all players after clouds and weather instead of together with the entities (also
 * in first person, where the own player is not rendered).
 *
 * <p>The bubble is additive and writes no depth (so water and glass behind it stay visible). Drawn with the
 * entities, the clouds rendered later would be blended over it.
 */
public final class BubbleRenderer {
    private static final ResourceLocation TEXTURE = Mccs.id("textures/entity/shield.png");

    private BubbleRenderer() {}

    /**
     * @param worldPose  the camera rotation of the world rendering
     * @param rotationOnStack whether that rotation is still on RenderSystem's model-view stack at this point (it is
     *                        then taken out once; depends on where the loader hooks in)
     */
    public static void render(PoseStack.Pose worldPose, boolean rotationOnStack, Camera camera, float partialTick) {
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 cameraPosition = camera.getPosition();
        PoseStack poseStack = new PoseStack();
        if (!rotationOnStack) {
            poseStack.last().pose().set(new Matrix4f(worldPose.pose()));
            poseStack.last().normal().set(new Matrix3f(worldPose.normal()));
        }
        StaffClient.forEachBubble(partialTick, bubble -> {
            Vec3 position = bubble.player().getPosition(partialTick).subtract(cameraPosition);
            poseStack.pushPose();
            poseStack.translate(position.x, position.y, position.z);
            draw(poseStack.last(), buffers, bubble);
            poseStack.popPose();
        });
        BeamRenderer.draw(camera, poseStack.last(), buffers, cameraPosition, partialTick);
        EnergyBallRenderer.drawPending(worldPose, rotationOnStack, buffers);
        buffers.endBatch();
    }

    /** Several additive layers drifting at different speeds. */
    private static void draw(PoseStack.Pose pose, MultiBufferSource buffers, StaffClient.Bubble bubble) {
        EnergyBubble.Grid grid = EnergyBubble.compute(RenderSinks.view(pose), bubble.wobble(), bubble.time());
        for (EnergyBubble.Layer layer : EnergyBubble.LAYERS) {
            float u = bubble.time() * layer.uDrift();
            float v = bubble.time() * layer.vDrift();
            RenderType type = bubble.inside() ? ModRenderTypes.energyBubbleInside(TEXTURE, u, v) : ModRenderTypes.energyBubble(TEXTURE, u, v);
            EnergyBubble.emitLayer(grid, layer, RenderSinks.sink(pose, buffers.getBuffer(type), LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY), bubble.intensity());
        }
    }
}
