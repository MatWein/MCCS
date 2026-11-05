package de.mcqw.core.config;

/** The staff's energy ball. */
public final class BallConfig {
    private final DoubleSetting damage;
    private final DoubleSetting radius;
    private final DoubleSetting edgeDamage;
    private final DoubleSetting knockback;
    private final DoubleSetting speed;
    private final IntSetting range;
    private final IntSetting energy;
    private final IntSetting cooldownTicks;
    private final IntSetting chargeTicks;
    private final DoubleSetting blockDropChance;
    private final DoubleSetting maxBlockResistance;
    private final DoubleSetting chargedDamageFactor;
    private final DoubleSetting chargedRadiusFactor;
    private final DoubleSetting chargedSizeFactor;
    private final DoubleSetting chargedEnergyFactor;

    BallConfig(ConfigBuilder builder) {
        builder.comment("Energy ball of the crystal staff").push("energyBall");
        damage = builder.comment("Damage at the center of the explosion")
                .defineInRange("damage", 40.0, 0.0, 1000.0);
        radius = builder.comment("Radius of the explosion (blocks); it never breaks blocks")
                .defineInRange("radius", 4.0, 0.0, 32.0);
        edgeDamage = builder.comment("Share of the damage at the edge of the explosion")
                .defineInRange("edgeDamage", 0.25, 0.0, 1.0);
        knockback = builder.comment("Knockback at the center of the explosion")
                .defineInRange("knockback", 1.2, 0.0, 10.0);
        speed = builder.comment("Flight speed (blocks/tick)")
                .defineInRange("speed", 0.8, 0.05, 8.0);
        range = builder.comment("How far it flies (blocks)")
                .defineInRange("range", 128, 1, 512);
        energy = builder.comment("Energy per shot (FE)")
                .defineInRange("energy", 30_000, 0, Integer.MAX_VALUE);
        cooldownTicks = builder.comment("Time between two shots (ticks)")
                .defineInRange("cooldownTicks", 40, 0, 1200);
        blockDropChance = builder.comment("With block damage on: chance of a destroyed block to drop as an item")
                .defineInRange("blockDropChance", 0.3, 0.0, 1.0);
        maxBlockResistance = builder.comment("With block damage on: blocks with a higher blast resistance survive (obsidian: 1200)")
                .defineInRange("maxBlockResistance", 100.0, 0.0, 10000.0);
        chargeTicks = builder.comment("Holding the use key this long charges a ball fully (ticks)")
                .defineInRange("chargeTicks", 40, 1, 1200);
        chargedDamageFactor = builder.comment("Damage multiplier of a fully charged ball")
                .defineInRange("chargedDamageFactor", 3.0, 1.0, 100.0);
        chargedRadiusFactor = builder.comment("Explosion radius multiplier of a fully charged ball")
                .defineInRange("chargedRadiusFactor", 1.5, 1.0, 10.0);
        chargedSizeFactor = builder.comment("Size multiplier of a fully charged ball")
                .defineInRange("chargedSizeFactor", 2.0, 1.0, 10.0);
        chargedEnergyFactor = builder.comment("Energy multiplier of a fully charged ball")
                .defineInRange("chargedEnergyFactor", 3.0, 1.0, 100.0);
        builder.pop();
    }

    public double damage() {
        return damage.get();
    }

    public double radius() {
        return radius.get();
    }

    public double edgeDamage() {
        return edgeDamage.get();
    }

    public double knockback() {
        return knockback.get();
    }

    public double speed() {
        return speed.get();
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

    public double blockDropChance() {
        return blockDropChance.get();
    }

    public double maxBlockResistance() {
        return maxBlockResistance.get();
    }

    public int chargeTicks() {
        return chargeTicks.get();
    }

    public double chargedDamageFactor() {
        return chargedDamageFactor.get();
    }

    public double chargedRadiusFactor() {
        return chargedRadiusFactor.get();
    }

    public double chargedSizeFactor() {
        return chargedSizeFactor.get();
    }

    public double chargedEnergyFactor() {
        return chargedEnergyFactor.get();
    }
}
