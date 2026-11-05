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
}
