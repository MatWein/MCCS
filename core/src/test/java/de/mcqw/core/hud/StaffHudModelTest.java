package de.mcqw.core.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class StaffHudModelTest {
    @Test
    void showsDrainAndRemainingTimeOnlyWhileUsingEnergy() {
        List<HudLine> running = StaffHudModel.lines(new StaffHudModel.State(600_000, 1_000_000, true, false, 500, true), 0);
        String drain = ((HudText.Literal) running.get(4).parts().get(0)).text();
        assertTrue(drain.contains("-500 FE/t"), drain);
        assertTrue(drain.contains("~1 min"), drain);
        assertEquals(4, StaffHudModel.lines(new StaffHudModel.State(600_000, 1_000_000, false, false, 0, true), 0).size());
    }

    @Test
    void lowEnergyBlinksOnlyWhenEnergyMatters() {
        assertEquals(HudColors.RED, StaffHudModel.lines(new StaffHudModel.State(5, 100, false, false, 0, true), 0).get(1).color());
        assertEquals(HudColors.DARK_RED, StaffHudModel.lines(new StaffHudModel.State(5, 100, false, false, 0, true), 10).get(1).color());
        assertEquals(HudColors.TEAL, StaffHudModel.lines(new StaffHudModel.State(5, 100, false, false, 0, false), 0).get(1).color());
    }
}
