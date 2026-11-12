package de.mccs.core.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.mccs.core.staff.StaffKind;
import org.junit.jupiter.api.Test;

class CrystalGlowTest {
    @Test
    void moreChargeShinesBrighterAndWider() {
        CrystalGlow.Halo full = CrystalGlow.halo(StaffKind.NORMAL, 100, 100, 0f);
        CrystalGlow.Halo half = CrystalGlow.halo(StaffKind.NORMAL, 50, 100, 0f);
        assertTrue(full.blue() > half.blue());
        assertTrue(full.radius() > half.radius());
    }

    @Test
    void nearlyEmptyIsRed() {
        CrystalGlow.Halo low = CrystalGlow.halo(StaffKind.NORMAL, 5, 100, 0f);
        assertTrue(low.red() > low.blue());
    }

    @Test
    void highEnergyCrystalShinesYellowish() {
        CrystalGlow.Halo glow = CrystalGlow.halo(StaffKind.HIGH_ENERGY, 100, 100, 0f);
        assertTrue(glow.red() > glow.green() && glow.green() > glow.blue());
    }

    @Test
    void onlyWellChargedCrackles() {
        assertTrue(CrystalGlow.crackles(60, 100));
        assertFalse(CrystalGlow.crackles(59, 100));
    }

    @Test
    void arcsStayNearTheCrystalAndSomeMomentsHaveNone() {
        float[] center = {1f, 2f, 3f};
        int none = 0;
        for (int t = 0; t < 300; t += 3) {
            float[][] arc = CrystalGlow.arc(center, 1f, 5, t, 0);
            if (arc == null) {
                none++;
                continue;
            }
            assertNotNull(arc[0]);
            double dx = arc[0][0] - center[0], dy = arc[0][1] - center[1], dz = arc[0][2] - center[2];
            assertTrue(Math.sqrt(dx * dx + dy * dy + dz * dz) < 0.3);
        }
        assertTrue(none > 0 && none < 100, "none=" + none);
    }

    @Test
    void haloIsOneQuad() {
        int[] vertices = {0};
        CrystalGlow.emitHalo(CrystalGlow.halo(StaffKind.NORMAL, 100, 100, 0f), new float[3], new float[]{1, 0, 0}, new float[]{0, 1, 0}, 1f,
                (x, y, z, r, g, b, a, u, v, nx, ny, nz) -> vertices[0]++);
        assertEquals(4, vertices[0]);
    }
}
