package de.mcqw.core.staff;

/** The shield's fade and hit flash for one player, with the same speeds as the shield of MCPJ's jumper. */
public final class ShieldAnimation {
    private static final float FADE_SPEED = 0.06f;
    private static final int FLASH_TICKS = 10;

    private float shield;
    private float shieldOld;
    private int flash;
    private int lastHits = -1;

    /** Once per client tick; {@code hits} is the hit counter of the active staff. */
    public void tick(boolean active, int hits) {
        shieldOld = shield;
        shield = active ? Math.min(1f, shield + FADE_SPEED) : Math.max(0f, shield - FADE_SPEED);
        if (flash > 0) flash--;
        if (active) {
            if (lastHits >= 0 && hits > lastHits) flash = FLASH_TICKS;
            lastHits = hits;
        } else {
            lastHits = -1;
        }
    }

    public float shield(float partialTick) {
        return shieldOld + (shield - shieldOld) * partialTick;
    }

    public float flash(float partialTick) {
        return Math.max(0, flash - partialTick) / FLASH_TICKS;
    }

    public boolean isGone() {
        return shield <= 0f && shieldOld <= 0f;
    }
}
