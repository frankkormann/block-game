package blockgame.physics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RevealingAreaTest {

	RevealingArea area;
	MovingRectangle rect;
	boolean hasCalledConsumer;

	@BeforeEach
	void setUp() {
		area = new RevealingArea(0, 0, 100, 100, null);
		area.setRevealAction(a -> hasCalledConsumer = true);
		rect = new MovingRectangle(0, 0, 100, 100);
		rect.setControlledByPlayer(true);
		hasCalledConsumer = false;
	}

	@Test
	void calls_Consumer_when_rect_enters() {
		area.handle(rect);

		assertTrue(hasCalledConsumer);
	}

	@Test
	void does_not_call_Consumer_when_rect_reenters() {
		area.handle(rect);
		rect.setX(-1000);
		area.handle(rect);
		rect.setX(0);

		hasCalledConsumer = false;
		area.handle(rect);

		assertFalse(hasCalledConsumer);
	}

}
