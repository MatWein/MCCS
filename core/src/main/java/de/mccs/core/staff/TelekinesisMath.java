package de.mccs.core.staff;

/** The arithmetic of the telekinesis. */
public final class TelekinesisMath {
    /** The share of the way to its place a held target covers per tick. */
    private static final double PULL = 0.35;
    private static final double MAX_PULL_SPEED = 1.6;

    private TelekinesisMath() {}

    /** The speed along one axis, towards its place. Together with the other axes it is limited in {@link #limit}. */
    public static double pull(double offset) {
        return offset * PULL;
    }

    /** Scales a pull down if it would be too fast: returns the factor for all three axes. */
    public static double limit(double x, double y, double z) {
        double speed = Math.sqrt(x * x + y * y + z * z);
        return speed > MAX_PULL_SPEED ? MAX_PULL_SPEED / speed : 1.0;
    }

    /** Damage of a thrown target hitting something at this speed; slow bumps do nothing. */
    public static float impact(double speed, double minSpeed, double perSpeed, double max) {
        if (speed < minSpeed) return 0f;
        return (float) Math.min(max, speed * perSpeed);
    }
}
