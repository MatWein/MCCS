package de.mcqw.core.hud;

import java.util.ArrayList;
import java.util.List;

/** What the staff's HUD shows (lines, values, warnings, colors); the platform only draws it. */
public final class StaffHudModel {
    private static final float ENERGY_GOOD = 0.3f;
    private static final float ENERGY_LOW = 0.1f;
    private static final int PERCENT = 100;
    private static final String KEY = "hud.mcqw.";

    /** {@code drain}: current use in FE/tick, 0 if the staff is not using energy. */
    public record State(int stored, int capacity, boolean shieldOn, boolean cloakOn, boolean lightsOn, boolean blockDamage, int drain, boolean consumesEnergy) {}

    private StaffHudModel() {}

    public static List<HudLine> lines(State state, int tickCount) {
        List<HudLine> lines = new ArrayList<>();
        lines.add(HudLine.of(text("title"), HudColors.GOLD));
        lines.add(energyLine(state, tickCount));
        HudText cloak = text(state.cloakOn() ? "cloak.on" : "cloak.off");
        lines.add(HudLine.of(text("cloak", cloak), state.cloakOn() ? HudColors.GOLD : HudColors.GREY));
        HudText shield = text(state.shieldOn() ? "shield.on" : "shield.off");
        lines.add(HudLine.of(text("shield", shield), state.shieldOn() ? HudColors.TEAL : HudColors.GREY));
        lines.add(HudLine.of(text("lights", text(state.lightsOn() ? "lights.on" : "lights.off")), state.lightsOn() ? HudColors.GOLD : HudColors.GREY));
        lines.add(HudLine.of(text("block_damage", text(state.blockDamage() ? "block_damage.on" : "block_damage.off")), state.blockDamage() ? HudColors.RED : HudColors.GREY));
        if (state.drain() > 0) lines.add(drainLine(state));
        return lines;
    }

    private static HudLine energyLine(State state, int tickCount) {
        float fraction = state.stored() / (float) Math.max(1, state.capacity());
        HudText energy = text("energy", HudText.literal((int) Math.floor(fraction * PERCENT)));
        return HudLine.of(energy, state.consumesEnergy() ? energyColor(fraction, tickCount) : HudColors.TEAL);
    }

    /** On its own line to keep the panel narrow: it must not reach the hotbar in small windows. */
    private static HudLine drainLine(State state) {
        return HudLine.of(HudText.literal("-" + HudFormat.compact(state.drain()) + " FE/t · ~"
                + HudFormat.duration(state.stored() / state.drain())), HudColors.TEAL);
    }

    private static int energyColor(float fraction, int tickCount) {
        if (fraction > ENERGY_GOOD) return HudColors.TEAL;
        if (fraction > ENERGY_LOW) return HudColors.GOLD;
        return HudColors.blink(tickCount, HudColors.RED, HudColors.DARK_RED);
    }

    private static HudText text(String key, HudText... args) {
        return HudText.translated(KEY + key, args);
    }
}
