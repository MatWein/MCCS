package de.mcqw.mc.mixin;

import de.mcqw.mc.client.StaffClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Right clicks do not replay the staff's equip animation. */
@Mixin(LocalPlayer.class)
abstract class LocalPlayerMixin {
    @Inject(method = "itemUsed", at = @At("HEAD"), cancellable = true)
    private void mcqw$keepStaff(InteractionHand hand, CallbackInfo callback) {
        if (StaffClient.keepsHandSteady(hand)) callback.cancel();
    }
}
