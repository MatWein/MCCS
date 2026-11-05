package de.mcqw.mc.mixin;

import de.mcqw.core.render.EnergyBubble;
import de.mcqw.mc.client.render.BubbleRenderer;
import de.mcqw.mc.client.StaffClient;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cloaked players see a faint glowing outline of their body, drawn like the pilot's view of a cloaked jumper in MCPJ.
 * Armor and items are not part of it. Others do not render a cloaked player at all (see EntityRenderDispatcherMixin).
 */
@Mixin(LivingEntityRenderer.class)
abstract class LivingEntityRendererMixin {
    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void mcqw$ghostType(LivingEntityRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing,
                                CallbackInfoReturnable<RenderType> result) {
        if (StaffClient.isOwnGhost(state instanceof AvatarRenderState avatar ? avatar.id : -1)) result.setReturnValue(BubbleRenderer.glow(((LivingEntityRenderer) (Object) this).getTextureLocation(state)));
    }

    @Inject(method = "getModelTint", at = @At("HEAD"), cancellable = true)
    private void mcqw$ghostTint(LivingEntityRenderState state, CallbackInfoReturnable<Integer> result) {
        if (StaffClient.isOwnGhost(state instanceof AvatarRenderState avatar ? avatar.id : -1)) result.setReturnValue(ARGB.colorFromFloat(1f, EnergyBubble.GHOST_BRIGHTNESS, EnergyBubble.GHOST_BRIGHTNESS, EnergyBubble.GHOST_BRIGHTNESS));
    }
}
