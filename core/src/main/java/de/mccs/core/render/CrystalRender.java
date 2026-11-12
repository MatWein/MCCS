package de.mccs.core.render;

import de.mccs.core.staff.StaffEnergy;
import de.mccs.core.staff.StaffKind;

import java.util.function.Consumer;

/**
 * The staff's crystal and its light, in the colors of the staff's kind, brighter the fuller and red and flickering when
 * nearly empty. Everything is made here; each Minecraft version only draws what it is handed ({@link Output}), and
 * tools/CrystalPreview.java renders the same to an image.
 *
 * <ul>
 * <li>the body: a cluster of irregular quartz shards ({@link CrystalShape}) shining on their own, almost white at their foot and the kind's
 * color towards the tips, neighbouring facets clearly brighter and darker like cut glass, with bands of light rising</li>
 * <li>glowing edges along all of the shards' edges, which draw the cut sharply</li>
 * <li>bloom: soft lights along each shard, pulled towards the camera so they lie over the crystal like light spilling out
 * of it, a hot spot at its foot and the halo, behind the crystal and once more in front of it</li>
 * <li>glints flashing on corners and tips and, when charged well, small electric arcs</li>
 * </ul>
 *
 * <p>The body is in item model space (one block = 1, the staff's axis at x = z = 0.5) and drawn with the item's pose. The
 * lights are in render space (where the item pose maps to, the camera at its origin): they face the camera along the
 * given right and up directions.
 */
public final class CrystalRender {
    /** The additive textures of the lights. */
    public enum Light {
        /** A soft round glow. */
        GLOW,
        /** A four-pointed star. */
        GLINT,
        /** A ribbon, bright along its middle (edges and arcs). */
        BEAM
    }

    /** Receives what to draw, in the order of the calls. */
    public interface Output {
        /** The body in item model space: opaque, full bright, with the crystal texture, drawn with the item's pose. */
        void body(Consumer<VertexSink> emitter);

        /** Additive light in render space with the given texture. */
        void light(Light light, Consumer<VertexSink> emitter);
    }

    /**
     * What decides the crystal's look: kind, charge, time in ticks, a seed for glints and arcs, and whether it is seen in
     * first person (held items are seen from very close and at the edge of the picture there, which makes the crystal look
     * squat: it is drawn narrower and taller to keep its proportions).
     */
    public record State(StaffKind kind, int stored, int capacity, float time, long seed, boolean firstPerson) {}

    private static final int LOW_LOOK = 3;
    /** The middle of the crystal in item model space: arcs and the ball being charged are centered on it. */
    public static final float[] CRYSTAL_CENTER = {0.5f, (CrystalShape.FOOT + 3.2f) / 16, 0.5f};
    private static final float[] HALO_CENTER = {0.5f, (CrystalShape.FOOT + 1.8f) / 16, 0.5f};
    /** In first person: the crystal's width and height relative to the other views, measured from its foot. */
    private static final float FIRST_PERSON_WIDTH = 0.8f;
    private static final float FIRST_PERSON_HEIGHT = 1.15f;
    private static final float FOOT = CrystalShape.FOOT;

    // charge and pulse
    private static final float MIN_BRIGHTNESS = 0.45f;
    private static final float PULSE_SPEED = 0.12f;
    private static final float PULSE = 0.1f;
    private static final float LOW_FLICKER_SPEED = 0.5f;
    private static final float[] RED_GLOW = {1.0f, 0.18f, 0.1f};
    /** How far the kind's color is lightened towards white: at the shards' foot and towards their tips. */
    private static final float HOT_WHITENESS = 0.85f;
    private static final float TIP_WHITENESS = 0.3f;

    // body
    /** Brightness of the facets around a shard: neighbours differ clearly, so the cut reads at a glance. */
    private static final float[] FACET_SHADES = {1.05f, 0.8f, 0.94f, 0.72f, 1.0f, 0.84f, 0.9f, 0.76f};
    private static final float TIP_SHADE = 1.1f;
    private static final float SECTION_SHADE = 0.15f;
    /** The body's own light at full charge, below white, so the facets and colors survive the bloom on top. */
    private static final float BODY_LIGHT = 0.82f;
    private static final float BAND_SPACING = 9f;
    private static final float BAND_SPEED = 0.18f;
    private static final int BAND_SHARPNESS = 8;
    private static final float BAND_STRENGTH = 0.45f;

    // edges
    private static final float EDGE_WIDTH = 0.011f;
    private static final float EDGE_LIGHT = 0.6f;
    /** Lights are pulled this far towards the camera (blocks), so the body does not hide them. */
    private static final float EDGE_LIFT = 0.004f;

    // bloom
    private static final int MAIN_BLOOMS = 6;
    private static final int SIDE_BLOOMS = 4;
    private static final int SMALL_BLOOMS = 3;
    /** Bloom radius relative to the shard's radius, how bright it is and how much of its radius it is pulled towards the camera. */
    private static final float BLOOM_SIZE = 3.4f;
    private static final float BLOOM_LIGHT = 0.24f;
    private static final float BLOOM_LIFT = 0.8f;
    private static final float HOT_SPOT_RADIUS = 0.085f;
    private static final float HOT_SPOT_LIGHT = 0.8f;
    private static final float HALO_MIN_RADIUS = 0.22f;
    private static final float HALO_RADIUS_RANGE = 0.2f;
    private static final float HALO_PULSE_SPEED = 0.15f;
    private static final float HALO_PULSE = 0.03f;
    private static final float HALO_LIGHT = 0.8f;

    // glints and arcs
    private static final int GLINTS = 4;
    private static final float GLINT_PERIOD = 30f;
    private static final float GLINT_DURATION = 7f;
    private static final float GLINT_RADIUS = 0.075f;
    private static final float GLINT_WHITENESS = 0.7f;
    private static final float ARCS_FROM_SHARE = 0.6f;
    private static final int ARCS = 2;
    private static final float ARC_WIDTH = 0.35f;
    private static final float ARC_REACH = 0.13f;
    private static final float ARC_AMPLITUDE = 0.035f;
    private static final int ARC_TICKS = 3;
    /** Of this many moments, one has no arc: it crackles rather than burns. */
    private static final int ARC_PAUSE = 3;

    private CrystalRender() {}

    /** The body alone: for the inventory, the ground and item frames. */
    public static void drawBody(State state, Output output) {
        Palette palette = palette(state);
        output.body(sink -> emitBody(palette, sink));
    }

    /**
     * The body and all of its light, for a staff in a hand. {@code itemPose} (column-major 4x4) maps item model space to
     * render space; {@code right} and {@code up} are the camera's directions in render space.
     */
    public static void draw(State state, float[] itemPose, float[] right, float[] up, Output output) {
        Palette palette = palette(state);
        float scale = scale(itemPose);
        output.body(sink -> emitBody(palette, sink));
        Billboards billboards = new Billboards(itemPose, right, up, scale);
        output.light(Light.GLOW, sink -> {
            emitHalo(palette, billboards, sink);
            emitBloom(palette, billboards, sink);
        });
        output.light(Light.BEAM, sink -> {
            emitEdges(palette, billboards, sink);
            emitArcs(palette, state, billboards, sink);
        });
        output.light(Light.GLINT, sink -> emitGlints(palette, state, billboards, sink));
    }

    /** Where the crystal's middle is in render space, e.g. for the ball being charged. */
    public static float[] center(State state, float[] itemPose) {
        return transform(itemPose, fit(palette(state), CRYSTAL_CENTER));
    }

    /** The item's own scale (held items are smaller in first person, and so is everything at the crystal). */
    public static float scale(float[] itemPose) {
        return (float) Math.sqrt(itemPose[0] * itemPose[0] + itemPose[1] * itemPose[1] + itemPose[2] * itemPose[2]);
    }

    /** Whether the crystal is charged well enough to crackle with arcs (and throw sparks). */
    public static boolean crackles(int stored, int capacity) {
        return stored / (float) Math.max(1, capacity) >= ARCS_FROM_SHARE;
    }

    // ---- colors

    /** The crystal's colors at this charge (before brightness), its brightness, the time for moving things (NaN: still, nearly empty). */
    private record Palette(float[] hot, float[] glow, float[] tip, float brightness, float time, float width, float height) {}

    private static Palette palette(State state) {
        boolean low = StaffEnergy.crystalLook(state.stored(), state.capacity()) == LOW_LOOK;
        float share = Math.min(1f, state.stored() / (float) Math.max(1, state.capacity()));
        float time = state.time();
        float brightness = low
                ? 0.55f + 0.45f * (float) Math.sin(time * LOW_FLICKER_SPEED)
                : (MIN_BRIGHTNESS + (1 - MIN_BRIGHTNESS) * share) * (1 - PULSE + PULSE * (float) Math.sin(time * PULSE_SPEED));
        float[] glow = new float[3];
        float[] hot = new float[3];
        float[] tip = new float[3];
        for (int c = 0; c < 3; c++) {
            glow[c] = low ? RED_GLOW[c] : state.kind().glow(c);
            hot[c] = glow[c] + (1 - glow[c]) * HOT_WHITENESS;
            tip[c] = glow[c] + (1 - glow[c]) * TIP_WHITENESS;
        }
        float width = state.firstPerson() ? FIRST_PERSON_WIDTH : 1f;
        float height = state.firstPerson() ? FIRST_PERSON_HEIGHT : 1f;
        return new Palette(hot, glow, tip, brightness, low ? Float.NaN : time, width, height);
    }

    /** From hot (foot) to the light kind's color (tip). */
    private static float[] along(Palette palette, float along, float light) {
        float t = smooth(Math.min(1f, Math.max(0f, (along - 0.05f) / 0.85f)));
        float[] color = new float[3];
        for (int c = 0; c < 3; c++) {
            color[c] = (palette.hot()[c] + (palette.tip()[c] - palette.hot()[c]) * t) * light;
        }
        return color;
    }

    private static float smooth(float t) {
        return t * t * (3 - 2 * t);
    }

    // ---- body

    private static void emitBody(Palette palette, VertexSink sink) {
        for (int s = 0; s < CrystalShape.SHARDS.length; s++) {
            int offset = s * 2;
            CrystalShape.faces(CrystalShape.SHARDS[s], (corners, along, side, section, tip) -> {
                // each section of the column catches the light a little differently, so the sides break into facets
                float sectionShade = 1 + SECTION_SHADE * (float) Math.sin(section * 2.3 + side * 1.7 + offset);
                float shade = FACET_SHADES[(side + offset) % FACET_SHADES.length] * sectionShade * (tip ? TIP_SHADE : 1f);
                float[] normal = CrystalShape.normalized(CrystalShape.cross(sub(corners[1], corners[0]), sub(corners[2], corners[0])));
                for (int i = 0; i < 4; i++) {
                    float band = Float.isNaN(palette.time()) ? 0
                            : (float) Math.pow(Math.max(0, Math.sin(along[i] * BAND_SPACING - palette.time() * BAND_SPEED)), BAND_SHARPNESS) * BAND_STRENGTH;
                    float[] color = along(palette, along[i], palette.brightness() * BODY_LIGHT * (shade + band));
                    float[] p = modelSpace(palette, corners[i]);
                    float u = i == 1 || i == 2 ? 1f : 0f;
                    sink.vertex(p[0], p[1], p[2], clamp(color[0]), clamp(color[1]), clamp(color[2]), 1f, u, 1 - along[i], normal[0], normal[1], normal[2]);
                }
            });
        }
    }

    // ---- lights

    /** Camera-facing quads in render space. */
    private record Billboards(float[] pose, float[] right, float[] up, float scale) {
        /** A point of the model in render space, pulled {@code lift} blocks (at item scale 1) towards the camera. */
        float[] at(float[] model, float lift) {
            float[] p = transform(pose, model);
            float length = (float) Math.sqrt(p[0] * p[0] + p[1] * p[1] + p[2] * p[2]);
            if (length > 1e-4f && lift != 0) {
                float k = lift * scale / length;
                p[0] -= p[0] * k;
                p[1] -= p[1] * k;
                p[2] -= p[2] * k;
            }
            return p;
        }

        void quad(VertexSink sink, float[] center, float radius, float[] color) {
            float r = radius * scale;
            vertex(sink, center, -r, -r, color, 0f, 0f);
            vertex(sink, center, r, -r, color, 1f, 0f);
            vertex(sink, center, r, r, color, 1f, 1f);
            vertex(sink, center, -r, r, color, 0f, 1f);
        }

        private void vertex(VertexSink sink, float[] c, float x, float y, float[] color, float u, float v) {
            sink.vertex(c[0] + right[0] * x + up[0] * y, c[1] + right[1] * x + up[1] * y, c[2] + right[2] * x + up[2] * y,
                    clamp(color[0]), clamp(color[1]), clamp(color[2]), 1f, u, v, 0f, 1f, 0f);
        }

        /** A ribbon from a to b (render space), facing the camera at the origin. */
        void ribbon(VertexSink sink, float[] a, float[] b, float width, float[] colorA, float[] colorB) {
            float[] direction = sub(b, a);
            float[] middle = {(a[0] + b[0]) / 2, (a[1] + b[1]) / 2, (a[2] + b[2]) / 2};
            float[] side = CrystalShape.normalized(CrystalShape.cross(direction, middle));
            float w = width * scale / 2;
            sink.vertex(a[0] - side[0] * w, a[1] - side[1] * w, a[2] - side[2] * w, clamp(colorA[0]), clamp(colorA[1]), clamp(colorA[2]), 1f, 0f, 0f, 0f, 1f, 0f);
            sink.vertex(a[0] + side[0] * w, a[1] + side[1] * w, a[2] + side[2] * w, clamp(colorA[0]), clamp(colorA[1]), clamp(colorA[2]), 1f, 0f, 1f, 0f, 1f, 0f);
            sink.vertex(b[0] + side[0] * w, b[1] + side[1] * w, b[2] + side[2] * w, clamp(colorB[0]), clamp(colorB[1]), clamp(colorB[2]), 1f, 1f, 1f, 0f, 1f, 0f);
            sink.vertex(b[0] - side[0] * w, b[1] - side[1] * w, b[2] - side[2] * w, clamp(colorB[0]), clamp(colorB[1]), clamp(colorB[2]), 1f, 1f, 0f, 0f, 1f, 0f);
        }
    }

    private static void emitHalo(Palette palette, Billboards billboards, VertexSink sink) {
        float radius = HALO_MIN_RADIUS + (Float.isNaN(palette.time()) ? 0
                : HALO_RADIUS_RANGE * (palette.brightness() - MIN_BRIGHTNESS) / (1 - MIN_BRIGHTNESS) + HALO_PULSE * (float) Math.sin(palette.time() * HALO_PULSE_SPEED));
        float[] color = scaled(palette.glow(), palette.brightness() * HALO_LIGHT);
        // behind the crystal, and once more in front of it, so its light also lies over the crystal
        float[] center = fit(palette, HALO_CENTER);
        billboards.quad(sink, billboards.at(center, 0), radius, color);
        billboards.quad(sink, billboards.at(center, radius), radius, color);
    }

    private static void emitBloom(Palette palette, Billboards billboards, VertexSink sink) {
        for (int s = 0; s < CrystalShape.SHARDS.length; s++) {
            CrystalShape.Shard shard = CrystalShape.SHARDS[s];
            int count = s == 0 ? MAIN_BLOOMS : shard.radius() > 0.5f ? SIDE_BLOOMS : SMALL_BLOOMS;
            for (int i = 0; i < count; i++) {
                float along = 0.15f + 0.72f * i / (count - 1);
                float radius = shard.radius() / 16 * BLOOM_SIZE * (1 - 0.3f * along);
                float[] color = scaled(palette.glow(), palette.brightness() * BLOOM_LIGHT * (1 - 0.1f * along));
                billboards.quad(sink, billboards.at(modelSpace(palette, CrystalShape.axisPoint(shard, along)), radius * BLOOM_LIFT), radius, color);
            }
        }
        // the hot spot at the main shard's foot
        float[] foot = modelSpace(palette, CrystalShape.axisPoint(CrystalShape.SHARDS[0], 0.12f));
        billboards.quad(sink, billboards.at(foot, HOT_SPOT_RADIUS), HOT_SPOT_RADIUS, scaled(palette.hot(), palette.brightness() * HOT_SPOT_LIGHT));
    }

    private static void emitEdges(Palette palette, Billboards billboards, VertexSink sink) {
        for (CrystalShape.Shard shard : CrystalShape.SHARDS) {
            CrystalShape.edges(shard, (from, to, alongFrom, alongTo) -> billboards.ribbon(sink,
                    billboards.at(modelSpace(palette, from), EDGE_LIFT), billboards.at(modelSpace(palette, to), EDGE_LIFT), EDGE_WIDTH,
                    along(palette, alongFrom, palette.brightness() * EDGE_LIGHT), along(palette, alongTo, palette.brightness() * EDGE_LIGHT)));
        }
    }

    private static void emitGlints(Palette palette, State state, Billboards billboards, VertexSink sink) {
        if (Float.isNaN(palette.time())) return;
        java.util.List<float[]> points = new java.util.ArrayList<>();
        for (CrystalShape.Shard shard : CrystalShape.SHARDS) CrystalShape.sparklePoints(shard, points::add);
        float[] white = new float[3];
        for (int c = 0; c < 3; c++) white[c] = palette.glow()[c] + (1 - palette.glow()[c]) * GLINT_WHITENESS;
        for (int i = 0; i < GLINTS; i++) {
            long slot = mix(state.seed() * 31 + i);
            float shifted = palette.time() + (slot & 0xFFFF) / 65536f * GLINT_PERIOD;
            long cycle = (long) Math.floor(shifted / GLINT_PERIOD);
            float age = shifted - cycle * GLINT_PERIOD;
            if (age >= GLINT_DURATION) continue;
            float flash = (float) Math.sin(Math.PI * age / GLINT_DURATION);
            float[] corner = points.get((int) Math.floorMod(mix(slot + cycle * 0x9E3779B97F4A7C15L), points.size()));
            float radius = GLINT_RADIUS * (0.4f + 0.6f * flash);
            billboards.quad(sink, billboards.at(modelSpace(palette, corner), radius), radius, scaled(white, palette.brightness() * flash));
        }
    }

    private static void emitArcs(Palette palette, State state, Billboards billboards, VertexSink sink) {
        if (Float.isNaN(palette.time()) || !crackles(state.stored(), state.capacity())) return;
        float[] center = billboards.at(fit(palette, CRYSTAL_CENTER), 0);
        for (int i = 0; i < ARCS; i++) {
            long moment = (long) (palette.time() / ARC_TICKS);
            long arcState = (moment * 31 + i) * 0x5DEECE66DL + state.seed();
            if (Math.floorMod(arcState >> 16, ARC_PAUSE) == 0) continue;
            float[] from = around(center, billboards.scale(), arcState);
            float[] to = around(center, billboards.scale(), arcState * 0x5DEECE66DL + 11);
            LightningBolt.emitCore(LightningBolt.path(from, to, arcState, ARC_AMPLITUDE * billboards.scale()), sink, ARC_WIDTH * billboards.scale());
        }
    }

    private static float[] around(float[] center, float scale, long state) {
        double angle = ((state >>> 8) & 0xFFFF) / 65536.0 * Math.PI * 2;
        double height = ((state >>> 24) & 0xFFFF) / 65536.0 * 2 - 1;
        double flat = Math.sqrt(1 - height * height);
        float reach = ARC_REACH * scale;
        return new float[]{center[0] + (float) (Math.cos(angle) * flat) * reach, center[1] + (float) height * reach, center[2] + (float) (Math.sin(angle) * flat) * reach};
    }

    // ---- small helpers

    /** From the shape's model units (around the staff's axis) to item model space, in the proportions of the view. */
    private static float[] modelSpace(Palette palette, float[] units) {
        return new float[]{0.5f + units[0] * palette.width() / 16, (FOOT + (units[1] - FOOT) * palette.height()) / 16, 0.5f + units[2] * palette.width() / 16};
    }

    /** A point of item model space in the proportions of the view. */
    private static float[] fit(Palette palette, float[] point) {
        return modelSpace(palette, new float[]{(point[0] - 0.5f) * 16, point[1] * 16, (point[2] - 0.5f) * 16});
    }

    static float[] transform(float[] m, float[] p) {
        return new float[]{
                m[0] * p[0] + m[4] * p[1] + m[8] * p[2] + m[12],
                m[1] * p[0] + m[5] * p[1] + m[9] * p[2] + m[13],
                m[2] * p[0] + m[6] * p[1] + m[10] * p[2] + m[14]};
    }

    private static float[] sub(float[] a, float[] b) {
        return new float[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    private static float[] scaled(float[] color, float k) {
        return new float[]{color[0] * k, color[1] * k, color[2] * k};
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xFF51AFD7ED558CCDL;
        value ^= value >>> 33;
        value *= 0xC4CEB9FE1A85EC53L;
        return value ^ (value >>> 33);
    }
}
