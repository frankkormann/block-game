package blockgame.physics;

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

	private static final int PREVENT_STRIPE_SIZE = 2;
	private static final int PREVENT_BORDER_THICKNESS = 2;

	public WallRectangle(int x, int y, int width, int height) {
		this(x, y, width, height, Colors.GRAY, ResizeBehavior.STAY);
	}

	@JsonCreator
	public WallRectangle(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("resizeBehavior") ResizeBehavior resizeBehavior) {
		this(x, y, width, height, Colors.GRAY, resizeBehavior);

		addAttachment(new JumpArea(), AttachmentOption.GLUED_NORTH,
				AttachmentOption.SAME_WIDTH);
	}

	public WallRectangle(int x, int y, int width, int height, Colors colorEnum,
			ResizeBehavior resizeBehavior) {
		super(x, y, width, height, colorEnum, resizeBehavior);
	}

	@Override
	public void draw(Graphics g) {
		super.draw(g);
		g = g.create();
		g.clipRect(getX(), getY(), getWidth(), getHeight());

		g.setColor(getColor(Colors.DARK_GRAY));
		if (getResizeBehavior() == ResizeBehavior.PREVENT_X) {
			g.fillRect(getX(), getY(), PREVENT_BORDER_THICKNESS, getHeight());
			g.fillRect(getX() + getWidth() - PREVENT_BORDER_THICKNESS, getY(),
					PREVENT_BORDER_THICKNESS, getHeight());
		}

		if (getResizeBehavior() == ResizeBehavior.PREVENT_Y) {
			g.fillRect(getX(), getY(), getWidth(), PREVENT_BORDER_THICKNESS);
			g.fillRect(getX(), getY() + getHeight() - PREVENT_BORDER_THICKNESS,
					getWidth(), PREVENT_BORDER_THICKNESS);
		}

		if (getResizeBehavior() != ResizeBehavior.STAY) {
			fillStripes(g, getColor(Colors.TRANSPARENT), PREVENT_STRIPE_SIZE,
					PREVENT_STRIPE_SIZE * 2, getX(), getY(), getWidth(),
					getHeight());
		}

		g.dispose();
	}

}
