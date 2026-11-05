package de.mcqw.neoforge.item;

import de.mcqw.core.config.ServerConfig;
import de.mcqw.core.config.StaffConfig;
import de.mcqw.core.staff.StaffEnergy;
import de.mcqw.mc.registry.McqwObjects;
import de.mcqw.mc.staff.StaffData;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/** Takes energy from other mods (Mekanism, ...); it cannot be drained. A new staff counts with its start charge. */
public final class StaffEnergyHandler extends ItemAccessEnergyHandler {
    public StaffEnergyHandler(ItemAccess access) {
        super(access, McqwObjects.ENERGY, ServerConfig.STAFF.capacity(), ServerConfig.STAFF.maxReceive(), 0);
    }

    @Override
    protected ItemResource update(ItemResource resource, int amount) {
        return super.update(resource, amount).with(DataComponents.CUSTOM_MODEL_DATA, StaffData.crystalLook(amount));
    }

    @Override
    protected int getAmountFrom(ItemResource resource) {
        if (!resource.is(validItem)) return 0;
        StaffConfig config = ServerConfig.STAFF;
        return StaffEnergy.stored(resource.getOrDefault(McqwObjects.ENERGY, StaffEnergy.UNSET), config.capacity(), config.startCharge());
    }
}
