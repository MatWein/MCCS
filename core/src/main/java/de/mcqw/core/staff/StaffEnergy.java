package de.mcqw.core.staff;

/** The energy arithmetic of the staff; the stored value itself lives on the item stack. */
public final class StaffEnergy {
    /** Saved value of a stack that was never charged or used. */
    public static final int UNSET = -1;

    private StaffEnergy() {}

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
