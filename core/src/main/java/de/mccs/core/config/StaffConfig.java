package de.mccs.core.config;

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
    private final DoubleSetting lightningCharge;
    private final DoubleSetting lightningAttractRange;
    private final IntSetting lightDrain;
    private final IntSetting lightLevel;
    private final IntSetting cloakDrain;
    private final DoubleSetting cloakWaterFactor;
    private final DoubleSetting cloakLavaFactor;
    private final DoubleSetting underFireFactor;
    private final DoubleSetting shieldUnderFireFactor;
    private final DoubleSetting underFireSeconds;
    private final IntSetting selfCharge;

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
                .defineInRange("shieldHitCost", 4_000, 0, MAX_FE);
        lightningCharge = builder.comment("Charge from being struck by lightning while holding the staff (share of the capacity)")
                .defineInRange("lightningCharge", 0.2, 0.0, 1.0);
        lightningAttractRange = builder.comment("Natural lightning within this range strikes a player with an active shield under open sky (blocks, 0 = never)")
                .defineInRange("lightningAttractRange", 32.0, 0.0, 128.0);
        lightDrain = builder.comment("Light drain while active (FE/tick)")
                .defineInRange("lightDrain", 2, 0, MAX_FE);
        lightLevel = builder.comment("Brightness of the light")
                .defineInRange("lightLevel", 15, 1, 15);
        cloakDrain = builder.comment("Cloak drain while active (FE/tick)")
                .defineInRange("cloakDrain", 40, 0, MAX_FE);
        cloakWaterFactor = builder.comment("Cloak drain multiplier when under water")
                .defineInRange("cloakWaterFactor", 1.5, 1.0, 100.0);
        cloakLavaFactor = builder.comment("Cloak drain multiplier when touching lava")
                .defineInRange("cloakLavaFactor", 2.0, 1.0, 100.0);
        underFireFactor = builder.comment("Cloak drain multiplier while under fire")
                .defineInRange("underFireFactor", 2.0, 1.0, 100.0);
        shieldUnderFireFactor = builder.comment("Shield drain multiplier while under fire: holding off attacks costs much more than standing by")
                .defineInRange("shieldUnderFireFactor", 6.0, 1.0, 100.0);
        underFireSeconds = builder.comment("How long the player counts as under fire after a hit (seconds)")
                .defineInRange("underFireSeconds", 2.0, 0.0, 60.0);
        selfCharge = builder.comment("Energy a staff in a player's inventory gathers on its own (FE/tick, 0 = off), so it can be used without other mods")
                .defineInRange("selfCharge", 250, 0, MAX_FE);
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
        return drain(shieldDrain.get(), inLava ? shieldLavaFactor.get() : underWater ? shieldWaterFactor.get() : 1.0, underFire ? shieldUnderFireFactor.get() : 1.0);
    }

    public int lightDrain() {
        return lightDrain.get();
    }

    public int lightLevel() {
        return lightLevel.get();
    }

    public double lightningCharge() {
        return lightningCharge.get();
    }

    public double lightningAttractRange() {
        return lightningAttractRange.get();
    }

    public int shieldHitCost() {
        return shieldHitCost.get();
    }

    /** The cloak's drain in FE/tick where the player is. */
    public int cloakDrain(boolean underWater, boolean inLava, boolean underFire) {
        return drain(cloakDrain.get(), inLava ? cloakLavaFactor.get() : underWater ? cloakWaterFactor.get() : 1.0, underFire ? underFireFactor.get() : 1.0);
    }

    private int drain(int base, double factor, double underFireFactor) {
        return (int) Math.min(Integer.MAX_VALUE, Math.round(base * factor * underFireFactor));
    }

    /** What a staff in a player's inventory gathers on its own, in FE/tick. */
    public int selfCharge() {
        return selfCharge.get();
    }

    public int underFireTicks() {
        return (int) Math.round(underFireSeconds.get() * 20);
    }
}
