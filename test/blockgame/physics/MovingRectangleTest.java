package blockgame.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MovingRectangleTest {

	MovingRectangle rect;

	@BeforeEach
	void setUp() {
		rect = new MovingRectangle(10, 10, 10, 10);
	}

	@Test
	void has_gravity_by_default() {
		assertTrue(rect.hasGravity());
	}

	@Test
	void cannot_jump_by_default() {
		assertFalse(rect.canJump());
	}

	@Test
	void comes_with_a_GroundingArea_by_default() {
		List<Area> attachments = rect.getAttachments();

		assertTrue(attachments.stream().anyMatch(a -> a instanceof JumpArea));
	}

	@Test
	void moveVelocity_moves_by_velocity_amounts() {
		int initialX = rect.getX(), initialY = rect.getY();
		rect.setXVelocity(5);
		rect.setYVelocity(5);
		rect.moveVelocity();

		assertEquals(initialX + rect.getXVelocity(), rect.getX());
		assertEquals(initialY + rect.getYVelocity(), rect.getY());
	}

	@Test
	void moveVelocity_clamps_to_max_speeds() {
		int initialVelocity = 100000;
		rect.setXVelocity(initialVelocity);
		rect.setYVelocity(initialVelocity);
		rect.moveVelocity();

		assertTrue(rect.getXVelocity() < initialVelocity);
		assertTrue(rect.getYVelocity() < initialVelocity);
	}

	@Test
	void moveCollision_moves_by_collision_amounts() {
		int collisionX = 5, collisionY = 7;
		int initialX = rect.getX(), initialY = rect.getY();
		rect.moveCollision(collisionX, collisionY);

		assertEquals(initialX + collisionX, rect.getX());
		assertEquals(initialY + collisionY, rect.getY());
	}

	@Test
	void moveCollision_sets_x_velocity_to_zero_when_it_moves_against_it() {
		rect.setXVelocity(5);
		rect.setYVelocity(5);
		rect.moveCollision(-2, 7);

		assertEquals(0, rect.getXVelocity());
		assertEquals(5, rect.getYVelocity());
	}

	@Test
	void moveCollision_sets_y_velocity_to_zero_when_it_moves_against_it() {
		rect.setXVelocity(5);
		rect.setYVelocity(5);
		rect.moveCollision(2, -7);

		assertEquals(5, rect.getXVelocity());
		assertEquals(0, rect.getYVelocity());
	}

	@Test
	void adding_to_left_changes_leftWidthChange() {
		rect.changeWidth(10, true);

		assertEquals(10, rect.getLeftWidthChange());
	}

	@Test
	void adding_to_right_does_not_change_leftWidthChange() {
		rect.changeWidth(10, false);

		assertEquals(0, rect.getLeftWidthChange());
	}

	@Test
	void adding_to_top_changes_topHeightChange() {
		rect.changeHeight(10, true);

		assertEquals(10, rect.getTopHeightChange());
	}

	@Test
	void adding_to_bottom_does_not_change_topHeightChange() {
		rect.changeWidth(10, false);

		assertEquals(0, rect.getTopHeightChange());
	}

	@Nested
	class Intersections {

		MovingRectangle other;

		@BeforeEach
		void createSecondRectangle() {
			other = new MovingRectangle(rect.getX(), rect.getY(),
					rect.getWidth(), rect.getHeight());
		}

		void updateLastPositions() {
			rect.updateLastPosition();
			other.updateLastPosition();
		}

		@Test
		void used_to_intersect_x() {
			other.setX(other.getX() - 5);
			updateLastPositions();

			assertTrue(rect.usedToIntersectX(other));
			assertTrue(other.usedToIntersectX(rect));
		}

		@Test
		void used_to_not_intersect_x() {
			other.setX(other.getX() - 2 * other.getWidth());
			updateLastPositions();

			assertFalse(rect.usedToIntersectX(other));
			assertFalse(other.usedToIntersectX(rect));
		}

		@Test
		void used_to_intersect_y() {
			other.setY(other.getY() - 5);
			updateLastPositions();

			assertTrue(rect.usedToIntersectY(other));
			assertTrue(other.usedToIntersectY(rect));
		}

		@Test
		void used_to_not_intersect_y() {
			other.setY(other.getY() - 2 * other.getHeight());
			updateLastPositions();

			assertFalse(rect.usedToIntersectY(other));
			assertFalse(other.usedToIntersectY(rect));
		}

		@Test
		void completely_overlapping_used_to_intersect_both() {
			updateLastPositions();

			assertTrue(rect.usedToIntersectX(other));
			assertTrue(other.usedToIntersectX(rect));

			assertTrue(rect.usedToIntersectY(other));
			assertTrue(other.usedToIntersectY(rect));
		}
	}
}
