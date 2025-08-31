package blockgame.physics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JumpAreaTest {

	JumpArea area;
	MovingRectangle rect;

	@BeforeEach
	void setUp() {
		area = new JumpArea(0, 0, 20);
		rect = new MovingRectangle(0, 0, 20, 20);
	}

	@Test
	void rect_can_jump_when_withinn() {
		assumeFalse(rect.canJump());

		area.handle(rect);

		assertTrue(rect.canJump());
	}

	@Test
	void rect_cannot_jump_when_it_leaves() {
		area.handle(rect);
		rect.setY(500);
		area.handle(rect);

		assertFalse(rect.canJump());
	}

	@Test
	void rect_can_jump_when_it_is_in_multiple_areas_and_it_only_leaves_one_of_them() {
		JumpArea secondArea = new JumpArea(0, area.getHeight(), 20);
		area.handle(rect);
		secondArea.handle(rect);

		rect.setY(secondArea.getY());
		area.handle(rect);
		secondArea.handle(rect);

		assertTrue(rect.canJump());
	}
}
