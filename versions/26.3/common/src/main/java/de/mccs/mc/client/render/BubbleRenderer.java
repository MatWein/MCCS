package de.mccs.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mccs.core.render.EnergyBubble;
import de.mccs.mc.Mccs;
import de.mccs.mc.client.ClientCompat;
import de.mccs.mc.client.StaffClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;

/** Submits the shield and cloak bubbles of all players together with the entities (also in first person, where the own player is not rendered). */
public final class BubbleRenderer {
    private static final Identifier TEXTURE = Mccs.id("textures/entity/shield.png");

    private BubbleRenderer() {}

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, Vec3 cameraPosition) {
        float partialTick = ClientCompat.partialTick(Minecraft.getInstance());
        StaffClient.forEachBubble(partialTick, bubble -> {
            Vec3 position = bubble.player().getPosition(partialTick).subtract(cameraPosition);
            poseStack.pushPose();
            poseStack.translate(position.x, position.y, position.z);
            draw(poseStack, collector, bubble);
            poseStack.popPose();
        });
    }

    /** Several additive layers drifting at different speeds. */
    private static void draw(PoseStack poseStack, SubmitNodeCollector collector, StaffClient.Bubble bubble) {
        EnergyBubble.Grid grid = EnergyBubble.compute(RenderSinks.view(poseStack.last()), bubble.wobble(), bubble.time());
        for (EnergyBubble.Layer layer : EnergyBubble.LAYERS) {
            float u = bubble.time() * layer.uDrift();
            float v = bubble.time() * layer.vDrift();
            RenderType type = bubble.inside() ? ModRenderTypes.energyBubbleInside(TEXTURE, u, v) : ModRenderTypes.energyBubble(TEXTURE, u, v);
            AfterWater.submit(poseStack, collector, type, (pose, buffer) -> EnergyBubble.emitLayer(grid, layer,
                    RenderSinks.sink(pose, buffer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY), bubble.intensity()));
        }
    }
}
