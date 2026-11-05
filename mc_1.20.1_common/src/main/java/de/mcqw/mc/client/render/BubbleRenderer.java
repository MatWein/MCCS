package de.mcqw.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mcqw.core.render.EnergyBubble;
import de.mcqw.core.staff.BubbleAnimation;
import de.mcqw.mc.Mcqw;
import de.mcqw.mc.client.StaffClient;
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
 * Draws the shield and cloak bubbles of all players after clouds and weather instead of together with the entities (also
 * in first person, where the own player is not rendered).
 *
 * <p>The bubble is additive and writes no depth (so water and glass behind it stay visible). Drawn with the
 * entities, the clouds rendered later would be blended over it.
 */
public final class BubbleRenderer {
    private static final ResourceLocation TEXTURE = Mcqw.id("textures/entity/shield.png");

    private BubbleRenderer() {}

    /**
     * @param worldPose  the camera rotation of the world rendering
     * @param rotationOnStack whether that rotation is still on RenderSystem's model-view stack at this point (it is
     *                        then taken out once; depends on where the loader hooks in)
     */
    public static void render(PoseStack.Pose worldPose, boolean rotationOnStack, Camera camera, float partialTick) {
        Map<Player, BubbleAnimation> bubbles = StaffClient.bubbles();
        if (bubbles.isEmpty()) return;
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
            float intensity = EnergyBubble.intensity(shield, animation.cloak(partialTick), animation.flash(partialTick), player == Minecraft.getInstance().player);
            if (!EnergyBubble.isVisible(intensity)) continue;
            Vec3 position = player.getPosition(partialTick).subtract(cameraPosition);
            poseStack.pushPose();
            poseStack.translate(position.x, position.y, position.z);
            draw(poseStack.last(), buffers, intensity, EnergyBubble.wobble(shield), player.tickCount + partialTick);
            poseStack.popPose();
        }
        buffers.endBatch();
    }

    /** Several additive layers drifting at different speeds. */
    private static void draw(PoseStack.Pose pose, MultiBufferSource buffers, float intensity, float wobble, float time) {
        EnergyBubble.Grid grid = EnergyBubble.compute(RenderSinks.view(pose), wobble, time);
        for (EnergyBubble.Layer layer : EnergyBubble.LAYERS) {
            RenderType type = ModRenderTypes.energyBubble(TEXTURE, time * layer.uDrift(), time * layer.vDrift());
            EnergyBubble.emitLayer(grid, layer, RenderSinks.sink(pose, buffers.getBuffer(type), LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY), intensity);
        }
    }
}
