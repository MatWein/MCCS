package de.mccs.core.render;

import java.util.function.Consumer;

/**
 * The shape of the staff's crystal: a cluster of quartz shards growing like a bouquet out of the staff's head, irregular
 * like real crystals. Each shard is an eight-sided column with faces of different widths, built from several slightly
 * twisted, tapering sections (so its sides break into many facets), with a short foot and a two-step tip; the smaller
 * ones are gently bent with off-centre tips, the main one stands straight. The irregularities come from a fixed seed
 * per shard. Coordinates are model units (1/16 block): x and z relative to the staff's axis, y absolute.
 */
final class CrystalShape {
    static final int SIDES = 8;
    /** Where the crystal sits: just above the staff's head (HEAD_RAISE in tools/make_staff_assets.py moves both). */
    static final float FOOT = 22.8f;
    /** How far the sections' middles and corners stray (relative to the radius), so the sides break into facets. */
    private static final float KINK = 0.12f;
    private static final float JITTER = 0.1f;

    /** A cross-section of a shard: its corners, its middle and how far along the shard it is (0 foot tip, 1 top tip). */
    private record Ring(float[][] corners, float[] center, float along, boolean edged) {}

    /** A shard, its rings from foot to top between its two tips. */
    record Shard(float[] footTip, Ring[] rings, float[] topTip, float radius) {}

    /**
     * The main shard in the middle (straight: no bend, kinks or leaning tip), then smaller ones leaning outwards between the
     * staff's fins (at 0, 90, 180, 270 degrees).
     */
    static final Shard[] SHARDS = {
            shard(0, 0f, FOOT, 0, 1.2f, 0.6f, 3.8f, 1.9f, 0f, 11),
            shard(130, 0.45f, FOOT + 0.3f, 19, 0.68f, 0.45f, 2.2f, 1.3f, 0.2f, 23),
            shard(250, 0.45f, FOOT + 0.2f, 24, 0.6f, 0.4f, 1.8f, 1.15f, 0.15f, 37),
            shard(48, 0.55f, FOOT + 0.4f, 33, 0.44f, 0.3f, 1.0f, 0.8f, 0.1f, 41),
            shard(318, 0.55f, FOOT + 0.5f, 37, 0.38f, 0.28f, 0.8f, 0.7f, 0.1f, 53)};

    private CrystalShape() {}

    /**
     * Builds a shard: direction (degrees around the staff's axis) and tilt away from it, the foot's offset and height, its
     * radius, the lengths of foot, column and tip, how much it bends (0: straight, without kinks and with its tip on its
     * axis) and the seed of its irregularities.
     */
    private static Shard shard(double degrees, float offset, float height, double tiltDegrees, float radius,
                               float foot, float column, float tip, float bend, long seed) {
        java.util.Random random = new java.util.Random(seed);
        double around = Math.toRadians(degrees);
        double tilt = Math.toRadians(tiltDegrees);
        float[] base = {(float) Math.cos(around) * offset, height, (float) Math.sin(around) * offset};
        float[] axis = {(float) (Math.cos(around) * Math.sin(tilt)), (float) Math.cos(tilt), (float) (Math.sin(around) * Math.sin(tilt))};
        float[][] basis = basis(axis);
        // which way the shard bends and its tip leans
        double bendAngle = random.nextDouble() * Math.PI * 2;
        float[] bendDirection = add(scale(basis[0], (float) Math.cos(bendAngle)), scale(basis[1], (float) Math.sin(bendAngle)));
        // faces of different widths: each corner a little in or out, the same along the whole column
        float[] corner = new float[SIDES];
        for (int k = 0; k < SIDES; k++) corner[k] = 0.88f + 0.22f * random.nextFloat();
        double turn = random.nextDouble() * Math.PI;
        float length = foot + column + tip;
        boolean straight = bend == 0;

        // the column's rings: foot, three sections tapering upwards, then a ring halfway up the tip
        float[] at = {foot, foot + column * 0.34f, foot + column * 0.68f, foot + column, foot + column + tip * 0.42f};
        float[] size = {0.88f, 1f, 0.95f, 0.88f, 0.5f};
        boolean[] edged = {false, false, false, true, true};
        Ring[] rings = new Ring[at.length];
        for (int r = 0; r < at.length; r++) {
            float t = at[r];
            float[] center = add(add(base, scale(axis, t)), scale(bendDirection, bend * (t / length) * (t / length)));
            // small kinks between the sections, like a crystal that grew in steps
            if (!straight && r > 0 && r < at.length - 1) {
                center = add(center, add(scale(basis[0], (random.nextFloat() - 0.5f) * radius * KINK),
                        scale(basis[1], (random.nextFloat() - 0.5f) * radius * KINK)));
            }
            // the tip's ring leans towards the side the tip points to
            if (!straight && r == at.length - 1) center = add(center, scale(bendDirection, radius * 0.18f));
            double twist = turn + Math.toRadians(4) * r;
            float[][] corners = new float[SIDES][];
            for (int k = 0; k < SIDES; k++) {
                double angle = twist + Math.PI * 2 * k / SIDES;
                float jitter = 1 + (random.nextFloat() - 0.5f) * JITTER;
                float rr = radius * size[r] * corner[k] * jitter;
                corners[k] = add(center, add(scale(basis[0], (float) Math.cos(angle) * rr), scale(basis[1], (float) Math.sin(angle) * rr)));
            }
            rings[r] = new Ring(corners, center, t / length, edged[r]);
        }
        float lean = straight ? 0 : 1;
        float[] footTip = add(base, scale(bendDirection, -radius * 0.1f * lean));
        float[] topTip = add(add(add(base, scale(axis, length)), scale(bendDirection, bend)), scale(bendDirection, radius * 0.3f * lean));
        return new Shard(footTip, rings, topTip, radius);
    }

    /** One face: four corners (a triangle repeats its tip), how far along the shard each is, its index around the shard, which section it is in and whether it belongs to the tip. */
    interface Faces {
        void face(float[][] corners, float[] along, int side, int section, boolean tip);
    }

    /** A straight edge of a shard between two points. */
    interface Edges {
        void edge(float[] from, float[] to, float alongFrom, float alongTo);
    }

    static void faces(Shard shard, Faces output) {
        Ring[] rings = shard.rings();
        int last = rings.length - 1;
        for (int k = 0; k < SIDES; k++) {
            int n = (k + 1) % SIDES;
            Ring foot = rings[0];
            output.face(new float[][]{foot.corners()[n], foot.corners()[k], shard.footTip(), shard.footTip()},
                    new float[]{foot.along(), foot.along(), 0f, 0f}, k, 0, false);
            for (int r = 0; r < last; r++) {
                Ring lower = rings[r];
                Ring upper = rings[r + 1];
                boolean tip = r + 1 == last;
                output.face(new float[][]{lower.corners()[k], lower.corners()[n], upper.corners()[n], upper.corners()[k]},
                        new float[]{lower.along(), lower.along(), upper.along(), upper.along()}, k, r + 1, tip);
            }
            Ring top = rings[last];
            output.face(new float[][]{top.corners()[k], top.corners()[n], shard.topTip(), shard.topTip()},
                    new float[]{top.along(), top.along(), 1f, 1f}, k, last + 1, true);
        }
    }

    /** The shard's edges: along the column and tip, and around the rings where the cut changes. */
    static void edges(Shard shard, Edges output) {
        Ring[] rings = shard.rings();
        int last = rings.length - 1;
        for (int k = 0; k < SIDES; k++) {
            int n = (k + 1) % SIDES;
            for (int r = 0; r < last; r++) {
                output.edge(rings[r].corners()[k], rings[r + 1].corners()[k], rings[r].along(), rings[r + 1].along());
            }
            output.edge(rings[last].corners()[k], shard.topTip(), rings[last].along(), 1f);
            for (Ring ring : rings) {
                if (ring.edged()) output.edge(ring.corners()[k], ring.corners()[n], ring.along(), ring.along());
            }
        }
    }

    /** A point on the shard's middle line, {@code along} from its foot tip (0) to its top tip (1). */
    static float[] axisPoint(Shard shard, float along) {
        float[] previous = shard.footTip();
        float previousAlong = 0f;
        for (Ring ring : shard.rings()) {
            if (along <= ring.along()) return lerp(previous, ring.center(), (along - previousAlong) / (ring.along() - previousAlong));
            previous = ring.center();
            previousAlong = ring.along();
        }
        return lerp(previous, shard.topTip(), (along - previousAlong) / (1f - previousAlong));
    }

    /** The corners where light catches: the top of the column, the tip's ring and the tip. */
    static void sparklePoints(Shard shard, Consumer<float[]> output) {
        for (Ring ring : shard.rings()) {
            if (ring.edged()) for (float[] corner : ring.corners()) output.accept(corner);
        }
        output.accept(shard.topTip());
    }

    // ---- vectors

    private static float[] lerp(float[] a, float[] b, float t) {
        return new float[]{a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t};
    }

    private static float[] add(float[] a, float[] b) {
        return new float[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    private static float[] scale(float[] a, float k) {
        return new float[]{a[0] * k, a[1] * k, a[2] * k};
    }

    /** Two directions perpendicular to the axis and to each other. */
    private static float[][] basis(float[] axis) {
        float[] reference = Math.abs(axis[1]) < 0.9f ? new float[]{0f, 1f, 0f} : new float[]{1f, 0f, 0f};
        float[] u = normalized(cross(reference, axis));
        float[] w = cross(axis, u);
        return new float[][]{u, w};
    }

    static float[] cross(float[] a, float[] b) {
        return new float[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    static float[] normalized(float[] v) {
        float length = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        return length == 0 ? new float[]{0f, 1f, 0f} : new float[]{v[0] / length, v[1] / length, v[2] / length};
    }
}
