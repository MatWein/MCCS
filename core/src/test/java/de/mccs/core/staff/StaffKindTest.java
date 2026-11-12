package de.mccs.core.staff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StaffKindTest {
    @Test
    void highEnergyStaffStoresTenTimesAsMuch() {
        assertEquals(5_000_000, StaffKind.NORMAL.capacity(5_000_000));
        assertEquals(50_000_000, StaffKind.HIGH_ENERGY.capacity(5_000_000));
    }

    @Test
    void capacityDoesNotOverflow() {
        assertEquals(Integer.MAX_VALUE, StaffKind.HIGH_ENERGY.capacity(Integer.MAX_VALUE));
    }
}
