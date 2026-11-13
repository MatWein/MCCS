package de.mccs.neoforge.config;

import de.mccs.core.config.ServerConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Builds the NeoForge config spec from the core's settings; register it with the mod container. */
public final class NeoForgeConfigs {
    public static final ModConfigSpec SERVER;

    static {
        NeoForgeConfigBuilder server = new NeoForgeConfigBuilder();
        ServerConfig.define(server);
        SERVER = server.build();
    }

    private NeoForgeConfigs() {}
}
