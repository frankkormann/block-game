package game;

import java.awt.Color;
import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import game.Area.TranslucentColors;
import game.MovingRectangle.Colors;

/**
 * Translucent {@code Rectangle} which can be toggled visible or not visible.
 * 
 * @author Frank Kormann
 */
public class HintRectangle extends Rectangle {

	private static final int OUTLINE_THICKNESS = 2;
	private static final float OPACITY = 0.5f;
	private static final float BORDER_DARKNESS = 1.2f;

	private boolean visible;

	@JsonCreator
	public HintRectangle(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("color") String color) {
		this(x, y, width, height, getColorEnum(color));
	}

	public HintRectangle(int x, int y, int width, int height,
			Enum<?> colorEnum) {
		super(x, y, width, height, colorEnum, ResizeBehavior.STAY);

		visible = false;
	}

	/**
	 * Return the enum value which corresponds to {@code color}. Searches
	 * {@code Colors} and {@code TranslucentColors}. Returns {@code null} if no
	 * value is found.
	 * 
	 * @param color name to search for
	 * 
	 * @return the enum value
	 */
	private static Enum<?> getColorEnum(String color) {
		try {
			return Enum.valueOf(Colors.class, color);
		}
		catch (IllegalArgumentException ignored) {}
		try {
			return Enum.valueOf(TranslucentColors.class, color);
		}
		catch (IllegalArgumentException ignored) {}

		return null;
	}

	@Override
	public void draw(Graphics g) {
		if (!visible) {
			return;
		}

		g = g.create();

		g.setColor(new Color((int) (getColor().getRed() / BORDER_DARKNESS),
				(int) (getColor().getGreen() / BORDER_DARKNESS),
				(int) (getColor().getBlue() / BORDER_DARKNESS),
				getColor().getAlpha()));

		g.fillRect(getX(), getY(), getWidth(), OUTLINE_THICKNESS);
		g.fillRect(getX(), getY(), OUTLINE_THICKNESS, getHeight());
		g.fillRect(getX(), getY() + getHeight() - OUTLINE_THICKNESS, getWidth(),
				OUTLINE_THICKNESS);
		g.fillRect(getX() + getWidth() - OUTLINE_THICKNESS, getY(),
				OUTLINE_THICKNESS, getHeight());

		g.setColor(new Color(getColor().getRed(), getColor().getGreen(),
				getColor().getBlue(), (int) (getColor().getAlpha() * OPACITY)));

		g.fillRect(getX(), getY(), getWidth(), getHeight());

		g.dispose();
	}

	public void toggleVisible() {
		visible = !visible;
	}

}
