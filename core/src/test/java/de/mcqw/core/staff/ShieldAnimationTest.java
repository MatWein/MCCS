package de.mcqw.core.staff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ShieldAnimationTest {
    @Test
    void fadesInAndOut() {
        ShieldAnimation animation = new ShieldAnimation();
        for (int i = 0; i < 30; i++) animation.tick(true, 0);
        assertEquals(1f, animation.shield(1f));
        for (int i = 0; i < 30; i++) animation.tick(false, 0);
        assertTrue(animation.isGone());
    }

    @Test
    void flashesOnlyOnNewHits() {
        ShieldAnimation animation = new ShieldAnimation();
        animation.tick(true, 5);
        assertEquals(0f, animation.flash(0f), "a staff that was hit before does not flash when it becomes active");
        animation.tick(true, 6);
        assertEquals(1f, animation.flash(0f));
    }

    @Test
    void notGoneWhileVisible() {
        ShieldAnimation animation = new ShieldAnimation();
        animation.tick(true, 0);
        assertFalse(animation.isGone());
    }
}
