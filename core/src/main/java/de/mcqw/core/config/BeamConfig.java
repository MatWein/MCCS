package de.mcqw.core.config;

/** The staff's lightning attack. */
public final class BeamConfig {
    private final DoubleSetting damagePerSecond;
    private final IntSetting hitsPerSecond;
    private final IntSetting range;
    private final DoubleSetting aimTolerance;
    private final IntSetting drain;
    private final IntSetting chainTargets;
    private final DoubleSetting chainRange;
    private final DoubleSetting chainFalloff;
    private final DoubleSetting chargeSeconds;
    private final DoubleSetting chargeStart;
    private final DoubleSetting chargeMax;
    private final DoubleSetting wetDamageFactor;
    private final DoubleSetting wetChainRangeFactor;
    private final IntSetting slowTicks;
    private final IntSetting slowLevel;
    private final DoubleSetting fireSeconds;

    BeamConfig(ConfigBuilder builder) {
        builder.comment("Lightning attack of the crystal staff").push("beam");
        damagePerSecond = builder.comment("Damage per second on the main target, before charging")
                .defineInRange("damagePerSecond", 16.0, 0.0, 1000.0);
        hitsPerSecond = builder.comment("Hits per second the damage is split into")
                .defineInRange("hitsPerSecond", 4, 1, 20);
        range = builder.comment("Reach in blocks")
                .defineInRange("range", 24, 1, 128);
        aimTolerance = builder.comment("How close to the crosshair a target counts as aimed at (blocks)")
                .defineInRange("aimTolerance", 0.5, 0.0, 4.0);
        drain = builder.comment("Energy while firing (FE/tick)")
                .defineInRange("drain", 500, 0, Integer.MAX_VALUE);
        chainTargets = builder.comment("Further hostile mobs the lightning jumps to")
                .defineInRange("chainTargets", 3, 0, 16);
        chainRange = builder.comment("Reach of each jump (blocks)")
                .defineInRange("chainRange", 6.0, 0.0, 32.0);
        chainFalloff = builder.comment("Damage share each jump keeps")
                .defineInRange("chainFalloff", 0.5, 0.0, 1.0);
        chargeSeconds = builder.comment("Seconds on the same target until the full charge")
                .defineInRange("chargeSeconds", 3.0, 0.0, 60.0);
        chargeStart = builder.comment("Damage multiplier when the lightning reaches a new target")
                .defineInRange("chargeStart", 0.5, 0.0, 10.0);
        chargeMax = builder.comment("Damage multiplier at full charge")
                .defineInRange("chargeMax", 1.5, 0.0, 10.0);
        wetDamageFactor = builder.comment("Damage multiplier on targets in water or rain")
                .defineInRange("wetDamageFactor", 1.5, 0.0, 10.0);
        wetChainRangeFactor = builder.comment("Multiplier for the jump reach from targets in water or rain")
                .defineInRange("wetChainRangeFactor", 1.5, 1.0, 10.0);
        slowTicks = builder.comment("How long a hit keeps a mob slowed (ticks, 0 = never)")
                .defineInRange("slowTicks", 10, 0, 200);
        slowLevel = builder.comment("Strength of the slowness (1 = Slowness I)")
                .defineInRange("slowLevel", 4, 1, 10);
        fireSeconds = builder.comment("Seconds a hit sets a target on fire, unless it is wet (0 = never); blocks never burn")
                .defineInRange("fireSeconds", 3.0, 0.0, 60.0);
        builder.pop();
    }

    public double damagePerSecond() {
        return damagePerSecond.get();
    }

    public int hitsPerSecond() {
        return hitsPerSecond.get();
    }

    public int range() {
        return range.get();
    }

    public double aimTolerance() {
        return aimTolerance.get();
    }

    public int drain() {
        return drain.get();
    }

    public int chainTargets() {
        return chainTargets.get();
    }

    public double chainRange() {
        return chainRange.get();
    }

    public double chainFalloff() {
        return chainFalloff.get();
    }

    public double chargeSeconds() {
        return chargeSeconds.get();
    }

    public double chargeStart() {
        return chargeStart.get();
    }

    public double chargeMax() {
        return chargeMax.get();
    }

    public double wetDamageFactor() {
        return wetDamageFactor.get();
    }

    public double wetChainRangeFactor() {
        return wetChainRangeFactor.get();
    }

    public int slowTicks() {
        return slowTicks.get();
    }

    public int slowLevel() {
        return slowLevel.get();
    }

    public double fireSeconds() {
        return fireSeconds.get();
    }
}
