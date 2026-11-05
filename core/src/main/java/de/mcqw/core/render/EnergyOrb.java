package de.mcqw.core.render;

/** How the energy ball is drawn: a bright core inside a wobbling shell, both in the look of the shield bubble. */
public final class EnergyOrb {
    public static final EnergyBubble.Shape SHELL = EnergyBubble.Shape.sphere(0.5f);
    public static final EnergyBubble.Shape CORE = EnergyBubble.Shape.sphere(0.24f);
    public static final float SHELL_INTENSITY = 1.2f;
    public static final float CORE_INTENSITY = 5f;
    public static final float WOBBLE = 0.06f;

    private EnergyOrb() {}
}
