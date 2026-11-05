package de.mcqw.core.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class LightningBoltTest {
    private static final float[] FROM = {1f, 2f, 3f};
    private static final float[] TO = {1f, 2f, 13f};

    @Test
    void endsStayFixedAndTheMiddleZigzags() {
        float[][] path = LightningBolt.path(FROM, TO, 42, 0.3f);
        assertArrayEquals(FROM, path[0], 1e-4f);
        assertArrayEquals(TO, path[path.length - 1], 1e-4f);
        boolean moved = false;
        for (float[] point : path) moved |= Math.abs(point[0] - 1f) > 1e-3f || Math.abs(point[1] - 2f) > 1e-3f;
        assertTrue(moved);
    }

    @Test
    void sameSeedSamePath() {
        assertTrue(Arrays.deepEquals(LightningBolt.path(FROM, TO, 7, 0.3f), LightningBolt.path(FROM, TO, 7, 0.3f)));
        assertFalse(Arrays.deepEquals(LightningBolt.path(FROM, TO, 7, 0.3f), LightningBolt.path(FROM, TO, 8, 0.3f)));
    }

    @Test
    void worksInEveryDirectionAndForZeroLength() {
        for (float[] to : new float[][]{{1f, 12f, 3f}, {11f, 2f, 3f}, {1f, 2f, 3f}}) {
            for (float[] point : LightningBolt.path(FROM, to, 3, 0.3f)) {
                for (float value : point) assertFalse(Float.isNaN(value));
            }
        }
    }

    @Test
    void emitsFourVerticesPerSegment() {
        float[][] path = LightningBolt.path(FROM, TO, 42, 0.3f);
        int[] vertices = {0};
        LightningBolt.emitCore(path, (x, y, z, r, g, b, a, u, v, nx, ny, nz) -> vertices[0]++, 1f);
        assertEquals(4 * (path.length - 1), vertices[0]);
    }
}
