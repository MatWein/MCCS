package de.mccs.core.config;

/** The staff's teleport. */
public final class TeleportConfig {
    private final IntSetting range;
    private final IntSetting energy;
    private final IntSetting cooldownTicks;

    TeleportConfig(ConfigBuilder builder) {
        builder.comment("Teleport of the crystal staff").push("teleport");
        range = builder.comment("Reach in blocks")
                .defineInRange("range", 30, 1, 128);
        energy = builder.comment("Energy per teleport (FE)")
                .defineInRange("energy", 20_000, 0, Integer.MAX_VALUE);
        cooldownTicks = builder.comment("Time between two teleports (ticks)")
                .defineInRange("cooldownTicks", 60, 0, 1200);
        builder.pop();
    }

    public int range() {
        return range.get();
    }

    public int energy() {
        return energy.get();
    }

    public int cooldownTicks() {
        return cooldownTicks.get();
    }
}
