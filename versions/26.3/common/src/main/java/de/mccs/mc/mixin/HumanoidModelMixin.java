package de.mccs.mc.mixin;

import de.mccs.mc.client.StaffArmPose;
import de.mccs.mc.item.CrystalStaffItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Holds the arm carrying the staff still, on both loaders. */
@Mixin(HumanoidModel.class)
abstract class HumanoidModelMixin {
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;

    @Inject(method = "poseRightArm", at = @At("TAIL"))
    private void mccs$holdStaffRight(HumanoidRenderState state, CallbackInfo ci) {
        if (state.rightHandItemStack.getItem() instanceof CrystalStaffItem) StaffArmPose.applyRight(rightArm);
    }

    @Inject(method = "poseLeftArm", at = @At("TAIL"))
    private void mccs$holdStaffLeft(HumanoidRenderState state, CallbackInfo ci) {
        if (state.leftHandItemStack.getItem() instanceof CrystalStaffItem) StaffArmPose.applyLeft(leftArm);
    }
}
