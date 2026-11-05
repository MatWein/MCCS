package de.mcqw.forge.item;

import de.mcqw.mc.client.StaffArmPose;
import de.mcqw.mc.item.CrystalStaffItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Adds the staff's arm pose, which Forge 1.20.1 offers per item (on the other platforms it is a mixin). */
public final class ForgeCrystalStaffItem extends CrystalStaffItem {
    public ForgeCrystalStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final HumanoidModel.ArmPose pose = HumanoidModel.ArmPose.create("MCQW_STAFF", false,
                    (model, entity, arm) -> StaffArmPose.apply(arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm));

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return pose;
            }
        });
    }
}
