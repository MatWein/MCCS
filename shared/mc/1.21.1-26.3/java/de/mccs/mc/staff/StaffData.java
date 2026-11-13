package de.mccs.mc.staff;

import de.mccs.core.staff.StaffEnergy;
import de.mccs.mc.registry.MccsObjects;
import net.minecraft.world.item.ItemStack;

/** What a staff saves on its item stack (data components, since Minecraft 1.20.5). */
public final class StaffData {
    private StaffData() {}

    /** The saved energy, or {@link StaffEnergy#UNSET}. */
    public static int savedEnergy(ItemStack stack) {
        return stack.getOrDefault(MccsObjects.ENERGY, StaffEnergy.UNSET);
    }

    /** Integer.MAX_VALUE means "as full as the capacity", from before the settings are known (creative tab). */
    public static void setSavedEnergy(ItemStack stack, int energy) {
        stack.set(MccsObjects.ENERGY, energy);
    }

    public static boolean shieldOn(ItemStack stack) {
        return stack.getOrDefault(MccsObjects.SHIELD_ON, false);
    }

    public static void setShieldOn(ItemStack stack, boolean on) {
        stack.set(MccsObjects.SHIELD_ON, on);
    }

    public static boolean cloakOn(ItemStack stack) {
        return stack.getOrDefault(MccsObjects.CLOAK_ON, false);
    }

    public static void setCloakOn(ItemStack stack, boolean on) {
        stack.set(MccsObjects.CLOAK_ON, on);
    }

    public static boolean lightsOn(ItemStack stack) {
        return stack.getOrDefault(MccsObjects.LIGHTS_ON, false);
    }

    public static void setLightsOn(ItemStack stack, boolean on) {
        stack.set(MccsObjects.LIGHTS_ON, on);
    }

    public static boolean blockDamage(ItemStack stack) {
        return stack.getOrDefault(MccsObjects.BLOCK_DAMAGE, false);
    }

    public static void setBlockDamage(ItemStack stack, boolean on) {
        stack.set(MccsObjects.BLOCK_DAMAGE, on);
    }

    public static int shieldHits(ItemStack stack) {
        return stack.getOrDefault(MccsObjects.SHIELD_HITS, 0);
    }

    public static void setShieldHits(ItemStack stack, int hits) {
        stack.set(MccsObjects.SHIELD_HITS, hits);
    }
}
