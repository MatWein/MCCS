package de.mccs.mc.item;

import de.mccs.mc.staff.StaffData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class StaffStacks {
    private StaffStacks() {}

    /** A fully charged staff for the creative tab, made before the settings (and so the capacity) are known. */
    public static ItemStack fullyCharged(Item staff) {
        ItemStack stack = new ItemStack(staff);
        StaffData.setSavedEnergy(stack, Integer.MAX_VALUE);
        return stack;
    }
}
