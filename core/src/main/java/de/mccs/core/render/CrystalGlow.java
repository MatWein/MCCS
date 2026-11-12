package de.mccs.core.render;

import de.mccs.core.staff.StaffEnergy;

/**
 * The light of the crystal: a halo facing the camera that follows the charge, red when nearly empty, and small electric arcs
 * around the crystal when charged well. All coordinates are relative to the camera.
 */
public final class CrystalGlow {
    private static final int LOW_LOOK = 3;
    private static final float[] BLUE = {0.3f, 0.6f, 1.0f};
    private static final float[] RED = {1.0f, 0.12f, 0.08f};
    private static final float MIN_BRIGHTNESS = 0.3f;
    private static final float MIN_RADIUS = 0.22f;
    private static final float RADIUS_RANGE = 0.2f;
    private static final float PULSE_SPEED = 0.15f;
    private static final float PULSE_RADIUS = 0.03f;
    private static final float LOW_FLICKER_SPEED = 0.5f;
    private static final float ARCS_FROM_SHARE = 0.6f;
    private static final float ARC_REACH = 0.26f;
    private static final float ARC_AMPLITUDE = 0.05f;
    private static final int ARC_TICKS = 3;
    /** Of this many moments, one has no arc: it crackles rather than burns. */
    private static final int ARC_PAUSE = 3;

    /** The halo: its color (already darkened) and radius. */
    public record Halo(float red, float green, float blue, float radius) {}

    private CrystalGlow() {}

    public static Halo halo(int stored, int capacity, float time) {
        float share = Math.min(1f, stored / (float) Math.max(1, capacity));
        if (StaffEnergy.crystalLook(stored, capacity) == LOW_LOOK) {
            float flicker = 0.55f + 0.45f * (float) Math.sin(time * LOW_FLICKER_SPEED);
            return new Halo(RED[0] * flicker, RED[1] * flicker, RED[2] * flicker, MIN_RADIUS);
        }
        float brightness = MIN_BRIGHTNESS + (1 - MIN_BRIGHTNESS) * share;
        float pulse = (float) Math.sin(time * PULSE_SPEED) * PULSE_RADIUS;
        return new Halo(BLUE[0] * brightness, BLUE[1] * brightness, BLUE[2] * brightness, MIN_RADIUS + RADIUS_RANGE * share + pulse);
    }

    public static boolean crackles(int stored, int capacity) {
        return stored / (float) Math.max(1, capacity) >= ARCS_FROM_SHARE;
    }

    /** An electric arc around the crystal, new every few ticks; null if there is none at this moment. */
    public static float[][] arc(float[] center, float scale, long seed, float time, int index) {
        long moment = (long) (time / ARC_TICKS);
        long state = (moment * 31 + index) * 0x5DEECE66DL + seed;
        if (Math.floorMod(state >> 16, ARC_PAUSE) == 0) return null;
        float[] from = around(center, scale, state);
        float[] to = around(center, scale, state * 0x5DEECE66DL + 11);
        return LightningBolt.path(from, to, state, ARC_AMPLITUDE * scale);
    }

    private static float[] around(float[] center, float scale, long state) {
        double angle = ((state >>> 8) & 0xFFFF) / 65536.0 * Math.PI * 2;
        double height = ((state >>> 24) & 0xFFFF) / 65536.0 * 2 - 1;
        double flat = Math.sqrt(1 - height * height);
        float reach = ARC_REACH * scale;
        return new float[]{center[0] + (float) (Math.cos(angle) * flat) * reach, center[1] + (float) height * reach, center[2] + (float) (Math.sin(angle) * flat) * reach};
    }

    /** The halo as one quad, spanned by the camera's right and up directions. */
    public static void emitHalo(Halo halo, float[] center, float[] right, float[] up, float scale, VertexSink sink) {
        float r = halo.radius() * scale;
        vertex(sink, center, right, up, -r, -r, halo, 0f, 0f);
        vertex(sink, center, right, up, r, -r, halo, 1f, 0f);
        vertex(sink, center, right, up, r, r, halo, 1f, 1f);
        vertex(sink, center, right, up, -r, r, halo, 0f, 1f);
    }

    private static void vertex(VertexSink sink, float[] c, float[] right, float[] up, float x, float y, Halo halo, float u, float v) {
        sink.vertex(c[0] + right[0] * x + up[0] * y, c[1] + right[1] * x + up[1] * y, c[2] + right[2] * x + up[2] * y,
                halo.red(), halo.green(), halo.blue(), 1f, u, v, 0f, 1f, 0f);
    }
}
