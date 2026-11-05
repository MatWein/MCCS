package de.mcqw.core.config;

/** Energy settings of the crystal staff. */
public final class StaffConfig {
    private static final int MAX_FE = Integer.MAX_VALUE;

    private final BooleanSetting consumeEnergy;
    private final IntSetting capacity;
    private final DoubleSetting startCharge;
    private final IntSetting maxReceive;
    private final IntSetting shieldDrain;
    private final DoubleSetting shieldWaterFactor;
    private final DoubleSetting shieldLavaFactor;
    private final IntSetting shieldHitCost;
    private final IntSetting cloakDrain;
    private final DoubleSetting cloakWaterFactor;
    private final DoubleSetting cloakLavaFactor;
    private final DoubleSetting underFireFactor;
    private final DoubleSetting underFireSeconds;

    StaffConfig(ConfigBuilder builder) {
        builder.comment("Crystal staff").push("staff");
        consumeEnergy = builder.comment("Whether shield and cloak use energy; if false they run for free, even when the staff is empty")
                .define("consumeEnergy", true);
        capacity = builder.comment("Energy storage (FE)")
                .defineInRange("capacity", 5_000_000, 1, MAX_FE);
        startCharge = builder.comment("Charge of a new staff (share of the capacity)")
                .defineInRange("startCharge", 0.2, 0.0, 1.0);
        maxReceive = builder.comment("Fastest charging from other mods (FE/tick)")
                .defineInRange("maxReceive", 10_000, 0, MAX_FE);
        shieldDrain = builder.comment("Shield drain while active (FE/tick)")
                .defineInRange("shieldDrain", 50, 0, MAX_FE);
        shieldWaterFactor = builder.comment("Shield drain multiplier when under water")
                .defineInRange("shieldWaterFactor", 2.0, 1.0, 100.0);
        shieldLavaFactor = builder.comment("Shield drain multiplier when touching lava")
                .defineInRange("shieldLavaFactor", 3.0, 1.0, 100.0);
        shieldHitCost = builder.comment("Extra energy per point of absorbed damage (FE)")
                .defineInRange("shieldHitCost", 2_000, 0, MAX_FE);
        cloakDrain = builder.comment("Cloak drain while active (FE/tick)")
                .defineInRange("cloakDrain", 40, 0, MAX_FE);
        cloakWaterFactor = builder.comment("Cloak drain multiplier when under water")
                .defineInRange("cloakWaterFactor", 1.5, 1.0, 100.0);
        cloakLavaFactor = builder.comment("Cloak drain multiplier when touching lava")
                .defineInRange("cloakLavaFactor", 2.0, 1.0, 100.0);
        underFireFactor = builder.comment("Shield and cloak drain multiplier while under fire")
                .defineInRange("underFireFactor", 2.0, 1.0, 100.0);
        underFireSeconds = builder.comment("How long the player counts as under fire after a hit (seconds)")
                .defineInRange("underFireSeconds", 2.0, 0.0, 60.0);
        builder.pop();
    }

    public boolean consumeEnergy() {
        return consumeEnergy.get();
    }

    public int capacity() {
        return capacity.get();
    }

    public double startCharge() {
        return startCharge.get();
    }

    public int maxReceive() {
        return maxReceive.get();
    }

    /** The shield's drain in FE/tick where the player is. */
    public int shieldDrain(boolean underWater, boolean inLava, boolean underFire) {
        return drain(shieldDrain.get(), inLava ? shieldLavaFactor.get() : underWater ? shieldWaterFactor.get() : 1.0, underFire);
    }

    public int shieldHitCost() {
        return shieldHitCost.get();
    }

    /** The cloak's drain in FE/tick where the player is. */
    public int cloakDrain(boolean underWater, boolean inLava, boolean underFire) {
        return drain(cloakDrain.get(), inLava ? cloakLavaFactor.get() : underWater ? cloakWaterFactor.get() : 1.0, underFire);
    }

    private int drain(int base, double factor, boolean underFire) {
        return (int) Math.min(Integer.MAX_VALUE, Math.round(base * factor * (underFire ? underFireFactor.get() : 1.0)));
    }

    public int underFireTicks() {
        return (int) Math.round(underFireSeconds.get() * 20);
    }
}
