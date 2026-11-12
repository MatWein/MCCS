package de.mccs.core.staff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BeamMathTest {
    @Test
    void hitsPerSecondBecomeAnInterval() {
        assertEquals(5, BeamMath.hitInterval(4));
        assertEquals(1, BeamMath.hitInterval(20));
    }

    @Test
    void chargeRisesFromStartToMaxAndStays() {
        assertEquals(0.5, BeamMath.chargeFactor(0, 3.0, 0.5, 1.5), 1e-9);
        assertEquals(1.0, BeamMath.chargeFactor(30, 3.0, 0.5, 1.5), 1e-9);
        assertEquals(1.5, BeamMath.chargeFactor(60, 3.0, 0.5, 1.5), 1e-9);
        assertEquals(1.5, BeamMath.chargeFactor(600, 3.0, 0.5, 1.5), 1e-9);
        assertEquals(1.5, BeamMath.chargeFactor(0, 0.0, 0.5, 1.5), 1e-9);
    }

    @Test
    void damageAddsUpToDamagePerSecond() {
        float perHit = BeamMath.hitDamage(8.0, BeamMath.hitInterval(4), 1.0, 0, 0.5, 1.0);
        assertEquals(8f, perHit * 4, 1e-5f);
    }

    @Test
    void eachJumpHalvesAndWetTargetsTakeMore() {
        assertEquals(1f, BeamMath.hitDamage(8.0, 5, 1.0, 1, 0.5, 1.0), 1e-5f);
        assertEquals(0.5f, BeamMath.hitDamage(8.0, 5, 1.0, 2, 0.5, 1.0), 1e-5f);
        assertEquals(3f, BeamMath.hitDamage(8.0, 5, 1.0, 0, 0.5, 1.5), 1e-5f);
    }
}
