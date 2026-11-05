package de.mcqw.core.staff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BallMathTest {
    @Test
    void blastFallsFromCenterToEdge() {
        assertEquals(1.0, BallMath.blastShare(0, 4, 0.25), 1e-9);
        assertEquals(0.625, BallMath.blastShare(2, 4, 0.25), 1e-9);
    }

    @Test
    void nothingAtOrBeyondTheRadius() {
        assertEquals(0.0, BallMath.blastShare(4, 4, 0.25), 1e-9);
        assertEquals(0.0, BallMath.blastShare(9, 4, 0.25), 1e-9);
        assertEquals(0.0, BallMath.blastShare(0, 0, 0.25), 1e-9);
    }

    @Test
    void chargeGrowsWithHoldingAndStopsAtFull() {
        assertEquals(0.0, BallMath.chargeShare(0, 40), 1e-9);
        assertEquals(0.5, BallMath.chargeShare(20, 40), 1e-9);
        assertEquals(1.0, BallMath.chargeShare(400, 40), 1e-9);
        assertEquals(40.0, BallMath.charged(40, 3, 0), 1e-9);
        assertEquals(80.0, BallMath.charged(40, 3, 0.5), 1e-9);
        assertEquals(120.0, BallMath.charged(40, 3, 1), 1e-9);
    }

    @Test
    void lowEnergyLimitsTheCharge() {
        assertEquals(1.0, BallMath.affordableShare(1.0, 90_000, 30_000, 3), 1e-9);
        assertEquals(0.5, BallMath.affordableShare(1.0, 60_000, 30_000, 3), 1e-9);
        assertEquals(0.0, BallMath.affordableShare(1.0, 30_000, 30_000, 3), 1e-9);
        assertEquals(-1.0, BallMath.affordableShare(1.0, 29_999, 30_000, 3), 1e-9);
        assertEquals(0.25, BallMath.affordableShare(0.25, 90_000, 30_000, 3), 1e-9);
    }

    @Test
    void flightTimeFromRangeAndSpeed() {
        assertEquals(160, BallMath.lifetime(128, 0.8));
    }
}
