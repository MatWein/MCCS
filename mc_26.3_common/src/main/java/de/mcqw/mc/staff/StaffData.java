package de.mcqw.mc.staff;

import de.mcqw.core.config.ServerConfig;
import de.mcqw.core.staff.StaffEnergy;
import de.mcqw.mc.registry.McqwObjects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

import java.util.List;

/** What a staff saves on its item stack (Minecraft 26.3: data components). */
public final class StaffData {
    private StaffData() {}

    /** The saved energy, or {@link StaffEnergy#UNSET}. */
    public static int savedEnergy(ItemStack stack) {
        return stack.getOrDefault(McqwObjects.ENERGY, StaffEnergy.UNSET);
    }

    public static void setSavedEnergy(ItemStack stack, int energy) {
        stack.set(McqwObjects.ENERGY, energy);
        stack.set(DataComponents.CUSTOM_MODEL_DATA, crystalLook(energy));
    }

    /** Selects the crystal's model at this charge (items/crystal_staff.json). */
    public static CustomModelData crystalLook(int energy) {
        // Integer.MAX_VALUE: "as full as the capacity", from before the settings are known (creative tab)
        int look = energy == Integer.MAX_VALUE ? 0 : StaffEnergy.crystalLook(energy, ServerConfig.STAFF.capacity());
        return new CustomModelData(List.of((float) look), List.of(), List.of(), List.of());
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
