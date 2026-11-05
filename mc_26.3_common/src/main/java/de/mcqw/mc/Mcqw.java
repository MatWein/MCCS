package de.mcqw.mc;

import de.mcqw.core.McqwIds;
import net.minecraft.resources.Identifier;

/** Mod id and resource locations. */
public final class Mcqw {
    public static final String MOD_ID = McqwIds.MOD_ID;

    private Mcqw() {}

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
