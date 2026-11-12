package de.mccs.core.staff;

/** The arithmetic of the energy ball. */
public final class BallMath {
    private BallMath() {}

    /** Share of the center's strength at a distance: falls linearly to {@code edge} at the radius, 0 outside of it. */
    public static double blastShare(double distance, double radius, double edge) {
        if (radius <= 0 || distance >= radius) return 0;
        return 1 + (edge - 1) * Math.max(0, distance) / radius;
    }

    /** Charge from 0 to 1 after holding the use key for the given ticks. */
    public static double chargeShare(int usedTicks, int chargeTicks) {
        return Math.max(0.0, Math.min(1.0, usedTicks / (double) Math.max(1, chargeTicks)));
    }

    /** A value of the uncharged ball with the multiplier for full charge applied by share. */
    public static double charged(double base, double fullFactor, double share) {
        return base * (1 + (fullFactor - 1) * share);
    }

    /** The highest charge affordable with the stored energy, at most the wanted one; negative if not even a plain shot is. */
    public static double affordableShare(double wanted, long stored, long baseEnergy, double fullFactor) {
        if (stored < baseEnergy) return -1;
        if (baseEnergy <= 0 || fullFactor <= 1) return wanted;
        return Math.min(wanted, (stored - baseEnergy) / (baseEnergy * (fullFactor - 1)));
    }

    /** Ticks until the ball has flown its range. */
    public static int lifetime(double range, double speed) {
        return (int) Math.ceil(range / speed);
    }
}
