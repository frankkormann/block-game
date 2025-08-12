package blockgame.physics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import blockgame.physics.Rectangle.Colors;

public class SwitchAreaTest {

	SwitchArea area;
	SwitchController controller;
	SwitchRectangle rect;

	@BeforeEach
	void setUp() {
		area = new SwitchArea(0, 0, 50, 50, Colors.BLACK, "key");
		rect = new SwitchRectangle(100, 100, 50, 50, Colors.BLACK, "key");
		controller = new SwitchController();
		area.setController(controller);
		controller.addSwitchRectangle(rect);
		rect.setActive(false);
	}

	@Test
	void children_become_active_when_something_enters() {
		area.handle(new MovingRectangle(0, 0, 10, 10));

		assertTrue(rect.isActive());
	}

	@Test
	void children_become_inactive_when_something_leaves() {
		MovingRectangle movingRect = new MovingRectangle(0, 0, 10, 10);
		area.handle(movingRect);
		movingRect.setX(100);
		area.handle(movingRect);

		assertFalse(rect.isActive());
	}

	@Test
	void children_stay_active_if_one_of_two_inside_leave() {
		MovingRectangle first = new MovingRectangle(0, 0, 10, 10);
		MovingRectangle second = new MovingRectangle(0, 0, 10, 10);
		area.handle(first);
		area.handle(second);
		first.setX(100);
		area.handle(first);
		area.handle(second);

		assertTrue(rect.isActive());
	}

	@Test
	void children_become_inactive_if_all_leave() {
		MovingRectangle first = new MovingRectangle(0, 0, 10, 10);
		MovingRectangle second = new MovingRectangle(0, 0, 10, 10);
		area.handle(first);
		area.handle(second);
		first.setX(100);
		second.setX(100);
		area.handle(first);
		area.handle(second);

		assertFalse(rect.isActive());
	}
}
