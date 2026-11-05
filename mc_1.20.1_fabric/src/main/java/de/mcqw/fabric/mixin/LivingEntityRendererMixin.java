package de.mcqw.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mcqw.mc.client.render.CloakedPlayerRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Cloaked players (Forge: RenderLivingEvent.Pre). */
@Mixin(LivingEntityRenderer.class)
abstract class LivingEntityRendererMixin {
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), cancellable = true)
    private void mcqw$cloak(LivingEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo callback) {
        if (CloakedPlayerRenderer.intercept((LivingEntityRenderer<?, ?>) (Object) this, entity, yaw, partialTick, poseStack, buffers, light)) callback.cancel();
    }
}
