package de.mccs.core.staff;

/** The energy arithmetic of the staff; the stored value itself lives on the item stack. */
public final class StaffEnergy {
    /** Saved value of a stack that was never charged or used. */
    public static final int UNSET = -1;

    private static final int BAR_WIDTH = 13;
    private static final double LOOK_FULL = 0.6;
    private static final double LOOK_MEDIUM = 0.3;
    private static final double LOOK_DIM = 0.1;

    private StaffEnergy() {}

    /** How the crystal looks at a charge: 0 bright, 1 medium, 2 dim, 3 red. */
    public static int crystalLook(int stored, int capacity) {
        double share = stored / (double) Math.max(1, capacity);
        if (share >= LOOK_FULL) return 0;
        if (share >= LOOK_MEDIUM) return 1;
        return share >= LOOK_DIM ? 2 : 3;
    }

    /** Width of the energy bar on the item, in pixels. */
    public static int barWidth(int stored, int capacity) {
        return (int) Math.round(BAR_WIDTH * Math.min(1.0, stored / (double) Math.max(1, capacity)));
    }

    /** The energy in a stack: its saved value within the capacity, or the start charge if it has none. */
    public static int stored(int saved, int capacity, double startCharge) {
        if (saved == UNSET) return (int) Math.round(capacity * startCharge);
        return Math.max(0, Math.min(saved, capacity));
    }

    /** How much of an offered amount a stack takes. */
    public static int accepted(int stored, int capacity, int maxReceive, long offered) {
        return (int) Math.max(0, Math.min(Math.min(offered, maxReceive), (long) capacity - stored));
    }

    /** Energy for absorbing a hit, rounded up to whole FE. */
    public static int hitCost(float damage, int costPerPoint) {
        double cost = Math.ceil((double) damage * costPerPoint);
        return (int) Math.max(0, Math.min(cost, Integer.MAX_VALUE));
    }
}
