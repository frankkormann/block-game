package blockgame.physics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import blockgame.physics.Rectangle.Colors;

public class SwitchControllerTest {

	SwitchController controller;
	SwitchRectangle rect;

	@BeforeEach
	void setUp() {
		controller = new SwitchController();
		rect = new SwitchRectangle(0, 0, 0, 0, Colors.BLACK, "key");
		controller.addSwitchRectangle(rect);
	}

	@Test
	void rect_becomes_active_when_an_area_is_activated() {
		controller.areaActivated();

		assertTrue(rect.isActive());
	}

	@Test
	void rect_becomes_inactive_when_an_area_is_deactivated() {
		controller.areaActivated();
		controller.areaDeactivated();

		assertFalse(rect.isActive());
	}

	@Test
	void rect_stays_active_if_one_of_two_active_areas_is_deactivated() {
		controller.areaActivated();
		controller.areaActivated();
		controller.areaDeactivated();

		assertTrue(rect.isActive());
	}

	@Test
	void rect_becomes_inactive_when_all_areas_are_deactivated() {
		controller.areaActivated();
		controller.areaActivated();
		controller.areaDeactivated();
		controller.areaDeactivated();

		assertFalse(rect.isActive());
	}
}
