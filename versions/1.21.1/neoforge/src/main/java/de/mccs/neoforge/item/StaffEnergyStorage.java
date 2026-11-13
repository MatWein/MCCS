package de.mccs.neoforge.item;

import de.mccs.core.config.ServerConfig;
import de.mccs.core.staff.StaffEnergy;
import de.mccs.mc.staff.StaffData;
import de.mccs.mc.staff.StaffSystems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;

/** Takes energy from other mods (Mekanism, ...); it cannot be drained. */
public record StaffEnergyStorage(ItemStack stack) implements IEnergyStorage {
    @Override
    public int receiveEnergy(int amount, boolean simulate) {
        int stored = StaffSystems.stored(stack);
        int accepted = StaffEnergy.accepted(stored, StaffSystems.capacity(stack), ServerConfig.STAFF.maxReceive(), amount);
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
        return StaffSystems.capacity(stack);
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
