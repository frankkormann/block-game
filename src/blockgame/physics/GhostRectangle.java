package blockgame.physics;

import java.awt.Color;
import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import blockgame.gui.MainFrame.Direction;
import blockgame.input.ParameterMapper.Parameter;

/**
 * {@code MovingRectangle} which does not interact with {@code SideRectangle}s
 * unless their {@code Direction} is {@code SOUTH}.
 * 
 * @author Frank Kormann
 */
public class GhostRectangle extends MovingRectangle {

	@JsonCreator
	public GhostRectangle(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("color") Colors color) {
		super(x, y, width, height, color);
	}

	// TODO Draw this more uniquely
	@Override
	public void draw(Graphics g) {
		g = g.create();

		g.setColor(getBorderColor());
		drawRectOutline(g, 2);

		g.setColor(new Color(getColor().getRed(), getColor().getGreen(),
				getColor().getBlue(), (int) (getColor().getAlpha()
						* paramMapper.getFloat(Parameter.OPACITY_MULTIPLIER))));

		g.fillRect(getX() + 2, getY() + 2, getWidth() - 2 * 2,
				getHeight() - 2 * 2);

		g.dispose();
	}

	@Override
	public boolean canInteract(Rectangle other) {
		if (other instanceof SideRectangle) {
			return ((SideRectangle) other).getDirection() == Direction.SOUTH;
		}
		return super.canInteract(other);
	}

}
