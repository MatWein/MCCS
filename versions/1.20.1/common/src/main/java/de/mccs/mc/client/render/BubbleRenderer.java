package de.mccs.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mccs.core.render.EnergyBubble;
import de.mccs.core.staff.BubbleAnimation;
import de.mccs.mc.Mccs;
import de.mccs.mc.client.StaffClient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Map;

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
        Map<Player, BubbleAnimation> bubbles = StaffClient.bubbles();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 cameraPosition = camera.getPosition();
        PoseStack poseStack = new PoseStack();
        if (!rotationOnStack) {
            poseStack.last().pose().set(new Matrix4f(worldPose.pose()));
            poseStack.last().normal().set(new Matrix3f(worldPose.normal()));
        }
        for (Map.Entry<Player, BubbleAnimation> bubble : bubbles.entrySet()) {
            Player player = bubble.getKey();
            BubbleAnimation animation = bubble.getValue();
            float shield = animation.shield(partialTick);
            boolean inside = isInside(player);
            float intensity = inside ? EnergyBubble.insideIntensity(shield, animation.flash(partialTick))
                    : EnergyBubble.intensity(shield, animation.cloak(partialTick), animation.flash(partialTick), player == Minecraft.getInstance().player);
            if (!EnergyBubble.isVisible(intensity)) continue;
            Vec3 position = player.getPosition(partialTick).subtract(cameraPosition);
            poseStack.pushPose();
            poseStack.translate(position.x, position.y, position.z);
            draw(poseStack.last(), buffers, intensity, EnergyBubble.wobble(shield), player.tickCount + partialTick, inside);
            poseStack.popPose();
        }
        BeamRenderer.draw(camera, poseStack.last(), buffers, cameraPosition, partialTick);
        EnergyBallRenderer.drawPending(worldPose, rotationOnStack, buffers);
        buffers.endBatch();
    }

    /** The camera sits inside the bubble of the player it belongs to in first person. */
    private static boolean isInside(Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        return player == minecraft.player && minecraft.getCameraEntity() == player && minecraft.options.getCameraType().isFirstPerson();
    }

    /** Several additive layers drifting at different speeds. */
    private static void draw(PoseStack.Pose pose, MultiBufferSource buffers, float intensity, float wobble, float time, boolean inside) {
        EnergyBubble.Grid grid = EnergyBubble.compute(RenderSinks.view(pose), wobble, time);
        for (EnergyBubble.Layer layer : EnergyBubble.LAYERS) {
            float u = time * layer.uDrift();
            float v = time * layer.vDrift();
            RenderType type = inside ? ModRenderTypes.energyBubbleInside(TEXTURE, u, v) : ModRenderTypes.energyBubble(TEXTURE, u, v);
            EnergyBubble.emitLayer(grid, layer, RenderSinks.sink(pose, buffers.getBuffer(type), LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY), intensity);
        }
    }
}
