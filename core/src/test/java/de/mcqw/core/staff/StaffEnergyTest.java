package de.mcqw.core.staff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StaffEnergyTest {
    @Test
    void newStackHasStartCharge() {
        assertEquals(2_000_000, StaffEnergy.stored(StaffEnergy.UNSET, 10_000_000, 0.2));
    }

    @Test
    void savedValueStaysWithinCapacity() {
        assertEquals(10_000_000, StaffEnergy.stored(Integer.MAX_VALUE, 10_000_000, 0.2));
        assertEquals(0, StaffEnergy.stored(0, 10_000_000, 0.2));
    }

    @Test
    void crystalLookFollowsTheCharge() {
        assertEquals(0, StaffEnergy.crystalLook(100, 100));
        assertEquals(0, StaffEnergy.crystalLook(60, 100));
        assertEquals(1, StaffEnergy.crystalLook(59, 100));
        assertEquals(2, StaffEnergy.crystalLook(29, 100));
        assertEquals(3, StaffEnergy.crystalLook(9, 100));
        assertEquals(3, StaffEnergy.crystalLook(0, 100));
    }

    @Test
    void barIsThirteenPixelsWhenFull() {
        assertEquals(13, StaffEnergy.barWidth(100, 100));
        assertEquals(0, StaffEnergy.barWidth(0, 100));
        assertEquals(7, StaffEnergy.barWidth(50, 100));
    }

    @Test
    void acceptsAtMostRoomAndRate() {
        assertEquals(100, StaffEnergy.accepted(900, 1000, 500, 1000));
        assertEquals(500, StaffEnergy.accepted(0, 1000, 500, Long.MAX_VALUE));
        assertEquals(300, StaffEnergy.accepted(0, 1000, 500, 300));
        assertEquals(0, StaffEnergy.accepted(1000, 1000, 500, 300));
    }

    @Test
    void hitCostScalesWithDamageWithoutOverflow() {
        assertEquals(1000, StaffEnergy.hitCost(5f, 200));
        assertEquals(0, StaffEnergy.hitCost(5f, 0));
        assertEquals(Integer.MAX_VALUE, StaffEnergy.hitCost(Float.MAX_VALUE, 200));
    }
}
