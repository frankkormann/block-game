package game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AntigravityAreaTest {

	AntigravityArea area;
	MovingRectangle rect;

	@BeforeEach
	void setUp() throws Exception {
		area = new AntigravityArea(0, 0, 20, 20);
		rect = new MovingRectangle(0, 0, 20, 20);
	}

	@Test
	void gravity_is_disabled_when_rect_enters() {
		assumeTrue(rect.hasGravity());

		area.handle(rect);

		assertFalse(rect.hasGravity());
	}

	@Test
	void gravity_is_enabled_again_when_rect_leaves() {
		area.handle(rect);
		rect.setX(2000);
		area.handle(rect);

		assertTrue(rect.hasGravity());
	}
}
