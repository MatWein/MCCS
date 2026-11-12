package de.mccs.neoforge.item;

import de.mccs.core.config.ServerConfig;
import de.mccs.core.staff.StaffEnergy;
import de.mccs.mc.registry.MccsObjects;
import de.mccs.mc.staff.StaffData;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/** Takes energy from other mods (Mekanism, ...); it cannot be drained. A new staff counts with its start charge. */
public final class StaffEnergyHandler extends ItemAccessEnergyHandler {
    /** The staff's capacity (it depends on the kind of staff). */
    private final int staffCapacity;

    public StaffEnergyHandler(ItemAccess access, int capacity) {
        super(access, MccsObjects.ENERGY, capacity, ServerConfig.STAFF.maxReceive(), 0);
        staffCapacity = capacity;
    }

    @Override
    protected ItemResource update(ItemResource resource, int amount) {
        return super.update(resource, amount).with(DataComponents.CUSTOM_MODEL_DATA, StaffData.crystalLook(amount, staffCapacity));
    }

    @Override
    protected int getAmountFrom(ItemResource resource) {
        if (!resource.is(validItem)) return 0;
        return StaffEnergy.stored(resource.getOrDefault(MccsObjects.ENERGY, StaffEnergy.UNSET), staffCapacity, ServerConfig.STAFF.startCharge());
    }
}
