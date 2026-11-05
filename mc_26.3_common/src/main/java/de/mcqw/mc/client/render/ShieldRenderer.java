package de.mcqw.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mcqw.core.render.EnergyBubble;
import de.mcqw.core.staff.ShieldAnimation;
import de.mcqw.mc.Mcqw;
import de.mcqw.mc.client.StaffClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

/** Submits the shield bubbles of all players together with the entities (also in first person, where the own player is not rendered). */
public final class ShieldRenderer {
    private static final Identifier TEXTURE = Mcqw.id("textures/entity/shield.png");

    private ShieldRenderer() {}

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, Vec3 cameraPosition) {
        Map<Player, ShieldAnimation> bubbles = StaffClient.bubbles();
        if (bubbles.isEmpty()) return;
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        for (Map.Entry<Player, ShieldAnimation> bubble : bubbles.entrySet()) {
            Player player = bubble.getKey();
            ShieldAnimation animation = bubble.getValue();
            float shield = animation.shield(partialTick);
            float intensity = EnergyBubble.intensity(shield, animation.flash(partialTick));
            if (!EnergyBubble.isVisible(intensity)) continue;
            Vec3 position = player.getPosition(partialTick).subtract(cameraPosition);
            poseStack.pushPose();
            poseStack.translate(position.x, position.y, position.z);
            draw(poseStack, collector, intensity, EnergyBubble.wobble(shield), player.tickCount + partialTick);
            poseStack.popPose();
        }
    }

    /** Several additive layers drifting at different speeds. */
    private static void draw(PoseStack poseStack, SubmitNodeCollector collector, float intensity, float wobble, float time) {
        EnergyBubble.Grid grid = EnergyBubble.compute(RenderSinks.view(poseStack.last()), wobble, time);
        for (EnergyBubble.Layer layer : EnergyBubble.LAYERS) {
            RenderType type = ModRenderTypes.energyBubble(TEXTURE, time * layer.uDrift(), time * layer.vDrift());
            collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> EnergyBubble.emitLayer(grid, layer,
                    RenderSinks.sink(pose, buffer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY), intensity));
        }
    }
}
