package de.mccs.forge.config;

import de.mccs.core.config.ServerConfig;
import net.minecraftforge.common.ForgeConfigSpec;

/** Builds the Forge config spec from the core's settings; register it with the mod loading context. */
public final class ForgeConfigs {
    public static final ForgeConfigSpec SERVER;

    static {
        ForgeConfigBuilder server = new ForgeConfigBuilder();
        ServerConfig.define(server);
        SERVER = server.build();
    }

    private ForgeConfigs() {}
}
