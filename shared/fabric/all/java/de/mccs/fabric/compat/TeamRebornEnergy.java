package de.mccs.fabric.compat;

import de.mccs.core.config.ServerConfig;
import de.mccs.core.config.StaffConfig;
import de.mccs.core.staff.StaffEnergy;
import de.mccs.mc.staff.StaffData;
import de.mccs.mc.staff.StaffSystems;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;

/**
 * Team Reborn Energy (the energy API of Fabric's tech mods): the staff accepts energy. Only loaded when the API is
 * installed; one unit counts as one Forge Energy unit.
 */
public final class TeamRebornEnergy {
    private TeamRebornEnergy() {}

    public static void register(Item staff) {
        EnergyStorage.ITEM.registerForItems((stack, context) -> new StaffStorage(context, staff), staff);
    }

    /** Insert only; the transaction restores the old stack when it is aborted. */
    private record StaffStorage(ContainerItemContext context, Item staff) implements EnergyStorage {
        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            ItemVariant variant = context.getItemVariant();
            if (!variant.isOf(staff)) return 0;
            ItemStack stack = variant.toStack();
            StaffConfig config = ServerConfig.STAFF;
            int stored = StaffSystems.stored(stack);
            int accepted = StaffEnergy.accepted(stored, config.capacity(), config.maxReceive(), maxAmount);
            if (accepted <= 0) return 0;
            StaffData.setSavedEnergy(stack, stored + accepted);
            return context.exchange(ItemVariant.of(stack), 1, transaction) == 1 ? accepted : 0;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public long getAmount() {
            ItemVariant variant = context.getItemVariant();
            return variant.isOf(staff) ? StaffSystems.stored(variant.toStack()) : 0;
        }

        @Override
        public long getCapacity() {
            return context.getItemVariant().isOf(staff) ? ServerConfig.STAFF.capacity() : 0;
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }
    }
}
