package game;

import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code Rectangle} to represent immovable walls. Default
 * {@code ResizeBehavior} is {@code STAY}.
 * 
 * @author Frank Kormann
 */
public class WallRectangle extends Rectangle {

	public enum WallColors {
		GRAY, DARK_GRAY
	}

	private static final int TICK_MARK_SIZE = 5;

	public WallRectangle(int x, int y, int width, int height) {
		this(x, y, width, height, WallColors.GRAY, ResizeBehavior.STAY);
	}

	@JsonCreator
	public WallRectangle(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("resizeBehavior") ResizeBehavior resizeBehavior) {
		this(x, y, width, height, WallColors.GRAY, resizeBehavior);

		addAttachment(new GroundingArea(x, y - 1, width),
				AttachmentOption.GLUED_NORTH, AttachmentOption.SAME_WIDTH);
	}

	public WallRectangle(int x, int y, int width, int height, Enum<?> colorEnum,
			ResizeBehavior resizeBehavior) {
		super(x, y, width, height, colorEnum, resizeBehavior);
	}

	// TODO Fix overlap with other rectangles in the PREVENT_X/PREVENT_Y border
	// drawing, and improve how inner lines are drawn
	@Override
	public void draw(Graphics g) {
		super.draw(g);
		g = g.create();

		g.setColor(getColor(WallColors.DARK_GRAY));
		if (getResizeBehavior() == ResizeBehavior.PREVENT_X) {
			g.drawLine(getX(), getY(), getX(), getY() + getHeight() - 1);
			g.drawLine(getX() + getWidth() - 1, getY(), getX() + getWidth() - 1,
					getY() + getHeight() - 1);

			for (int y = getY(); y < getY()
					+ getHeight(); y += TICK_MARK_SIZE) {
				g.drawLine(getX() + TICK_MARK_SIZE, y, getX(),
						y + TICK_MARK_SIZE);
				g.drawLine(getX() + getWidth() - 1, y,
						getX() + getWidth() - TICK_MARK_SIZE - 1,
						y + TICK_MARK_SIZE);
			}
		}

		if (getResizeBehavior() == ResizeBehavior.PREVENT_Y) {
			g.drawLine(getX(), getY(), getX() + getWidth() - 1, getY());
			g.drawLine(getX(), getY() + getHeight() - 1,
					getX() + getWidth() - 1, getY() + getHeight() - 1);

			for (int x = getX(); x < getX() + getWidth(); x += TICK_MARK_SIZE) {
				g.drawLine(x, getY() + TICK_MARK_SIZE, x + TICK_MARK_SIZE,
						getY());
				g.drawLine(x, getY() + getHeight() - 1, x + TICK_MARK_SIZE,
						getY() + getHeight() - TICK_MARK_SIZE - 1);
			}
		}

		g.dispose();
	}

}
