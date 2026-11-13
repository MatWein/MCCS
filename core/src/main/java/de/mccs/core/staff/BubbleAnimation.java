package de.mccs.core.staff;

/** The shield and cloak fades and the hit flash for one player. */
public final class BubbleAnimation {
    private static final float SHIELD_FADE_SPEED = 0.06f;
    private static final float CLOAK_FADE_SPEED = 0.05f;
    private static final int FLASH_TICKS = 10;
    private static final float CLOAKED_THRESHOLD = 0.5f;

    /** Ticks after which a cloak hides the player: when {@link #isHidden} turns true. */
    public static final int CLOAK_HIDE_TICKS = 10;

    private float shield;
    private float shieldOld;
    private float cloak;
    private float cloakOld;
    private int flash;
    private int lastHits = -1;
    /** Ticks since the shield last took a hit (large if never). */
    private int sinceHit = Integer.MAX_VALUE;

    /** Once per client tick; {@code hits} is the hit counter of the staff with the active shield. */
    public void tick(boolean shieldOn, int hits, boolean cloakOn) {
        shieldOld = shield;
        shield = approach(shield, shieldOn ? 1f : 0f, SHIELD_FADE_SPEED);
        cloakOld = cloak;
        cloak = approach(cloak, cloakOn ? 1f : 0f, CLOAK_FADE_SPEED);
        if (flash > 0) flash--;
        if (sinceHit < Integer.MAX_VALUE) sinceHit++;
        if (shieldOn) {
            if (lastHits >= 0 && hits > lastHits) {
                flash = FLASH_TICKS;
                sinceHit = 0;
            }
            lastHits = hits;
        } else {
            lastHits = -1;
        }
    }

    private static float approach(float value, float target, float speed) {
        return value < target ? Math.min(target, value + speed) : Math.max(target, value - speed);
    }

    public float shield(float partialTick) {
        return shieldOld + (shield - shieldOld) * partialTick;
    }

    public float cloak(float partialTick) {
        return cloakOld + (cloak - cloakOld) * partialTick;
    }

    /** Ticks since the shield last took a hit, as far as this side has seen it (Integer.MAX_VALUE if never). */
    public int ticksSinceHit() {
        return sinceHit;
    }

    public float flash(float partialTick) {
        return Math.max(0, flash - partialTick) / FLASH_TICKS;
    }

    /** Cloaked enough to be invisible: only the bubble is left. */
    public boolean isHidden() {
        return cloak > CLOAKED_THRESHOLD;
    }

    public boolean isGone() {
        return shield <= 0f && shieldOld <= 0f && cloak <= 0f && cloakOld <= 0f;
    }
}
