package de.mcqw.core.staff;

/** The arithmetic of the energy ball. */
public final class BallMath {
    private BallMath() {}

    /** Share of the center's strength at a distance: falls linearly to {@code edge} at the radius, 0 outside of it. */
    public static double blastShare(double distance, double radius, double edge) {
        if (radius <= 0 || distance >= radius) return 0;
        return 1 + (edge - 1) * Math.max(0, distance) / radius;
    }

    /** Ticks until the ball has flown its range. */
    public static int lifetime(double range, double speed) {
        return (int) Math.ceil(range / speed);
    }
}
