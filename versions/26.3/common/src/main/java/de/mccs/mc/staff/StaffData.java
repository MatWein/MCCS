package de.mccs.mc.staff;

import de.mccs.core.config.ServerConfig;
import de.mccs.core.staff.StaffEnergy;
import de.mccs.mc.registry.MccsObjects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

import java.util.List;

/** What a staff saves on its item stack (Minecraft 26.3: data components). */
public final class StaffData {
    private StaffData() {}

    /** The saved energy, or {@link StaffEnergy#UNSET}. */
    public static int savedEnergy(ItemStack stack) {
        return stack.getOrDefault(MccsObjects.ENERGY, StaffEnergy.UNSET);
    }

    public static void setSavedEnergy(ItemStack stack, int energy) {
        stack.set(MccsObjects.ENERGY, energy);
        stack.set(DataComponents.CUSTOM_MODEL_DATA, crystalLook(energy));
    }

    /** Selects the crystal's model at this charge (items/crystal_staff.json). */
    public static CustomModelData crystalLook(int energy) {
        // Integer.MAX_VALUE: "as full as the capacity", from before the settings are known (creative tab)
        int look = energy == Integer.MAX_VALUE ? 0 : StaffEnergy.crystalLook(energy, ServerConfig.STAFF.capacity());
        return new CustomModelData(List.of((float) look), List.of(), List.of(), List.of());
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
