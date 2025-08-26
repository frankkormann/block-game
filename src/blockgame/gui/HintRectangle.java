package blockgame.gui;

import java.awt.Color;
import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import blockgame.input.ParameterMapper.Parameter;
import blockgame.physics.Rectangle;

/**
 * Translucent {@code Rectangle} which can be toggled visible or not visible.
 * <p>
 * A {@code ParameterMapper} with a value for {@code Parameter.HINT_OPACITY}
 * needs to be set with {@code setParameterMapper} before this can be drawn.
 * 
 * @author Frank Kormann
 */
// Extends Rectangle to imitate the look of a Rectangle, should never be used
// as a physics object
public class HintRectangle extends Rectangle {

	private static final int OUTLINE_THICKNESS = 2;

	private boolean visible;

	@JsonCreator
	public HintRectangle(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("color") Colors colorEnum) {
		super(x, y, width, height, colorEnum, ResizeBehavior.STAY);

		visible = false;
	}

	@Override
	public void draw(Graphics g) {
		if (!visible) {
			return;
		}

		g = g.create();

		g.setColor(getBorderColor());
		drawRectOutline(g, OUTLINE_THICKNESS);

		g.setColor(new Color(getColor().getRed(), getColor().getGreen(),
				getColor().getBlue(), (int) (getColor().getAlpha()
						* paramMapper.getFloat(Parameter.OPACITY_MULTIPLIER))));

		g.fillRect(getX() + OUTLINE_THICKNESS, getY() + OUTLINE_THICKNESS,
				getWidth() - 2 * OUTLINE_THICKNESS,
				getHeight() - 2 * OUTLINE_THICKNESS);

		g.dispose();
	}

	public void toggleVisible() {
		visible = !visible;
	}

}
