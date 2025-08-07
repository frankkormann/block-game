package blockgame.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import blockgame.input.ColorMapper;
import blockgame.mocks.AccessibleArea;
import blockgame.physics.Rectangle.Colors;
import blockgame.util.SaveManager;

class AreaTest {

	BufferedImage bufferedImage;
	AccessibleArea area;

	@BeforeEach
	void setUp(@TempDir Path dir) {
		SaveManager.setDirectory(dir.toString());
		Rectangle.setColorMapper(new ColorMapper());
		area = new AccessibleArea(0, 0, 10, 10, Colors.BLACK);
		bufferedImage = new BufferedImage(area.getWidth(), area.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
	}

	private void drawArea() {
		Graphics g = bufferedImage.getGraphics();
		area.draw(g);
		g.dispose();
	}

	@Test
	void no_border_when_drawn() {
		drawArea();

		int edgeColor = bufferedImage.getRGB(area.getX(), area.getY());
		int centerColor = bufferedImage.getRGB(
				area.getX() + area.getWidth() / 2,
				area.getY() + area.getHeight() / 2);

		assertEquals(edgeColor, centerColor);
	}

	@Nested
	class WithMovingRectangle {

		MovingRectangle rect;

		@BeforeEach
		void setUpRectangle() {
			rect = new MovingRectangle(0, 0, 10, 10);
		}

		@Test
		void recognizes_rect_has_entered() {
			area.handle(rect);

			assertTrue(area.hasEntered);
		}

		@Test
		void does_not_recognize_rect_has_entered_when_rect_is_outside() {
			rect.setX(-100);
			area.handle(rect);

			assertFalse(area.hasEntered);
		}

		@Test
		void recognizes_rect_has_exited() {
			area.handle(rect);
			rect.setX(-100);
			area.handle(rect);

			assertTrue(area.hasExited);
		}

		@Test
		void does_not_recognize_rect_has_exited_when_rect_is_still_inside() {
			area.handle(rect);
			area.handle(rect);

			assertFalse(area.hasExited);
		}

		@Test
		void everyFrame_is_called_the_correct_number_of_times() {
			int numberOfTimes = 100;
			for (int i = 0; i < numberOfTimes; i++) {
				area.handle(rect);
			}

			assertEquals(numberOfTimes, area.callsToEveryframe.get(rect));
		}
	}
}
