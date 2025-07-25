package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mocks.DrawableMock;

class DrawingPaneTest {

	DrawingPane drawingPane;
	BufferedImage bufferedImage;
	DrawableMock drawable;

	@BeforeEach
	void setUp() {
		drawingPane = new DrawingPane();
		bufferedImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
		drawable = new DrawableMock(10, 10, 10, 10, Color.GREEN);

		drawingPane.add(drawable, 0);
	}

	@Test
	void both_offsets_are_zero_by_default() {
		assertEquals(0, drawingPane.getXOffset());
		assertEquals(0, drawingPane.getYOffset());
	}

	@Test
	void background_color_is_white() {
		assertEquals(Color.WHITE, drawingPane.getBackground());
	}

	private void draw() {
		Graphics g = bufferedImage.getGraphics();
		drawingPane.paintComponent(g);
		g.dispose();
	}

	private void assertCornersAreColored(int x, int y, int width, int height) {
		int blankRGB = Color.WHITE.getRGB();
		assertNotEquals(blankRGB, bufferedImage.getRGB(x, y));
		assertNotEquals(blankRGB, bufferedImage.getRGB(x + width - 1, y));
		assertNotEquals(blankRGB, bufferedImage.getRGB(x, y + height - 1));
		assertNotEquals(blankRGB,
				bufferedImage.getRGB(x + width - 1, y + height - 1));
	}

	@Test
	void rectangle_is_drawn_at_x_y_when_offsets_are_zero() {
		draw();

		assertCornersAreColored(drawable.x, drawable.y, drawable.width,
				drawable.height);
	}

	@Test
	void drawable_is_drawn_offset_when_offsets_are_not_zero() {
		int xOffset = 3, yOffset = 2;
		drawingPane.setXOffset(xOffset);
		drawingPane.setYOffset(yOffset);
		draw();

		assertCornersAreColored(drawable.x - xOffset, drawable.y - yOffset,
				drawable.width, drawable.height);
	}

	@Test
	void drawables_which_overlap_are_drawn_in_correct_order() {
		Rectangle other = new WallRectangle(15, 15, 10, 10);
		drawingPane.add(other, 1);
		draw();

		int rectRGB = bufferedImage.getRGB(drawable.x, drawable.y);
		int otherRGB = bufferedImage.getRGB(other.getX() + other.getWidth() - 1,
				other.getY() + other.getHeight() - 1);

		assertNotEquals(rectRGB,
				bufferedImage.getRGB(drawable.x + drawable.width - 1,
						drawable.y + drawable.height - 1));
		assertEquals(otherRGB,
				bufferedImage.getRGB(other.getX(), other.getY()));
	}
}
