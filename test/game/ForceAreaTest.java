package game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ForceAreaTest {

	private static int X_FORCE = 1;
	private static int Y_FORCE = 2;

	ForceArea area;
	MovingRectangle rect;

	@BeforeEach
	void setUp() throws Exception {
		area = new ForceArea(0, 0, 20, 20, X_FORCE, Y_FORCE);
		rect = new MovingRectangle(0, 0, 20, 20);
	}

	@Test
	void correct_force_is_applied() {
		rect.setXVelocity(0);
		rect.setYVelocity(0);
		area.handle(rect);

		assertEquals(X_FORCE, rect.getXVelocity());
		assertEquals(Y_FORCE, rect.getYVelocity());
	}
}
