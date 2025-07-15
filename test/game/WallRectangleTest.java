package game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import game.Rectangle.ResizeBehavior;

class WallRectangleTest {

	BufferedImage bufferedImage;
	WallRectangle wall;

	@BeforeEach
	void setUp() {
		wall = new WallRectangle(0, 0, 10, 10);
		bufferedImage = new BufferedImage(wall.getWidth(), wall.getHeight(),
				BufferedImage.TYPE_INT_RGB);
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
	void PREVENT_X_draws_darker_border_along_side_edges() {
		wall.setResizeBehavior(ResizeBehavior.PREVENT_X);
		Graphics g = bufferedImage.getGraphics();
		wall.draw(g);
		g.dispose();

		for (int y = wall.getY(); y < wall.getY() + wall.getHeight(); y++) {
			assertPixelColor(wall.getX(), y,
					WallRectangle.PREVENT_COLOR.getRGB());
			assertPixelColor(wall.getX() + wall.getWidth() - 1, y,
					WallRectangle.PREVENT_COLOR.getRGB());
		}
	}

	@Test
	void PREVENT_Y_draws_darker_border_along_top_and_bottom_edges() {
		wall.setResizeBehavior(ResizeBehavior.PREVENT_Y);
		Graphics g = bufferedImage.getGraphics();
		wall.draw(g);
		g.dispose();

		for (int x = wall.getX(); x < wall.getX() + wall.getWidth(); x++) {
			assertPixelColor(x, wall.getY(),
					WallRectangle.PREVENT_COLOR.getRGB());
			assertPixelColor(x, wall.getY() + wall.getHeight() - 1,
					WallRectangle.PREVENT_COLOR.getRGB());
		}
	}
}
