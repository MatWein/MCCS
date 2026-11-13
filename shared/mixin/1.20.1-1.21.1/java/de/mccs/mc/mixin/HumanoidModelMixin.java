package de.mccs.mc.mixin;

import de.mccs.mc.client.StaffArmPose;
import de.mccs.mc.item.CrystalStaffItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Holds the arm carrying the staff still (Forge 1.20.1: the arm pose of its item extension). */
@Mixin(HumanoidModel.class)
abstract class HumanoidModelMixin {
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;

    @Inject(method = "poseRightArm", at = @At("TAIL"))
    private void mccs$holdStaffRight(LivingEntity entity, CallbackInfo ci) {
        if (holdsStaff(entity, HumanoidArm.RIGHT)) StaffArmPose.applyRight(rightArm);
    }

    @Inject(method = "poseLeftArm", at = @At("TAIL"))
    private void mccs$holdStaffLeft(LivingEntity entity, CallbackInfo ci) {
        if (holdsStaff(entity, HumanoidArm.LEFT)) StaffArmPose.applyLeft(leftArm);
    }

    private static boolean holdsStaff(LivingEntity entity, HumanoidArm arm) {
        var stack = entity.getMainArm() == arm ? entity.getMainHandItem() : entity.getOffhandItem();
        return stack.getItem() instanceof CrystalStaffItem;
    }
}
