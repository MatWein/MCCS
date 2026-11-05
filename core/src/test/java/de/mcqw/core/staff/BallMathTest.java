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
    void flightTimeFromRangeAndSpeed() {
        assertEquals(160, BallMath.lifetime(128, 0.8));
    }
}
