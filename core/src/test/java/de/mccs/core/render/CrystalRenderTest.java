package de.mccs.core.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.mccs.core.staff.StaffKind;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

class CrystalRenderTest {
    private static final float[] IDENTITY = {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};
    /** The item in front of the camera (render space has the camera at its origin, looking along -z). */
    private static final float[] IN_FRONT = {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, -0.5f, -1.7f, -2f, 1};

    /** Collects vertices per layer: body and each light. */
    private static final class Recorder implements CrystalRender.Output {
        final List<float[]> body = new ArrayList<>();
        final Map<CrystalRender.Light, List<float[]>> lights = new EnumMap<>(CrystalRender.Light.class);
        final List<String> order = new ArrayList<>();

        @Override
        public void body(Consumer<VertexSink> emitter) {
            order.add("body");
            emitter.accept((x, y, z, r, g, b, a, u, v, nx, ny, nz) -> body.add(new float[]{x, y, z, r, g, b}));
        }

        @Override
        public void light(CrystalRender.Light light, Consumer<VertexSink> emitter) {
            order.add(light.name());
            List<float[]> list = lights.computeIfAbsent(light, l -> new ArrayList<>());
            emitter.accept((x, y, z, r, g, b, a, u, v, nx, ny, nz) -> list.add(new float[]{x, y, z, r, g, b}));
        }
    }

    private static Recorder draw(StaffKind kind, int stored, float time) {
        Recorder recorder = new Recorder();
        CrystalRender.draw(new CrystalRender.State(kind, stored, 100, time, 7, false), IN_FRONT, new float[]{1, 0, 0}, new float[]{0, 1, 0}, recorder);
        return recorder;
    }

    @Test
    void bodyComesFirstInQuadsAndSitsAboveTheStaffHead() {
        Recorder recorder = draw(StaffKind.NORMAL, 100, 10);
        assertEquals("body", recorder.order.get(0));
        assertEquals(0, recorder.body.size() % 4);
        for (float[] v : recorder.body) {
            assertTrue(v[1] > 22f / 16 && v[1] < 31.5f / 16, "y=" + v[1]);
            assertTrue(Math.abs(v[0] - 0.5f) < 0.25f && Math.abs(v[2] - 0.5f) < 0.25f);
        }
    }

    @Test
    void lightsGlowEdgesAndGlintsAreDrawnAfterTheBody() {
        Recorder recorder = draw(StaffKind.NORMAL, 100, 10);
        assertEquals(List.of("body", "GLOW", "BEAM", "GLINT"), recorder.order);
        assertFalse(recorder.lights.get(CrystalRender.Light.GLOW).isEmpty());
        assertFalse(recorder.lights.get(CrystalRender.Light.BEAM).isEmpty());
    }

    @Test
    void bodyIsBrighterAtTheFootThanAtTheTip() {
        Recorder recorder = draw(StaffKind.NORMAL, 100, 0);
        float foot = 0, tip = 0;
        for (float[] v : recorder.body) {
            float value = v[3] + v[4] + v[5];
            if (v[1] < 23.9f / 16) foot = Math.max(foot, value);
            if (v[1] > 28.5f / 16) tip = Math.max(tip, value);
        }
        assertTrue(foot > tip, "foot " + foot + " tip " + tip);
    }

    @Test
    void highEnergyCrystalIsWarm() {
        float red = 0, blue = 0;
        for (float[] v : draw(StaffKind.HIGH_ENERGY, 100, 0).body) {
            red += v[3];
            blue += v[5];
        }
        assertTrue(red > blue);
    }

    @Test
    void nearlyEmptyCrystalHasNoGlintsAndIsRed() {
        Recorder recorder = draw(StaffKind.NORMAL, 5, 0);
        assertTrue(recorder.lights.getOrDefault(CrystalRender.Light.GLINT, List.of()).isEmpty());
        float red = 0, blue = 0;
        for (float[] v : recorder.body) {
            red += v[3];
            blue += v[5];
        }
        assertTrue(red > blue);
    }

    @Test
    void scaleAndCenterFollowThePose() {
        assertEquals(1f, CrystalRender.scale(IDENTITY), 1e-5f);
        float[] half = {0.5f, 0, 0, 0, 0, 0.5f, 0, 0, 0, 0, 0.5f, 0, 0, 0, 0, 1};
        assertEquals(0.5f, CrystalRender.scale(half), 1e-5f);
        CrystalRender.State state = new CrystalRender.State(StaffKind.NORMAL, 100, 100, 0, 7, false);
        assertEquals(CrystalRender.CRYSTAL_CENTER[1], CrystalRender.center(state, IDENTITY)[1], 1e-5f);
    }

    @Test
    void inFirstPersonTheCrystalIsNarrowerAndTaller() {
        float[] normal = extent(false);
        float[] firstPerson = extent(true);
        assertTrue(firstPerson[0] < normal[0], "narrower");
        assertTrue(firstPerson[1] > normal[1], "taller");
    }

    /** Widest distance from the staff's axis and highest point of the body. */
    private static float[] extent(boolean firstPerson) {
        Recorder recorder = new Recorder();
        CrystalRender.drawBody(new CrystalRender.State(StaffKind.NORMAL, 100, 100, 0, 7, firstPerson), recorder);
        float[] result = new float[2];
        for (float[] v : recorder.body) {
            result[0] = Math.max(result[0], (float) Math.hypot(v[0] - 0.5f, v[2] - 0.5f));
            result[1] = Math.max(result[1], v[1]);
        }
        return result;
    }

    @Test
    void onlyWellChargedCrackles() {
        assertTrue(CrystalRender.crackles(60, 100));
        assertFalse(CrystalRender.crackles(59, 100));
    }
}
