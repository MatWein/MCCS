package de.mcqw.core.config;

/**
 * All server settings (synced to clients). The platform module calls {@link #define} once while building its
 * config; afterwards the sections are available as static fields.
 */
public final class ServerConfig {
    public static StaffConfig STAFF;
    public static BeamConfig BEAM;
    public static BallConfig BALL;
    public static TeleportConfig TELEPORT;
    public static TelekinesisConfig TELEKINESIS;

    private ServerConfig() {}

    public static void define(ConfigBuilder builder) {
        STAFF = new StaffConfig(builder);
        BEAM = new BeamConfig(builder);
        BALL = new BallConfig(builder);
        TELEPORT = new TeleportConfig(builder);
        TELEKINESIS = new TelekinesisConfig(builder);
    }
}
