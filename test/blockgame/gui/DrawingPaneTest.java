package blockgame.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import blockgame.util.SaveManager;
import mocks.DrawableMock;

class DrawingPaneTest {

	DrawingPane drawingPane;
	BufferedImage bufferedImage;
	DrawableMock drawable;

	@BeforeEach
	void setUp(@TempDir Path dir) {
		SaveManager.setDirectory(dir.toString());
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
		DrawableMock other = new DrawableMock(15, 15, 10, 10, Color.BLACK);
		drawingPane.add(other, 1);
		draw();

		int rectRGB = bufferedImage.getRGB(drawable.x, drawable.y);
		int otherRGB = bufferedImage.getRGB(other.x + other.width - 1,
				other.y + other.height - 1);

		assertNotEquals(rectRGB,
				bufferedImage.getRGB(drawable.x + drawable.width - 1,
						drawable.y + drawable.height - 1));
		assertEquals(otherRGB, bufferedImage.getRGB(other.x, other.y));
	}

	@Test
	void scaling_is_applied_properly() {
		drawingPane.setScale(0.5f);
		draw();

		assertCornersAreColored(drawable.x / 2, drawable.y / 2,
				drawable.width / 2, drawable.height / 2);
	}
}
