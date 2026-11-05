package de.mcqw.core.staff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TelekinesisMathTest {
    @Test
    void pullsTowardsThePlaceAndNotTooFast() {
        assertEquals(0.35, TelekinesisMath.pull(1.0), 1e-9);
        assertEquals(1.0, TelekinesisMath.limit(0.3, 0.3, 0.3), 1e-9);
        assertEquals(1.6 / 3.2, TelekinesisMath.limit(3.2, 0, 0), 1e-9);
    }

    @Test
    void impactsNeedSpeedAndHaveAMaximum() {
        assertEquals(0f, TelekinesisMath.impact(0.3, 0.5, 6, 20));
        assertEquals(12f, TelekinesisMath.impact(2.0, 0.5, 6, 20));
        assertEquals(20f, TelekinesisMath.impact(9.0, 0.5, 6, 20));
    }
}
