package de.mcqw.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mcqw.core.render.EnergyBubble;
import de.mcqw.core.staff.BubbleAnimation;
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

/** Submits the shield and cloak bubbles of all players together with the entities (also in first person, where the own player is not rendered). */
public final class BubbleRenderer {
    private static final Identifier TEXTURE = Mcqw.id("textures/entity/shield.png");

    private BubbleRenderer() {}

    /** The glow render type for the mixin drawing the cloaked player's outline. */
    public static RenderType glow(Identifier texture) {
        return ModRenderTypes.glow(texture);
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, Vec3 cameraPosition) {
        Map<Player, BubbleAnimation> bubbles = StaffClient.bubbles();
        if (bubbles.isEmpty()) return;
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
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
            draw(poseStack, collector, intensity, EnergyBubble.wobble(shield), player.tickCount + partialTick, inside);
            poseStack.popPose();
        }
    }

    /** The camera sits inside the bubble of the player it belongs to in first person. */
    private static boolean isInside(Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        return player == minecraft.player && minecraft.getCameraEntity() == player && minecraft.options.getCameraType().isFirstPerson();
    }

    /** Several additive layers drifting at different speeds. */
    private static void draw(PoseStack poseStack, SubmitNodeCollector collector, float intensity, float wobble, float time, boolean inside) {
        EnergyBubble.Grid grid = EnergyBubble.compute(RenderSinks.view(poseStack.last()), wobble, time);
        for (EnergyBubble.Layer layer : EnergyBubble.LAYERS) {
            float u = time * layer.uDrift();
            float v = time * layer.vDrift();
            RenderType type = inside ? ModRenderTypes.energyBubbleInside(TEXTURE, u, v) : ModRenderTypes.energyBubble(TEXTURE, u, v);
            collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> EnergyBubble.emitLayer(grid, layer,
                    RenderSinks.sink(pose, buffer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY), intensity));
        }
    }
}
