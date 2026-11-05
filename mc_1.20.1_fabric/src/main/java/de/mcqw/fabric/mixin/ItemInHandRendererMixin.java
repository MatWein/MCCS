package de.mcqw.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mcqw.mc.client.StaffClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** No first person hand while cloaked (Forge: RenderHandEvent). */
@Mixin(ItemInHandRenderer.class)
abstract class ItemInHandRendererMixin {
    /** Right clicks do not replay the staff's equip animation. */
    @Inject(method = "itemUsed", at = @At("HEAD"), cancellable = true)
    private void mcqw$keepStaff(InteractionHand hand, CallbackInfo callback) {
        if (StaffClient.keepsHandSteady(hand)) callback.cancel();
    }

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void mcqw$hideHand(float partialTick, PoseStack poseStack, MultiBufferSource.BufferSource buffers, LocalPlayer player, int light, CallbackInfo callback) {
        if (StaffClient.hidesOwnHand()) callback.cancel();
    }
}
