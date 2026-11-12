package de.mccs.fabric.config;

import de.mccs.core.config.ServerConfig;
import net.fabricmc.loader.api.FabricLoader;

/** The server settings as a file in Fabric's config folder (mccs-server.toml). */
public final class FabricConfigs {
    public static final TomlConfig SERVER = new TomlConfig(FabricLoader.getInstance().getConfigDir().resolve("mccs-server.toml"));

    private FabricConfigs() {}

    public static void define() {
        ServerConfig.define(SERVER);
        SERVER.load();
    }
}
