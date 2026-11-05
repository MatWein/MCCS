package de.mcqw.mc.staff;

import de.mcqw.core.staff.StaffEnergy;
import de.mcqw.mc.registry.McqwObjects;
import net.minecraft.world.item.ItemStack;

/** What a staff saves on its item stack (Minecraft 26.3: data components). */
public final class StaffData {
    private StaffData() {}

    /** The saved energy, or {@link StaffEnergy#UNSET}. */
    public static int savedEnergy(ItemStack stack) {
        return stack.getOrDefault(McqwObjects.ENERGY, StaffEnergy.UNSET);
    }

    public static void setSavedEnergy(ItemStack stack, int energy) {
        stack.set(McqwObjects.ENERGY, energy);
    }

    public static boolean shieldOn(ItemStack stack) {
        return stack.getOrDefault(McqwObjects.SHIELD_ON, false);
    }

    public static void setShieldOn(ItemStack stack, boolean on) {
        stack.set(McqwObjects.SHIELD_ON, on);
    }

    public static boolean cloakOn(ItemStack stack) {
        return stack.getOrDefault(McqwObjects.CLOAK_ON, false);
    }

    public static void setCloakOn(ItemStack stack, boolean on) {
        stack.set(McqwObjects.CLOAK_ON, on);
    }

    public static int shieldHits(ItemStack stack) {
        return stack.getOrDefault(McqwObjects.SHIELD_HITS, 0);
    }

    public static void setShieldHits(ItemStack stack, int hits) {
        stack.set(McqwObjects.SHIELD_HITS, hits);
    }
}
