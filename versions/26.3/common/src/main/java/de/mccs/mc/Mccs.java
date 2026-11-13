package de.mccs.mc;

import de.mccs.core.MccsIds;
import net.minecraft.resources.Identifier;

public final class Mccs {
    public static final String MOD_ID = MccsIds.MOD_ID;

    private Mccs() {}

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
