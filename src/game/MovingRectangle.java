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

	public enum State {
		ON_GROUND, IN_AIR
	}

	private static final int MAX_X_SPEED = 10;
	private static final int MAX_Y_SPEED = 20;

	private int xVelocity, yVelocity;

	private int lastX, lastY, lastWidth, lastHeight;
	// These are used in collision to determine how much width/height to remove if
	// this is colliding because it increased in width/height
	private int leftWidthChange, topHeightChange;

	private boolean hasGravity;
	private boolean controlledByPlayer;
	private boolean hasMoved;

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
		state = State.IN_AIR;
		hasMoved = false;

		updateLastPosition();

		addAttachment(new GroundingArea(x, y - 1, width, 1),
				Rectangle.AttachmentOption.GLUED_NORTH,
				Rectangle.AttachmentOption.SAME_WIDTH);
	}

	public void updateLastPosition() {
		lastX = getX();
		lastY = getY();
		lastWidth = getWidth();
		lastHeight = getHeight();
		leftWidthChange = 0;
		topHeightChange = 0;

		hasMoved = false;
	}

	/**
	 * Applies one frame's worth of velocity and clamps to max speeds
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
	 * Sets {@code x += xChange}, {@code y += yChange}. If this was moved in the
	 * opposite direction to its velocity, sets that component of its velocity to
	 * zero.
	 */
	public void moveCollision(int xChange, int yChange) {

		if (xChange != 0 && Math.signum(xChange) != Math.signum(xVelocity)) {
			xVelocity = 0;
		}
		if (yChange != 0 && Math.signum(yChange) != Math.signum(yVelocity)) {
			yVelocity = 0;
		}

		setX(getX() + xChange);
		setY(getY() + yChange);

		hasMoved = true;
	}

	/**
	 * Calculate whether this intersected with other in the x direction on the
	 * previous frame.
	 * 
	 * @param other Other Rectangle
	 * 
	 * @return true if they used to intersect in the x direction
	 */
	public boolean usedToIntersectX(Rectangle other) {
		boolean usedToBeInBoundsX = (lastX <= other.getLastX()
				&& other.getLastX() < lastX + lastWidth)
				|| (lastX < other.getLastX() + other.getLastWidth()
						&& other.getLastX() + other.getLastWidth() <= lastX + lastWidth)
				|| (other.getLastX() < lastX
						&& lastX < other.getLastX() + other.getLastWidth());
		return canInteract(other) && other.canInteract(this) && usedToBeInBoundsX;
	}

	/**
	 * Calculate whether this intersected with other in the y direction on the
	 * previous frame.
	 * 
	 * @param other Other Rectangle
	 * 
	 * @return true if they used to intersect in the y direction
	 */
	public boolean usedToIntersectY(Rectangle other) {
		boolean usedToBeInBoundsY = (lastY <= other.getLastY()
				&& other.getLastY() < lastY + lastHeight)
				|| (lastY < other.getLastY() + other.getLastHeight() && other.getLastY()
						+ other.getLastHeight() <= lastY + lastHeight)
				|| (other.getLastY() < lastY
						&& lastY < other.getLastY() + other.getLastHeight());
		return canInteract(other) && other.canInteract(this) && usedToBeInBoundsY;
	}

	public void changeWidth(int change, boolean addToLeft) {
		setWidth(getWidth() + change);
		if (getWidth() <= 0) {
			change += 1 - getWidth();
			setWidth(1);
		}
		if (addToLeft) {
			setX(getX() - change);
			leftWidthChange += change;
		}
		hasMoved = true;
	}

	public void changeHeight(int change, boolean addToTop) {
		setHeight(getHeight() + change);
		if (getHeight() <= 0) {
			change += 1 - getHeight();
			setHeight(1);
		}
		if (addToTop) {
			setY(getY() - change);
			topHeightChange += change;
		}
		hasMoved = true;
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

	public int getLastX() {
		return lastX;
	}

	public int getLastY() {
		return lastY;
	}

	public int getLastWidth() {
		return lastWidth;
	}

	public int getLastHeight() {
		return lastHeight;
	}

	public int getLeftWidthChange() {
		return leftWidthChange;
	}

	public int getTopHeightChange() {
		return topHeightChange;
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

	public boolean hasMoved() {
		return hasMoved;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

}
