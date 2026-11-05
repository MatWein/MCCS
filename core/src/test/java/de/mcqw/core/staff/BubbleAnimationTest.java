package de.mcqw.core.staff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BubbleAnimationTest {
    @Test
    void fadesInAndOut() {
        BubbleAnimation animation = new BubbleAnimation();
        for (int i = 0; i < 30; i++) animation.tick(true, 0, false);
        assertEquals(1f, animation.shield(1f));
        for (int i = 0; i < 30; i++) animation.tick(false, 0, false);
        assertTrue(animation.isGone());
    }

    @Test
    void flashesOnlyOnNewHits() {
        BubbleAnimation animation = new BubbleAnimation();
        animation.tick(true, 5, false);
        assertEquals(0f, animation.flash(0f), "a staff that was hit before does not flash when it becomes active");
        animation.tick(true, 6, false);
        assertEquals(1f, animation.flash(0f));
    }

    @Test
    void notGoneWhileVisible() {
        BubbleAnimation animation = new BubbleAnimation();
        animation.tick(false, 0, true);
        assertFalse(animation.isGone());
    }

    @Test
    void cloakHidesAfterItsFadePassedHalfway() {
        BubbleAnimation animation = new BubbleAnimation();
        for (int i = 0; i < BubbleAnimation.CLOAK_HIDE_TICKS - 1; i++) animation.tick(false, 0, true);
        assertFalse(animation.isHidden());
        animation.tick(false, 0, true);
        animation.tick(false, 0, true);
        assertTrue(animation.isHidden());
    }
}
