package blockgame.physics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import blockgame.physics.GoalArea;
import blockgame.physics.MovingRectangle;

class GoalAreaTest {

	GoalArea goal;
	MovingRectangle rect;

	@BeforeEach
	void setUp() {
		goal = new GoalArea(0, 0, 20, 20, "");
		rect = new MovingRectangle(0, 0, 20, 20);
	}

	private void handleALot(int numTimes) {
		for (int i = 0; i < numTimes; i++) {
			goal.handle(rect);
		}
	}

	@Test
	void player_has_won_after_standing_in_it_for_a_while() {
		assumeFalse(goal.hasWon());

		rect.setControlledByPlayer(true);
		handleALot(500);

		assertTrue(goal.hasWon());
	}

	@Test
	void nothing_happens_for_a_rectangle_not_controlled_by_player() {
		assumeFalse(goal.hasWon());

		handleALot(500);

		assertFalse(goal.hasWon());
	}

	@Test
	void is_not_won_after_being_marked_as_used() {
		assumeFalse(goal.hasWon());

		rect.setControlledByPlayer(true);
		handleALot(500);
		goal.markUsed();

		assertFalse(goal.hasWon());
	}
}
