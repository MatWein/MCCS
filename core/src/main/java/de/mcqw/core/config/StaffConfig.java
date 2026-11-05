package de.mcqw.core.config;

/** Energy settings of the crystal staff. */
public final class StaffConfig {
    private static final int MAX_FE = Integer.MAX_VALUE;

    private final BooleanSetting consumeEnergy;
    private final IntSetting capacity;
    private final DoubleSetting startCharge;
    private final IntSetting maxReceive;
    private final IntSetting shieldDrain;
    private final IntSetting shieldHitCost;

    StaffConfig(ConfigBuilder builder) {
        builder.comment("Crystal staff").push("staff");
        consumeEnergy = builder.comment("Whether the shield uses energy; if false it runs for free, even when the staff is empty")
                .define("consumeEnergy", true);
        capacity = builder.comment("Energy storage (FE)")
                .defineInRange("capacity", 10_000_000, 1, MAX_FE);
        startCharge = builder.comment("Charge of a new staff (share of the capacity)")
                .defineInRange("startCharge", 0.2, 0.0, 1.0);
        maxReceive = builder.comment("Fastest charging from other mods (FE/tick)")
                .defineInRange("maxReceive", 100_000, 0, MAX_FE);
        shieldDrain = builder.comment("Shield drain while active (FE/tick)")
                .defineInRange("shieldDrain", 500, 0, MAX_FE);
        shieldHitCost = builder.comment("Extra energy per point of absorbed damage (FE)")
                .defineInRange("shieldHitCost", 200, 0, MAX_FE);
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

    public int shieldDrain() {
        return shieldDrain.get();
    }

    public int shieldHitCost() {
        return shieldHitCost.get();
    }
}
