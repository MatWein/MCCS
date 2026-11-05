package de.mcqw.mc;

import de.mcqw.core.McqwIds;
import net.minecraft.resources.ResourceLocation;

/** Mod id and resource locations. */
public final class Mcqw {
    public static final String MOD_ID = McqwIds.MOD_ID;

    private Mcqw() {}

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
