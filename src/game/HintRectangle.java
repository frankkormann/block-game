package game;

import java.awt.Color;
import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HintRectangle extends Rectangle {

	private static final int OUTLINE_THICKNESS = 2;
	private static final float OPACITY = 0.5f;
	private static final float BORDER_DARKNESS = 1.2f;

	private boolean visible;

	public HintRectangle(int x, int y, int width, int height, Color color) {
		super(x, y, width, height, color, Rectangle.ResizeBehavior.STAY);
	}

	@JsonCreator
	public HintRectangle(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width, @JsonProperty("height") int height,
			@JsonProperty("color") MovingRectangle.Colors color) {
		this(x, y, width, height, color.color);

		visible = false;
	}

	@Override
	public void draw(Graphics g) {
		if (!visible) {
			return;
		}

		g.setColor(new Color((int) (getColor().getRed() / BORDER_DARKNESS),
				(int) (getColor().getGreen() / BORDER_DARKNESS),
				(int) (getColor().getBlue() / BORDER_DARKNESS)));

		g.fillRect(getX(), getY(), getWidth(), OUTLINE_THICKNESS);
		g.fillRect(getX(), getY(), OUTLINE_THICKNESS, getHeight());
		g.fillRect(getX(), getY() + getHeight() - OUTLINE_THICKNESS, getWidth(),
				OUTLINE_THICKNESS);
		g.fillRect(getX() + getWidth() - OUTLINE_THICKNESS, getY(), OUTLINE_THICKNESS,
				getHeight());

		g.setColor(new Color(getColor().getRed(), getColor().getGreen(),
				getColor().getBlue(), (int) (255 * OPACITY)));
		g.fillRect(getX(), getY(), getWidth(), getHeight());
	}

	public void toggleVisible() {
		visible = !visible;
	}

}
