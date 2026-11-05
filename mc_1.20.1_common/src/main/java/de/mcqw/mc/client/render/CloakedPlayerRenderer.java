package de.mcqw.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.mcqw.core.render.EnergyBubble;
import de.mcqw.mc.client.StaffClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * A cloaked player is invisible to others; they themselves see a faint glowing outline of their body, drawn like the
 * pilot's view of a cloaked jumper in MCPJ. Armor and items are not part of it.
 */
public final class CloakedPlayerRenderer {
    private static boolean drawingGhost;

    private CloakedPlayerRenderer() {}

    /**
     * Before an entity is rendered.
     *
     * @return true if its normal rendering has to be skipped
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static boolean intercept(LivingEntityRenderer renderer, LivingEntity entity, float yaw, float partialTick, PoseStack poseStack,
                                    MultiBufferSource buffers, int light) {
        if (drawingGhost || !(entity instanceof Player player) || !StaffClient.isHidden(player)) return false;
        if (player != Minecraft.getInstance().player) return true;
        ResourceLocation skin = renderer.getTextureLocation(player);
        RenderType body = renderer.getModel().renderType(skin);
        VertexConsumer glow = new ScaledColors(buffers.getBuffer(RenderType.eyes(skin)), EnergyBubble.GHOST_BRIGHTNESS);
        VertexConsumer nothing = new Discard();
        MultiBufferSource ghost = type -> type == body ? glow : nothing;
        boolean invisible = player.isInvisible();
        drawingGhost = true;
        // the server makes the player invisible, which would skip the body
        player.setInvisible(false);
        try {
            renderer.render(player, yaw, partialTick, poseStack, ghost, light);
        } finally {
            player.setInvisible(invisible);
            drawingGhost = false;
        }
        return true;
    }

    private record ScaledColors(VertexConsumer target, float factor) implements VertexConsumer {
        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            target.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            target.color((int) (red * factor), (int) (green * factor), (int) (blue * factor), alpha);
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            target.uv(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            target.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            target.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            target.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            target.endVertex();
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            target.defaultColor(red, green, blue, alpha);
        }

        @Override
        public void unsetDefaultColor() {
            target.unsetDefaultColor();
        }
    }

    private static final class Discard implements VertexConsumer {
        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return this;
        }

        @Override
        public void endVertex() {}

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {}

        @Override
        public void unsetDefaultColor() {}
    }
}
