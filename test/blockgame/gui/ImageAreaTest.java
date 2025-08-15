package blockgame.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import blockgame.input.ColorMapper;
import blockgame.physics.GrowArea;
import blockgame.physics.MovingRectangle;
import blockgame.physics.Rectangle.Colors;

public class ImageAreaTest {

	private static final String REFERENCE_IMAGE_RESOURCE = "/title_screen_text.png";

	static ColorMapper colorMapper;
	ImageArea imgArea;

	@BeforeAll
	static void setUpColorMapper() {
		colorMapper = new ColorMapper();
		ImageArea.setColorMapper(colorMapper);
	}

	@BeforeEach
	void setUp() {
		imgArea = new ImageArea(0, 0, REFERENCE_IMAGE_RESOURCE);
	}

	private void assertImages(BufferedImage expected, BufferedImage actual) {
		assertEquals(expected.getWidth(), actual.getWidth());
		assertEquals(expected.getHeight(), actual.getHeight());

		for (int x = 0; x < expected.getWidth(); x++) {
			for (int y = 0; y < expected.getHeight(); y++) {
				int actualRGB = actual.getRGB(x, y);
				int expectedRGB = expected.getRGB(x, y);
				// Allow +/- 1 point in ARGB values
				try {
					assertEquals((actualRGB << 24) & 0xFF,
							(expectedRGB << 24) & 0xFF, 1);
					assertEquals((actualRGB << 16) & 0xFF,
							(expectedRGB << 16) & 0xFF, 1);
					assertEquals((actualRGB << 8) & 0xFF,
							(expectedRGB << 8) & 0xFF, 1);
					assertEquals(actualRGB & 0xFF, expectedRGB & 0xFF, 1);
				}
				catch (AssertionError e) {
					System.err.println("Failed at pixel " + x + ", " + y);
					throw e;
				}
			}
		}
	}

	@Test
	void draws_grayscale_image_correctly() throws IOException {
		BufferedImage areaImg = new BufferedImage(imgArea.getWidth(),
				imgArea.getHeight(), BufferedImage.TYPE_INT_ARGB);
		imgArea.draw(areaImg.getGraphics());
		BufferedImage referenceImg = ImageIO
				.read(getClass().getResourceAsStream(REFERENCE_IMAGE_RESOURCE));

		assertImages(referenceImg, areaImg);
	}

	private BufferedImage applyColorMask(BufferedImage source, int mask) {
		BufferedImage maskedImg = new BufferedImage(source.getWidth(),
				source.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < maskedImg.getWidth(); x++) {
			for (int y = 0; y < maskedImg.getHeight(); y++) {
				maskedImg.setRGB(x, y, source.getRGB(x, y) & mask);
			}
		}
		return maskedImg;
	}

	@Test
	void draws_colored_image_correctly() throws IOException {
		colorMapper.setColor(Colors.BLUE, new Color(0, 0, 255));
		imgArea.setColor(Colors.BLUE);

		BufferedImage areaImg = new BufferedImage(imgArea.getWidth(),
				imgArea.getHeight(), BufferedImage.TYPE_INT_ARGB);
		imgArea.draw(areaImg.getGraphics());

		BufferedImage readImg = ImageIO
				.read(getClass().getResourceAsStream(REFERENCE_IMAGE_RESOURCE));

		assertImages(applyColorMask(readImg, 0xFF0000FF), areaImg);
	}

	@Test
	void updates_color_when_ColorMapper_changes() throws IOException {
		colorMapper.setColor(Colors.BLUE, new Color(0, 0, 255));
		imgArea.setColor(Colors.BLUE);

		BufferedImage areaImg = new BufferedImage(imgArea.getWidth(),
				imgArea.getHeight(), BufferedImage.TYPE_INT_ARGB);
		colorMapper.setColor(Colors.BLUE, new Color(0, 255, 0));
		imgArea.draw(areaImg.getGraphics());

		BufferedImage readImg = ImageIO
				.read(getClass().getResourceAsStream(REFERENCE_IMAGE_RESOURCE));

		assertImages(applyColorMask(readImg, 0xFF00FF00), areaImg);
	}

	@Test
	void imitates_an_area() {
		MovingRectangle rect = new MovingRectangle(0, 0, 100, 100);
		imgArea.setImitatedArea(new GrowArea(0, 0, 0, 0, 1, 1));
		imgArea.handle(rect);

		assertEquals(101, rect.getHeight());
		assertEquals(101, rect.getWidth());
	}
}
