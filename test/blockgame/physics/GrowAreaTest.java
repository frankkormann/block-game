package blockgame.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import blockgame.physics.GrowArea;
import blockgame.physics.MovingRectangle;

class GrowAreaTest {

	private static int X_GROWTH = 1;
	private static int Y_GROWTH = 2;

	GrowArea area;
	MovingRectangle rect;

	int initialX, initialWidth;

	@BeforeEach
	void setUp() {
		area = new GrowArea(0, 0, 100, 100, X_GROWTH, Y_GROWTH);
		rect = new MovingRectangle(0, 0, 20, 20);
	}

	private void setInitialValuesAndHandle() {
		initialX = rect.getX();
		initialWidth = rect.getWidth();
		area.handle(rect);
	}

	@Test
	void rect_grows_in_both_directions_when_fully_inside() {
		rect.setX(50);
		setInitialValuesAndHandle();

		assertEquals(2 * X_GROWTH, rect.getWidth() - initialWidth);
		assertEquals(X_GROWTH, initialX - rect.getX());
	}

	@Test
	void rect_only_grows_to_the_east_when_only_its_east_side_is_inside() {
		rect.setX(-10);
		setInitialValuesAndHandle();

		assertEquals(X_GROWTH, rect.getWidth() - initialWidth);
		assertEquals(0, initialX - rect.getX());
	}

	@Test
	void rect_only_grows_to_the_west_when_only_its_west_side_is_inside() {
		rect.setX(90);
		setInitialValuesAndHandle();

		assertEquals(X_GROWTH, rect.getWidth() - initialWidth);
		assertEquals(X_GROWTH, initialX - rect.getX());
	}

	@Nested
	class VeryHighGrowth {

		@BeforeEach
		void setUp() {
			area = new GrowArea(area.getX(), area.getY(), area.getWidth(),
					area.getHeight(), 1000, 1000);
		}

		@Test
		void rect_does_not_grow_beyond_area_bounds() {
			rect.setX(50);
			area.handle(rect);

			assertEquals(area.getX() + area.getWidth(),
					rect.getX() + rect.getWidth());
			assertEquals(area.getX(), rect.getX());
		}
	}
}
