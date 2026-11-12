package de.mccs.core.staff;

import de.mccs.core.MccsIds;

/**
 * The kinds of crystal staff. They all work the same way and differ only in how much energy they store (a multiple of the
 * configured capacity) and in the color of their crystal's light.
 */
public enum StaffKind {
    NORMAL(MccsIds.CRYSTAL_STAFF, 1, 0.3f, 0.6f, 1.0f),
    HIGH_ENERGY(MccsIds.HIGH_ENERGY_CRYSTAL_STAFF, 10, 1.0f, 0.8f, 0.3f);

    private final String id;
    private final int capacityFactor;
    private final float glowRed;
    private final float glowGreen;
    private final float glowBlue;

    StaffKind(String id, int capacityFactor, float glowRed, float glowGreen, float glowBlue) {
        this.id = id;
        this.capacityFactor = capacityFactor;
        this.glowRed = glowRed;
        this.glowGreen = glowGreen;
        this.glowBlue = glowBlue;
    }

    /** The item's registry name. */
    public String id() {
        return id;
    }

    /** The energy storage of this kind, given the configured capacity of a normal staff. */
    public int capacity(int baseCapacity) {
        return (int) Math.min(Integer.MAX_VALUE, (long) baseCapacity * capacityFactor);
    }

    public float glowRed() {
        return glowRed;
    }

    public float glowGreen() {
        return glowGreen;
    }

    public float glowBlue() {
        return glowBlue;
    }
}
