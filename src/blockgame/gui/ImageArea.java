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
import blockgame.physics.Area;
import blockgame.physics.MovingRectangle;

/**
 * {@code Area} which draws an image from a resource as its texture. Does not
 * implement {@code onEnter}, {@code onExit}, or {@code everyFrame}.
 * <p>
 * This has two modes: one where it draws the image as normal, and one where it
 * applies a coloring to the image. In the second mode, the supplied image
 * resource should be grayscale. Whiter sections will become more colored and
 * darker sections will stay darker.
 * 
 * @author Frank Kormann
 */
public class ImageArea extends Area implements ValueChangeListener {

	private static ColorMapper colorMapper;

	private BufferedImage baseImage;
	private BufferedImage imageToDraw;
	private Colors color;

	@JsonCreator
	public ImageArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("source") String source) {
		super(x, y, 0, 0, Colors.TRANSPARENT);

		color = null;
		colorMapper.addListener(this);
		try {
			baseImage = ImageIO
					.read(ImageArea.class.getResourceAsStream(source));
			imageToDraw = new BufferedImage(baseImage.getWidth(),
					baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
			setWidth(baseImage.getWidth());
			setHeight(baseImage.getHeight());
			for (int x1 = 0; x1 < baseImage.getWidth(); x1++) {
				for (int y1 = 0; y1 < baseImage.getHeight(); y1++) {
					imageToDraw.setRGB(x1, y1, baseImage.getRGB(x1, y1));
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Failed to read image '" + source + "'", e)
					.setVisible(true);
		}
	}

	public static void setColorMapper(ColorMapper colorMapper) {
		ImageArea.colorMapper = colorMapper;
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(imageToDraw, getX(), getY(), null);
	}

	/**
	 * Applies the coloring to {@code imageToDraw}, taking grayscale RGB data
	 * from {@code baseImage}.
	 */
	private void colorImage() {
		if (color == null) {
			return;
		}
		Color referenceColor = colorMapper.getColor(color);
		if (referenceColor == null) {
			return;
		}
		int refA = referenceColor.getAlpha();
		int refR = referenceColor.getRed();
		int refG = referenceColor.getGreen();
		int refB = referenceColor.getBlue();

		for (int x = 0; x < imageToDraw.getWidth(); x++) {
			for (int y = 0; y < imageToDraw.getHeight(); y++) {
				int rgb = baseImage.getRGB(x, y);
				int α = ((rgb >> 24) & 0xFF) * refA / 0xFF;
				int r = ((rgb >> 16) & 0xFF) * refR / 0xFF;
				int g = ((rgb >> 8) & 0xFF) * refG / 0xFF;
				int b = (rgb & 0xFF) * refB / 0xFF;
				imageToDraw.setRGB(x, y, (α << 24) | (r << 16) | (g << 8) | b);
			}
		}
	}

	public void setColor(Colors color) {
		this.color = color;
		colorImage();
	}

	/* Area */

	@Override
	protected void onEnter(MovingRectangle rect) {}

	@Override
	protected void onExit(MovingRectangle rect) {}

	@Override
	protected void everyFrame(MovingRectangle rect) {}

	/* ValueChangeListener */

	@Override
	public void valueChanged(Enum<?> key, Object newValue) {
		if (key == color) {
			colorImage();
		}
	}

	@Override
	public void valueRemoved(Enum<?> key) {}

}
