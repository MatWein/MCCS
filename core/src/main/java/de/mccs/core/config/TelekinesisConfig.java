package de.mccs.core.config;

public final class TelekinesisConfig {
    private final IntSetting range;
    private final DoubleSetting aimTolerance;
    private final DoubleSetting holdDistance;
    private final DoubleSetting maxSize;
    private final BooleanSetting liftPlayers;
    private final IntSetting drain;
    private final IntSetting throwEnergy;
    private final DoubleSetting throwSpeed;
    private final DoubleSetting impactDamage;
    private final DoubleSetting maxImpactDamage;

    TelekinesisConfig(ConfigBuilder builder) {
        builder.comment("Telekinesis of the crystal staff").push("telekinesis");
        range = builder.comment("Reach in blocks")
                .defineInRange("range", 24, 1, 128);
        aimTolerance = builder.comment("How close to the crosshair a target counts as aimed at (blocks)")
                .defineInRange("aimTolerance", 0.7, 0.0, 4.0);
        holdDistance = builder.comment("How far in front of the player a lifted target floats (blocks)")
                .defineInRange("holdDistance", 5.0, 2.0, 32.0);
        maxSize = builder.comment("Largest target that can be lifted: width and height (blocks)")
                .defineInRange("maxSize", 4.0, 0.5, 64.0);
        liftPlayers = builder.comment("Whether other players can be lifted")
                .define("liftPlayers", false);
        drain = builder.comment("Energy while holding a target (FE/tick)")
                .defineInRange("drain", 100, 0, Integer.MAX_VALUE);
        throwEnergy = builder.comment("Energy per throw (FE)")
                .defineInRange("throwEnergy", 5_000, 0, Integer.MAX_VALUE);
        throwSpeed = builder.comment("Speed of a throw (blocks/tick)")
                .defineInRange("throwSpeed", 2.5, 0.1, 10.0);
        impactDamage = builder.comment("Damage when a thrown target hits something, per block/tick of its speed")
                .defineInRange("impactDamage", 6.0, 0.0, 1000.0);
        maxImpactDamage = builder.comment("Highest damage of an impact")
                .defineInRange("maxImpactDamage", 20.0, 0.0, 1000.0);
        builder.pop();
    }

    public int range() {
        return range.get();
    }

    public double aimTolerance() {
        return aimTolerance.get();
    }

    public double holdDistance() {
        return holdDistance.get();
    }

    public double maxSize() {
        return maxSize.get();
    }

    public boolean liftPlayers() {
        return liftPlayers.get();
    }

    public int drain() {
        return drain.get();
    }

    public int throwEnergy() {
        return throwEnergy.get();
    }

    public double throwSpeed() {
        return throwSpeed.get();
    }

    public double impactDamage() {
        return impactDamage.get();
    }

    public double maxImpactDamage() {
        return maxImpactDamage.get();
    }
}
