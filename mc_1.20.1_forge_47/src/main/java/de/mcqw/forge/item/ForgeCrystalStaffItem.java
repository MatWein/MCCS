package de.mcqw.forge.item;

import de.mcqw.core.config.ServerConfig;
import de.mcqw.core.config.StaffConfig;
import de.mcqw.core.staff.StaffEnergy;
import de.mcqw.mc.client.StaffArmPose;
import de.mcqw.mc.item.CrystalStaffItem;
import de.mcqw.mc.staff.StaffData;
import de.mcqw.mc.staff.StaffSystems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.Consumer;

/** Forge's additions to the staff: Forge Energy and the arm pose (on the other platforms the pose is a mixin). */
public final class ForgeCrystalStaffItem extends CrystalStaffItem {
    public ForgeCrystalStaffItem(Properties properties) {
        super(properties);
    }

    /** Takes energy from other mods (Mekanism, Thermal, ...); it cannot be drained. */
    private record EnergyView(ItemStack stack) implements IEnergyStorage {
        @Override
        public int receiveEnergy(int amount, boolean simulate) {
            StaffConfig config = ServerConfig.STAFF;
            int stored = StaffSystems.stored(stack);
            int accepted = StaffEnergy.accepted(stored, config.capacity(), config.maxReceive(), amount);
            if (!simulate && accepted > 0) StaffData.setSavedEnergy(stack, stored + accepted);
            return accepted;
        }

        @Override
        public int extractEnergy(int amount, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return StaffSystems.stored(stack);
        }

        @Override
        public int getMaxEnergyStored() {
            return ServerConfig.STAFF.capacity();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> new EnergyView(stack));
        return new ICapabilityProvider() {
            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
                return capability == ForgeCapabilities.ENERGY ? energy.cast() : LazyOptional.empty();
            }
        };
    }

    /** The stack changes constantly while the shield runs: this must neither replay the equip animation... */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !oldStack.is(newStack.getItem());
    }

    /** ...nor restart breaking a block. */
    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return !oldStack.is(newStack.getItem());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final HumanoidModel.ArmPose pose = HumanoidModel.ArmPose.create("MCQW_STAFF", false,
                    (model, entity, arm) -> {
                        if (arm == HumanoidArm.RIGHT) StaffArmPose.applyRight(model.rightArm);
                        else StaffArmPose.applyLeft(model.leftArm);
                    });

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return pose;
            }
        });
    }
}
