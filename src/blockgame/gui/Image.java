package blockgame.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import blockgame.input.ColorMapper;
import blockgame.input.ValueChangeListener;
import blockgame.physics.Rectangle.Colors;

/**
 * {@code Drawable} which draws an image from a resource.
 * <p>
 * This has two modes: one where it draws the image as normal, and one where it
 * applies a coloring to the image. In the second mode, the supplied image
 * resource should be grayscale. Whiter sections will become more colored and
 * darker sections will stay darker.
 * 
 * @author Frank Kormann
 */
public class Image implements Drawable, ValueChangeListener {

	private static ColorMapper colorMapper;

	private int x, y;
	private BufferedImage baseImage;
	private BufferedImage image;
	private Colors color;

	@JsonCreator
	public Image(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("source") String source) {
		this.x = x;
		this.y = y;
		color = null;
		colorMapper.addListener(this);
		try {
			baseImage = ImageIO.read(Image.class.getResourceAsStream(source));
			image = ImageIO.read(Image.class.getResourceAsStream(source));
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Failed to read image '" + source + "'", e)
					.setVisible(true);
		}
	}

	public static void setColorMapper(ColorMapper colorMapper) {
		Image.colorMapper = colorMapper;
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(image, x, y, null);
	}

	public void setColor(Colors color) {
		this.color = color;
		colorImage();
	}

	/**
	 * Applies the coloring to {@code image}, taking grayscale RGB data from
	 * {@code baseImage}.
	 */
	private void colorImage() {
		if (color == null) {
			return;
		}
		Color referenceColor = colorMapper.getColor(color);
		if (referenceColor == null) {
			return;
		}
		int refR = referenceColor.getRed();
		int refG = referenceColor.getGreen();
		int refB = referenceColor.getBlue();

		for (int x1 = 0; x1 < image.getWidth(); x1++) {
			for (int y1 = 0; y1 < image.getHeight(); y1++) {
				int rgb = baseImage.getRGB(x1, y1);
				int α = (rgb >> 24) & 0xff;
				int r = ((rgb >> 16) & 0xff) * refR / 0xff;
				int g = ((rgb >> 8) & 0xff) * refG / 0xff;
				int b = (rgb & 0xff) * refB / 0xff;
				image.setRGB(x1, y1, (α << 24) + (r << 16) + (g << 8) + b);
			}
		}
	}

	@Override
	public void valueChanged(Enum<?> key, Object newValue) {
		if (key == color) {
			colorImage();
		}
	}

	@Override
	public void valueRemoved(Enum<?> key) {}

}
