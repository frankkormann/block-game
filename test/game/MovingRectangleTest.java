package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import game.MovingRectangle.State;

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
	void state_is_in_air_by_default() {
		assertTrue(rect.getState() == State.IN_AIR);
	}

	@Test
	void comes_with_a_grounding_area_by_default() {
		List<Area> attachments = rect.getAttachments();

		assertTrue(attachments.stream().anyMatch(a -> a instanceof GroundingArea));
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
}
