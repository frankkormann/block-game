package blockgame.physics;

import java.awt.Color;
import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import blockgame.gui.MainFrame.Direction;
import blockgame.input.ParameterMapper.Parameter;
import blockgame.util.DrawUtils;

/**
 * {@code MovingRectangle} which does not interact with {@code SideRectangle}s
 * unless their {@code Direction} is {@code SOUTH}.
 * 
 * @author Frank Kormann
 */
public class GhostRectangle extends MovingRectangle {

	private static final int BORDER_THICKNESS = 1;
	private static final int STRIPE_THICKNESS_OPAQUE = 5;
	private static final int STRIPE_THICKNESS_TRANSLUCENT = 10;

	@JsonCreator
	public GhostRectangle(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("color") Colors color) {
		super(x, y, width, height, color);
	}

	@Override
	public void draw(Graphics g) {
		g = g.create();

		g.clipRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(getBorderColor());
		DrawUtils.drawRectOutline(g, BORDER_THICKNESS, getX(), getY(),
				getWidth(), getHeight());

		Color translucentColor = new Color(getColor().getRed(),
				getColor().getGreen(), getColor().getBlue(),
				(int) (getColor().getAlpha()
						* paramMapper.getFloat(Parameter.OPACITY_MULTIPLIER)));
//		boolean isTranslucentStripe = true;
//		int thickness = STRIPE_THICKNESS_TRANSLUCENT;
//		// Multiply width/height by 2 to capture both halves of the rectangle
//		for (int x = getX() + BORDER_THICKNESS, y = getY() + BORDER_THICKNESS; x
//				+ thickness < getX() + getWidth() * 2
//				|| y + thickness < getY()
//						+ getHeight() * 2; x += thickness, y += thickness) {
//
//			thickness = isTranslucentStripe ? STRIPE_THICKNESS_TRANSLUCENT
//					: STRIPE_THICKNESS_OPAQUE;
//			g.setColor(
//					isTranslucentStripe ? translucentColor : getBorderColor());
//			g.fillPolygon(new int[] { getX(), x, x + thickness, getX() },
//					new int[] { y, getY(), getY(), y + thickness }, 4);
//
//			isTranslucentStripe = !isTranslucentStripe;
//		}
		DrawUtils.fillStripes(g, translucentColor, STRIPE_THICKNESS_OPAQUE,
				STRIPE_THICKNESS_TRANSLUCENT, getX() + BORDER_THICKNESS,
				getY() + BORDER_THICKNESS, getWidth() - 2 * BORDER_THICKNESS,
				getHeight() - 2 * BORDER_THICKNESS);

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
