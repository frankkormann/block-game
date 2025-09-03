package blockgame.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import blockgame.input.ValueChangeListener;
import blockgame.physics.Area;
import blockgame.physics.MovingRectangle;

/**
 * {@code Area} which draws an image from a resource as its texture.
 * <p>
 * This has two modes: one where it draws the image as normal, and one where it
 * applies a coloring to the image. In the second mode, the supplied image
 * resource should be grayscale. Whiter sections will become more colored and
 * darker sections will stay darker.
 * <p>
 * An {@code Area} can be set as the "imitated {@code Area}". If there is an
 * imitated {@code Area}, all {@code MovingRectangle}s which enter this will be
 * treated as though they entered an {@code Area} of the imitated {@code Area}'s
 * type. There is no need to set a position or size for the imitated
 * {@code Area}, since it will inherit its position and size from this.
 * 
 * @author Frank Kormann
 */
public class ImageArea extends Area implements ValueChangeListener {

	private BufferedImage baseImage;
	private BufferedImage imageToDraw;
	private Colors color;
	private Area imitatedArea;

	@JsonCreator
	public ImageArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("source") String source) {
		super(x, y, 0, 0, Colors.TRANSPARENT);
		color = null;
		imitatedArea = null;
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

	/**
	 * Sets the {@code Area} which this should act like. Calls to
	 * {@code onEnter}, {@code onExit}, and {@code everyFrame} will be passed to
	 * this {@code Area} and it will have its bounds updated to match this.
	 * 
	 * @param area {@code Area} to imitate
	 */
	public void setImitatedArea(Area area) {
		imitatedArea = area;
		imitatedArea.setX(getX());
		imitatedArea.setY(getY());
		imitatedArea.setWidth(getWidth());
		imitatedArea.setHeight(getHeight());
	}

	/**
	 * Gets the {@code Area} which this is acting like.
	 * 
	 * @return the imitated {@code Area}
	 * 
	 * @see #setImitatedArea(Area)
	 */
	public Area getImitatedArea() {
		return imitatedArea;
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		if (imitatedArea != null) {
			imitatedArea.setX(getX());
		}
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		if (imitatedArea != null) {
			imitatedArea.setY(getY());
		}
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		if (imitatedArea != null) {
			imitatedArea.setWidth(getWidth());
		}
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		if (imitatedArea != null) {
			imitatedArea.setHeight(getHeight());
		}
	}

	/* Area */

	/**
	 * Does nothing unless there is an imitated {@code Area} set. In that case,
	 * calls {@code imitatedArea.onEnter(rect)}.
	 * 
	 * @param rect {@code MovingRectangle} which entered
	 * 
	 * @see #setImitatedArea(Area)
	 */
	@Override
	public void onEnter(MovingRectangle rect) {
		if (imitatedArea != null) {
			imitatedArea.onEnter(rect);
		}
	}

	/**
	 * Does nothing unless there is an imitated {@code Area} set. In that case,
	 * calls {@code imitatedArea.onExit(rect)}.
	 * 
	 * @param rect {@code MovingRectangle} which exited
	 * 
	 * @see #setImitatedArea(Area)
	 */
	@Override
	public void onExit(MovingRectangle rect) {
		if (imitatedArea != null) {
			imitatedArea.onExit(rect);
		}
	}

	/**
	 * Does nothing unless there is an imitated {@code Area} set. In that case,
	 * calls {@code imitatedArea.everyFrame(rect)}.
	 * 
	 * @param rect {@code MovingRectangle} which is inside
	 * 
	 * @see #setImitatedArea(Area)
	 */
	@Override
	public void everyFrame(MovingRectangle rect) {
		if (imitatedArea != null) {
			imitatedArea.everyFrame(rect);
		}
	}

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
