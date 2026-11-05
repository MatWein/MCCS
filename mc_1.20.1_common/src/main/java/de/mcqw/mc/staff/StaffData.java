package de.mcqw.mc.staff;

import de.mcqw.core.config.ServerConfig;
import de.mcqw.core.staff.StaffEnergy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** What a staff saves on its item stack (Minecraft 1.20.1: the stack's tag). */
public final class StaffData {
    private static final String ENERGY = "Energy";
    /** Vanilla's property that selects the crystal's model. */
    private static final String CRYSTAL_LOOK = "CustomModelData";
    private static final String SHIELD_ON = "ShieldOn";
    private static final String CLOAK_ON = "CloakOn";
    private static final String LIGHTS_ON = "LightsOn";
    private static final String BLOCK_DAMAGE = "BlockDamage";
    private static final String SHIELD_HITS = "ShieldHits";

    private StaffData() {}

    /** The saved energy, or {@link StaffEnergy#UNSET}. */
    public static int savedEnergy(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(ENERGY) ? tag.getInt(ENERGY) : StaffEnergy.UNSET;
    }

    public static void setSavedEnergy(ItemStack stack, int energy) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(ENERGY, energy);
        // Integer.MAX_VALUE: "as full as the capacity", from before the settings are known (creative tab)
        int look = energy == Integer.MAX_VALUE ? 0 : StaffEnergy.crystalLook(energy, ServerConfig.STAFF.capacity());
        if (look == 0) tag.remove(CRYSTAL_LOOK);
        else tag.putInt(CRYSTAL_LOOK, look);
    }

    public static boolean shieldOn(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(SHIELD_ON);
    }

    public static void setShieldOn(ItemStack stack, boolean on) {
        stack.getOrCreateTag().putBoolean(SHIELD_ON, on);
    }

    public static boolean cloakOn(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(CLOAK_ON);
    }

    public static void setCloakOn(ItemStack stack, boolean on) {
        stack.getOrCreateTag().putBoolean(CLOAK_ON, on);
    }

    public static boolean lightsOn(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(LIGHTS_ON);
    }

    public static void setLightsOn(ItemStack stack, boolean on) {
        stack.getOrCreateTag().putBoolean(LIGHTS_ON, on);
    }

    public static boolean blockDamage(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(BLOCK_DAMAGE);
    }

    public static void setBlockDamage(ItemStack stack, boolean on) {
        stack.getOrCreateTag().putBoolean(BLOCK_DAMAGE, on);
    }

    public static int shieldHits(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(SHIELD_HITS) : 0;
    }

    public static void setShieldHits(ItemStack stack, int hits) {
        stack.getOrCreateTag().putInt(SHIELD_HITS, hits);
    }
}
