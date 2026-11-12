package de.mccs.core.staff;

import de.mccs.core.MccsIds;

/**
 * The kinds of crystal staff. They all work the same way and differ only in how much energy they store (a multiple of the
 * configured capacity) and in the colors of their crystal: its light (also the bright top of the crystal) and the deep
 * color at its bottom.
 */
public enum StaffKind {
    NORMAL(MccsIds.CRYSTAL_STAFF, 1, new float[]{0.3f, 0.6f, 1.0f}, new float[]{0.08f, 0.3f, 0.95f}),
    HIGH_ENERGY(MccsIds.HIGH_ENERGY_CRYSTAL_STAFF, 10, new float[]{1.0f, 0.8f, 0.3f}, new float[]{0.95f, 0.35f, 0.03f});

    private final String id;
    private final int capacityFactor;
    private final float[] glow;
    private final float[] deep;

    StaffKind(String id, int capacityFactor, float[] glow, float[] deep) {
        this.id = id;
        this.capacityFactor = capacityFactor;
        this.glow = glow;
        this.deep = deep;
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
        return glow[0];
    }

    public float glowGreen() {
        return glow[1];
    }

    public float glowBlue() {
        return glow[2];
    }

    /** The crystal's deep color at the given channel (0 red, 1 green, 2 blue). */
    public float deep(int channel) {
        return deep[channel];
    }

    /** The light's color at the given channel (0 red, 1 green, 2 blue). */
    public float glow(int channel) {
        return glow[channel];
    }
}
