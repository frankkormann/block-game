package blockgame.physics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import blockgame.gui.MainFrame.Direction;
import blockgame.physics.Rectangle.ResizeBehavior;

class SideRectangleTest {

	SideRectangle side;

	@BeforeEach
	void setUp() {
		side = new SideRectangle(0, 10, 1000, 1, null);
	}

	@Test
	void is_acting_like_a_wall_by_default() {
		assertTrue(side.isActingLikeWall());
	}

	@Test
	void can_not_interact_with_other_SideRectangles() {
		SideRectangle other = new SideRectangle(0, 0, 0, 0, Direction.EAST);

		assertFalse(side.canInteract(other));
	}

	@Test
	void can_not_interact_with_ResizeBehavior_STAY_rectangle() {
		Rectangle other = new WallRectangle(0, 0, 0, 0, ResizeBehavior.STAY);

		assertFalse(side.canInteract(other));
	}

	@Nested
	class NorthSouth {
		SideRectangle northSide, southSide;

		@BeforeEach
		void setUp() {
			northSide = new SideRectangle(0, 0, 0, 0, Direction.NORTH);
			southSide = new SideRectangle(0, 0, 0, 0, Direction.SOUTH);
		}

		@Test
		void can_not_interact_with_PREVENT_X_rectangles() {
			WallRectangle preventXWall = new WallRectangle(0, 0, 0, 0,
					ResizeBehavior.PREVENT_X);

			assertFalse(northSide.canInteract(preventXWall));
			assertFalse(southSide.canInteract(preventXWall));
		}

		@Test
		void can_interact_with_PREVENT_Y_rectangles() {
			WallRectangle preventYWall = new WallRectangle(0, 0, 0, 0,
					ResizeBehavior.PREVENT_Y);

			assertTrue(northSide.canInteract(preventYWall));
			assertTrue(southSide.canInteract(preventYWall));
		}

		@Test
		void can_not_push_x() {
			assertFalse(northSide.canPushX());
			assertFalse(southSide.canPushX());
		}

		@Test
		void can_push_y() {
			assertTrue(northSide.canPushY());
			assertTrue(southSide.canPushY());
		}
	}

	@Nested
	class WestEast {
		SideRectangle westSide, eastSide;

		@BeforeEach
		void setUp() {
			westSide = new SideRectangle(0, 0, 0, 0, Direction.WEST);
			eastSide = new SideRectangle(0, 0, 0, 0, Direction.EAST);
		}

		@Test
		void can_interact_with_PREVENT_X_rectangles() {
			WallRectangle preventXWall = new WallRectangle(0, 0, 0, 0,
					ResizeBehavior.PREVENT_X);

			assertTrue(westSide.canInteract(preventXWall));
			assertTrue(eastSide.canInteract(preventXWall));
		}

		@Test
		void can_not_interact_with_PREVENT_Y_rectangles() {
			WallRectangle preventYWall = new WallRectangle(0, 0, 0, 0,
					ResizeBehavior.PREVENT_Y);

			assertFalse(westSide.canInteract(preventYWall));
			assertFalse(eastSide.canInteract(preventYWall));
		}

		@Test
		void can_push_x() {
			assertTrue(westSide.canPushX());
			assertTrue(eastSide.canPushX());
		}

		@Test
		void can_not_push_y() {
			assertFalse(westSide.canPushY());
			assertFalse(eastSide.canPushY());
		}
	}
}
