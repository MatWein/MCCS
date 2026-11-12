package de.mccs.neoforge.item;

import de.mccs.core.staff.StaffKind;
import de.mccs.mc.item.CrystalStaffItem;
import net.minecraft.world.item.ItemStack;

/** The stack changes constantly while the shield runs: this must neither replay the equip animation nor restart breaking a block. */
public final class NeoForgeCrystalStaffItem extends CrystalStaffItem {
    public NeoForgeCrystalStaffItem(Properties properties, StaffKind kind) {
        super(properties, kind);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !oldStack.is(newStack.getItem());
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return !oldStack.is(newStack.getItem());
    }
}
