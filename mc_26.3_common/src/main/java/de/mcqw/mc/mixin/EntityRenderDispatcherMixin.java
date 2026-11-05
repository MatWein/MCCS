package de.mcqw.mc.mixin;

import de.mcqw.mc.client.StaffClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Other players' cloaked players are not rendered at all, with everything they carry. */
@Mixin(EntityRenderDispatcher.class)
abstract class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void mcqw$hideCloaked(E entity, Frustum culler, double camX, double camY, double camZ, float partialTicks,
                                                     CallbackInfoReturnable<Boolean> result) {
        if (entity instanceof Player player && player != Minecraft.getInstance().player && StaffClient.isHidden(player)) result.setReturnValue(false);
    }
}
