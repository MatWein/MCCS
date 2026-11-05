package de.mcqw.mc.staff;

import de.mcqw.core.staff.StaffEnergy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** What a staff saves on its item stack (Minecraft 1.20.1: the stack's tag). */
public final class StaffData {
    private static final String ENERGY = "Energy";
    private static final String SHIELD_ON = "ShieldOn";
    private static final String SHIELD_HITS = "ShieldHits";

    private StaffData() {}

    /** The saved energy, or {@link StaffEnergy#UNSET}. */
    public static int savedEnergy(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(ENERGY) ? tag.getInt(ENERGY) : StaffEnergy.UNSET;
    }

    public static void setSavedEnergy(ItemStack stack, int energy) {
        stack.getOrCreateTag().putInt(ENERGY, energy);
    }

    public static boolean shieldOn(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(SHIELD_ON);
    }

    public static void setShieldOn(ItemStack stack, boolean on) {
        stack.getOrCreateTag().putBoolean(SHIELD_ON, on);
    }

    public static int shieldHits(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(SHIELD_HITS) : 0;
    }

    public static void setShieldHits(ItemStack stack, int hits) {
        stack.getOrCreateTag().putInt(SHIELD_HITS, hits);
    }
}
