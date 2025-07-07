package game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShrinkAreaTest {

	private static int X_SHRINK = 1;
	private static int Y_SHRINK = 2;

	ShrinkArea area;
	MovingRectangle rect;

	int initialX, initialWidth;

	@BeforeEach
	void setUp() {
		area = new ShrinkArea(0, 0, 20, 10, X_SHRINK, Y_SHRINK);
		rect = new MovingRectangle(0, 0, 200, 20);
	}

	private void setInitialValuesAndHandle() {
		initialX = rect.getX();
		initialWidth = rect.getWidth();
		area.handle(rect);
	}

	@Test
	void rect_shrinks_in_both_directions_when_beyond_its_bounds_on_both_sides() {
		rect.setX(-20);
		setInitialValuesAndHandle();

		assertEquals(2 * X_SHRINK, initialWidth - rect.getWidth());
		assertEquals(X_SHRINK, rect.getX() - initialX);
	}

	@Test
	void rect_only_shrinks_from_the_east_when_only_its_west_side_is_inside() {
		rect.setX(10);
		setInitialValuesAndHandle();

		assertEquals(X_SHRINK, initialWidth - rect.getWidth());
		assertEquals(0, rect.getX() - initialX);
	}

	@Test
	void rect_only_shrinks_from_the_wast_when_only_its_east_side_is_inside() {
		rect.setX(-190);
		setInitialValuesAndHandle();

		assertEquals(X_SHRINK, initialWidth - rect.getWidth());
		assertEquals(X_SHRINK, rect.getX() - initialX);
	}
}
