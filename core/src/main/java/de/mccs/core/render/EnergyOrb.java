package de.mccs.core.render;

/** How the energy ball is drawn: a bright core inside a wobbling shell, both in the look of the shield bubble. */
public final class EnergyOrb {
    public static final EnergyBubble.Shape SHELL = EnergyBubble.Shape.sphere(0.5f);
    public static final EnergyBubble.Shape CORE = EnergyBubble.Shape.sphere(0.24f);
    public static final float SHELL_INTENSITY = 1.2f;
    public static final float CORE_INTENSITY = 5f;
    public static final float WOBBLE = 0.06f;

    private static final float[] BLUE = {0.5f, 0.78f, 1f};
    /** A fully charged ball looks clearly different from a normal one. */
    private static final float[] YELLOW = {1f, 0.8f, 0.3f};
    private static final float FULL = 0.999f;
    /** While charging at the staff it grows from this size to that of the charged ball. */
    private static final float PREVIEW_START_SIZE = 0.35f;

    private EnergyOrb() {}

    public static float[] color(float charge) {
        return charge >= FULL ? YELLOW : BLUE;
    }

    /** Size while charging at the staff; {@code fullSize} is that of a fully charged ball. */
    public static float previewSize(float charge, float fullSize) {
        return PREVIEW_START_SIZE + (fullSize - PREVIEW_START_SIZE) * charge;
    }
}
