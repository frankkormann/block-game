package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import game.MovingRectangle.State;

class GroundingAreaTest {

	GroundingArea area;
	MovingRectangle rect;

	@BeforeEach
	void setUp() {
		area = new GroundingArea(0, 0, 20);
		rect = new MovingRectangle(0, 0, 20, 20);
	}

	@Test
	void rect_state_is_ON_GROUND_when_within() {
		assumeFalse(rect.getState() == State.ON_GROUND);

		area.handle(rect);

		assertEquals(State.ON_GROUND, rect.getState());
	}

	@Test
	void rect_state_is_IN_AIR_when_it_leaves() {
		area.handle(rect);
		rect.setY(500);
		area.handle(rect);

		assertEquals(State.IN_AIR, rect.getState());
	}

	@Test
	void rect_state_is_ON_GROUND_when_it_is_in_multiple_areas_and_it_only_leaves_one_of_them() {
		GroundingArea secondArea = new GroundingArea(0, area.getHeight(), 20);
		area.handle(rect);
		secondArea.handle(rect);

		rect.setY(secondArea.getY());
		area.handle(rect);
		secondArea.handle(rect);

		assertEquals(State.ON_GROUND, rect.getState());
	}
}
