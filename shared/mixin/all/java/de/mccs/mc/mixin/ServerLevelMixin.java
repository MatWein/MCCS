package de.mccs.mc.mixin;

import de.mccs.mc.staff.StaffLightning;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Natural lightning strikes a player holding up the staff's shield, like a lightning rod ({@link StaffLightning#target}). */
@Mixin(ServerLevel.class)
abstract class ServerLevelMixin {
    @Inject(method = "findLightningTargetAround", at = @At("RETURN"), cancellable = true)
    private void mccs$shieldAttractsLightning(BlockPos pos, CallbackInfoReturnable<BlockPos> callback) {
        callback.setReturnValue(StaffLightning.target((ServerLevel) (Object) this, callback.getReturnValue()));
    }
}
