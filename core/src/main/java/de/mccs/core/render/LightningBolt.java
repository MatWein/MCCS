package de.mccs.core.render;

/**
 * A jagged lightning path, drawn as ribbons that always face the camera. All coordinates are relative to the camera,
 * which is at the origin.
 */
public final class LightningBolt {
    private static final float SEGMENT_LENGTH = 0.8f;
    private static final int MAX_SEGMENTS = 48;
    private static final float MIN_LENGTH = 1e-4f;
    /** Blue of the crystal around a nearly white core. */
    private static final float[] GLOW_COLOR = {0.18f, 0.45f, 1.0f};
    private static final float[] CORE_COLOR = {0.75f, 0.92f, 1.0f};
    private static final float GLOW_WIDTH = 0.16f;
    private static final float CORE_WIDTH = 0.045f;

    private LightningBolt() {}

    /** The path's points; the ends stay fixed and the zigzag is widest in the middle. The same seed gives the same path. */
    public static float[][] path(float[] from, float[] to, long seed, float amplitude) {
        float dx = to[0] - from[0];
        float dy = to[1] - from[1];
        float dz = to[2] - from[2];
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        int segments = Math.max(2, Math.min(MAX_SEGMENTS, (int) Math.ceil(length / SEGMENT_LENGTH)));
        float[] axisA = perpendicular(dx, dy, dz);
        float[] axisB = normalized(cross(new float[]{dx, dy, dz}, axisA));
        float[][] points = new float[segments + 1][];
        long state = seed * 0x5DEECE66DL + 11;
        for (int i = 0; i <= segments; i++) {
            float t = i / (float) segments;
            float envelope = (float) Math.sin(Math.PI * t) * amplitude;
            state = state * 0x5DEECE66DL + 11;
            float a = random(state) * envelope;
            state = state * 0x5DEECE66DL + 11;
            float b = random(state) * envelope;
            points[i] = new float[]{
                    from[0] + dx * t + axisA[0] * a + axisB[0] * b,
                    from[1] + dy * t + axisA[1] * a + axisB[1] * b,
                    from[2] + dz * t + axisA[2] * a + axisB[2] * b};
        }
        return points;
    }

    /** Draws a path with its glow and core, four vertices per segment and layer; {@code scale} makes it thinner. */
    public static void emitGlow(float[][] points, VertexSink sink, float scale) {
        emit(points, sink, GLOW_WIDTH * scale, GLOW_COLOR);
    }

    public static void emitCore(float[][] points, VertexSink sink, float scale) {
        emit(points, sink, CORE_WIDTH * scale, CORE_COLOR);
    }

    private static void emit(float[][] points, VertexSink sink, float width, float[] color) {
        for (int i = 0; i + 1 < points.length; i++) {
            float[] a = points[i];
            float[] b = points[i + 1];
            float[] direction = {b[0] - a[0], b[1] - a[1], b[2] - a[2]};
            float[] middle = {(a[0] + b[0]) / 2, (a[1] + b[1]) / 2, (a[2] + b[2]) / 2};
            float[] side = cross(direction, middle);
            float sideLength = length(side);
            if (sideLength < MIN_LENGTH) continue;
            float x = side[0] / sideLength * width;
            float y = side[1] / sideLength * width;
            float z = side[2] / sideLength * width;
            vertex(sink, a[0] - x, a[1] - y, a[2] - z, color, 0f, 0f);
            vertex(sink, a[0] + x, a[1] + y, a[2] + z, color, 0f, 1f);
            vertex(sink, b[0] + x, b[1] + y, b[2] + z, color, 1f, 1f);
            vertex(sink, b[0] - x, b[1] - y, b[2] - z, color, 1f, 0f);
        }
    }

    private static void vertex(VertexSink sink, float x, float y, float z, float[] color, float u, float v) {
        sink.vertex(x, y, z, color[0], color[1], color[2], 1f, u, v, 0f, 1f, 0f);
    }

    private static float random(long state) {
        return ((state >>> 16) & 0xFFFF) / 32767.5f - 1f;
    }

    private static float[] perpendicular(float x, float y, float z) {
        float[] other = Math.abs(y) < Math.abs(x) + Math.abs(z) ? new float[]{0f, 1f, 0f} : new float[]{1f, 0f, 0f};
        return normalized(cross(new float[]{x, y, z}, other));
    }

    private static float[] cross(float[] a, float[] b) {
        return new float[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    private static float length(float[] v) {
        return (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }

    private static float[] normalized(float[] v) {
        float length = Math.max(MIN_LENGTH, length(v));
        return new float[]{v[0] / length, v[1] / length, v[2] / length};
    }
}
