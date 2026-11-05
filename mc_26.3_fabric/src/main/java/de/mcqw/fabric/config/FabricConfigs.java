package de.mcqw.fabric.config;

import de.mcqw.core.config.ServerConfig;
import net.fabricmc.loader.api.FabricLoader;

/** The server settings as a file in Fabric's config folder (mcqw-server.toml). */
public final class FabricConfigs {
    public static final TomlConfig SERVER = new TomlConfig(FabricLoader.getInstance().getConfigDir().resolve("mcqw-server.toml"));

    private FabricConfigs() {}

    public static void define() {
        ServerConfig.define(SERVER);
        SERVER.load();
    }
}
