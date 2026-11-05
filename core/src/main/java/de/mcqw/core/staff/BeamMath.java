package de.mcqw.core.staff;

/** The arithmetic of the lightning attack. */
public final class BeamMath {
    private BeamMath() {}

    /** Ticks between two hits. */
    public static int hitInterval(int hitsPerSecond) {
        return Math.max(1, 20 / Math.max(1, hitsPerSecond));
    }

    /** Damage multiplier after staying on the same target for the given ticks: rises from start to max. */
    public static double chargeFactor(int ticksOnTarget, double chargeSeconds, double start, double max) {
        double chargeTicks = chargeSeconds * 20;
        if (chargeTicks <= 0) return max;
        double share = Math.min(1.0, ticksOnTarget / chargeTicks);
        return start + (max - start) * share;
    }

    /**
     * Damage of one hit.
     *
     * @param chainIndex 0 for the main target, 1 for the first jump, ...
     */
    public static float hitDamage(double damagePerSecond, int interval, double charge, int chainIndex, double falloff, double wetFactor) {
        return (float) (damagePerSecond * interval / 20.0 * charge * Math.pow(falloff, chainIndex) * wetFactor);
    }
}
