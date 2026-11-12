package de.mccs.mc;

import de.mccs.core.MccsIds;
import net.minecraft.resources.ResourceLocation;

/** Mod id and resource locations. */
public final class Mccs {
    public static final String MOD_ID = MccsIds.MOD_ID;

    private Mccs() {}

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
