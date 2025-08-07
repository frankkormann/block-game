package blockgame.physics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import blockgame.gui.MainFrame.Direction;
import blockgame.physics.Rectangle.Colors;

public class SwitchRectangleTest {

	SwitchRectangle rect;

	@BeforeEach
	void setUp() {
		rect = new SwitchRectangle(0, 0, 50, 50, Colors.BLACK, "key");
	}

	@Test
	void becameActive_returns_true_when_it_goes_from_inactive_to_active() {
		rect.setActive(false);
		rect.updateLastPosition();
		rect.setActive(true);

		assertTrue(rect.becameActive());
	}

	@Test
	void becameActive_returns_false_when_always_active() {
		rect.setActive(true);
		rect.updateLastPosition();
		rect.setActive(true);

		assertFalse(rect.becameActive());
	}

	@Test
	void becameActive_returns_false_when_always_inactive() {
		rect.setActive(false);
		rect.updateLastPosition();
		rect.setActive(false);

		assertFalse(rect.becameActive());
	}

	@Test
	void becameActive_returns_false_when_it_goes_from_active_to_inactive() {
		rect.setActive(true);
		rect.updateLastPosition();
		rect.setActive(false);

		assertFalse(rect.becameActive());
	}

	@Nested
	class WhenInactive {

		@BeforeEach
		void setInactive() {
			rect.setActive(false);
		}

		@Test
		void no_gravity() {
			assertFalse(rect.hasGravity());
		}

		@Test
		void did_not_used_to_intersect_any_MovingRectangle_afted_becoming_active() {
			MovingRectangle movingRect = new MovingRectangle(10, 10, 10, 10);
			movingRect.updateLastPosition();
			rect.updateLastPosition();
			rect.setActive(true);

			assertFalse(rect.usedToIntersectX(movingRect));
		}

		@Test
		void can_interact_with_WallRectangles() {
			assertTrue(rect.canInteract(new WallRectangle(0, 0, 0, 0)));
		}

		@Test
		void can_interact_with_SideRectangles() {
			assertTrue(rect.canInteract(
					new SideRectangle(0, 0, 0, 0, Direction.NORTH)));
		}

		@Test
		void can_interact_with_SwitchAreas_with_its_key() {
			assertTrue(rect.canInteract(
					new SwitchArea(0, 0, 0, 0, Colors.RED, rect.getKey())));
		}

		@Test
		void cannot_interact_with_MovingRectangles() {
			assertFalse(rect.canInteract(new MovingRectangle(0, 0, 0, 0)));
		}

		@Test
		void cannot_interact_with_most_Areas() {
			assertFalse(rect.canInteract(new AntigravityArea(0, 0, 0, 0)));
		}

		@Test
		void cannot_interact_with_SwitchAreas_with_different_key() {
			assertFalse(rect.canInteract(new SwitchArea(0, 0, 0, 0, Colors.RED,
					rect.getKey() + "not")));
		}
	}

	@Nested
	class WhenActive {

		@BeforeEach
		void setActive() {
			rect.setActive(true);
		}

		@Test
		void has_gravity() {
			assertTrue(rect.hasGravity());
		}

		@Test
		void can_interact_with_MovingRectangles() {
			assertTrue(rect.canInteract(new MovingRectangle(0, 0, 0, 0)));
		}
	}
}
