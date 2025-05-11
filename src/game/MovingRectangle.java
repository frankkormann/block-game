package game;

import java.awt.Color;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code Rectangle} with x- and y-velocity, affected by gravity and friction.
 * Default {@code ResizeBehavior} is {@code MOVE}.
 * <p>
 * This should generally by moved through {@link#moveVelocity} and
 * {@link#moveCollision}. {@code moveVelocity} should be called once every frame
 * after this's velocity is determined. {@code moveCollision} should be called
 * if this pushed by another {@code Rectangle}.
 * <p>
 * If the player is in control of this, the {@code controlledByPlayer} flag
 * should be set to indicate it.
 * 
 * @author Frank Kormann
 */

public class MovingRectangle extends Rectangle {

	public enum Colors {
		BLACK(0, 0, 0, 255), BLUE(130, 202, 255, 255), GREEN(100, 250, 100, 255),
		GRAY(229, 229, 229, 255), ORANGE(255, 174, 66, 255), RED(246, 114, 128, 255),
		PLAYER(66, 148, 255, 255);

		public final Color color;

		private Colors(int r, int g, int b, int a) {
			this.color = new Color(r, g, b, a);
		}
	}

	// TODO Find a better system for telling whether a player rectangle can jump
	public enum State {
		ON_GROUND, IN_AIR
	}

	private static final int MAX_X_SPEED = 10;
	private static final int MAX_Y_SPEED = 20;

	private int xVelocity, yVelocity;

	private boolean hasGravity;
	private boolean controlledByPlayer;

	private State state;

	public MovingRectangle(int x, int y, int width, int height) {
		this(x, y, width, height, Colors.GRAY.color);
	}

	public MovingRectangle(int x, int y, int width, int height, Color color) {
		this(x, y, width, height, color, true, Rectangle.ResizeBehavior.MOVE);
	}

	@JsonCreator
	public MovingRectangle(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width, @JsonProperty("height") int height,
			@JsonProperty("color") Colors color) {
		this(x, y, width, height, color.color);
	}

	public MovingRectangle(int x, int y, int width, int height, Color color,
			boolean hasGravity, Rectangle.ResizeBehavior resizeBehavior) {
		super(x, y, width, height, color, resizeBehavior);
		this.hasGravity = hasGravity;
		xVelocity = 0;
		yVelocity = 0;
		controlledByPlayer = false;
	}

	/**
	 * Apply one frame's worth of velocity and clamp to max speeds.
	 */
	public void moveVelocity() {
		// Speed limits
		if (xVelocity > MAX_X_SPEED) {
			xVelocity = MAX_X_SPEED;
		}
		else if (xVelocity < MAX_X_SPEED * -1) {
			xVelocity = MAX_X_SPEED * -1;
		}

		if (yVelocity > MAX_Y_SPEED) {
			yVelocity = MAX_Y_SPEED;
		}
		else if (yVelocity < MAX_Y_SPEED * -1) {
			yVelocity = MAX_Y_SPEED * -1;
		}

		setX(getX() + xVelocity);
		setY(getY() + yVelocity);
	}

	/**
	 * Zero velocities and set ground status if necessary
	 */
	public void moveCollision(int xChange, int yChange) {

		if (xChange != 0) {
			xVelocity = 0;
		}
		if (yChange != 0) {
			yVelocity = 0;
			if (yChange < 0) {
				setState(State.ON_GROUND);
			}
		}

		// If the block is colliding because it grew, calculate how much to shrink it
		// and how much to actually move it
		if (xChange != 0) {
			int widthChange;
			if (xChange > 0) {
				widthChange = -getLeftWidthChange();
			}
			else {
				widthChange = -getWidth() + getLeftWidthChange() + getLastWidth();
				xChange -= widthChange; // Doesn't use addToLeft parameter in
										 // changeWidth() to fix a bug with growing in a
										 // space confined between two walls
			}
			setX(getX() + xChange);
			if (widthChange < 0) { // Don't add width
				changeWidth(widthChange, false);
			}
		}

		if (yChange != 0) {
			int heightChange;
			if (yChange > 0) {
				heightChange = -getTopHeightChange();
			}
			else {
				heightChange = -getHeight() + getTopHeightChange() + getLastHeight();
				yChange -= heightChange; // Same reasoning applies as in width section
			}
			setY(getY() + yChange);
			if (heightChange < 0) { // Don't add height
				changeHeight(heightChange, false);
			}
		}

	}

	public int getXVelocity() {
		return xVelocity;
	}

	public void setXVelocity(int xVelocity) {
		this.xVelocity = xVelocity;
	}

	public int getYVelocity() {
		return yVelocity;
	}

	public void setYVelocity(int yVelocity) {
		this.yVelocity = yVelocity;
	}

	public boolean hasGravity() {
		return hasGravity;
	}

	public void setHasGravity(boolean hasGravity) {
		this.hasGravity = hasGravity;
	}

	public boolean isControlledByPlayer() {
		return controlledByPlayer;
	}

	public void setControlledByPlayer(boolean controlledByPlayer) {
		this.controlledByPlayer = controlledByPlayer;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

}
