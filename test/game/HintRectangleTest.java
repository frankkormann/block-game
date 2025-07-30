package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import game.MovingRectangle.Colors;

class HintRectangleTest {

	BufferedImage bufferedImage;
	HintRectangle hint;

	@BeforeEach
	void setUp() {
		SaveManager.setUp(System.getProperty("java.io.tmpdir"));
		Rectangle.setColorMapper(new ColorMapper());
		hint = new HintRectangle(0, 0, 10, 10, Colors.BLUE);
		bufferedImage = new BufferedImage(hint.getWidth(), hint.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
	}

	private void drawHint() {
		Graphics g = bufferedImage.getGraphics();
		hint.draw(g);
		g.dispose();
	}

	private void assertPixelColor(int x, int y, int rgb) {
		try {
			assertEquals(rgb, bufferedImage.getRGB(x, y));
		}
		catch (AssertionError e) {
			System.out.println(
					"Pixel color at " + x + ", " + y + " assertion failed");
			throw e;
		}
	}

	@Test
	void is_not_visible_by_default() {
		drawHint();

		for (int x = bufferedImage.getMinX(); x < bufferedImage.getMinX()
				+ bufferedImage.getWidth(); x++) {
			for (int y = bufferedImage.getMinY(); y < bufferedImage.getMinY()
					+ bufferedImage.getHeight(); y++) {
				assertPixelColor(x, y, new Color(0, 0, 0, 0).getRGB());
			}
		}
	}

	@Test
	void has_translucent_center_when_visible() {
		hint.toggleVisible();
		drawHint();

		int centerRGB = bufferedImage.getRGB(hint.getX() + hint.getWidth() / 2,
				hint.getY() + hint.getHeight() / 2);
		int centerAlpha = new Color(centerRGB, true).getAlpha();
		assertTrue(0 < centerAlpha);
		assertTrue(centerAlpha < 255);
	}
}
