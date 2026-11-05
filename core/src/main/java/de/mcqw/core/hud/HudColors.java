package de.mcqw.core.hud;

/** Colors of the staff's HUD (RGB, ARGB where noted), the same as MCPJ's cockpit HUD. */
public final class HudColors {
    public static final int TEAL = 0x7FE6F2;
    public static final int GOLD = 0xFFC94A;
    public static final int GREY = 0x9AA8AD;
    public static final int RED = 0xFF5040;
    public static final int DARK_RED = 0x803020;
    /** ARGB */
    public static final int PANEL_BACKGROUND = 0x80102028;
    /** ARGB */
    public static final int PANEL_ACCENT = 0xFF000000 | TEAL;

    private static final int BLINK_HALF_PERIOD = 10;

    private HudColors() {}

    public static int blink(int tickCount, int on, int off) {
        return (tickCount / BLINK_HALF_PERIOD) % 2 == 0 ? on : off;
    }
}
