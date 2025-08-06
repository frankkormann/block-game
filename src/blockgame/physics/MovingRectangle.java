package blockgame.physics;

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
 * Width/height should generally be adjusted using {@link#changeWidth} and
 * {@link#changeHeight}. This allows some metadata to be updated which is used
 * in resolving collisions.
 * 
 * @author Frank Kormann
 */
public class MovingRectangle extends Rectangle {

	public enum State {
		ON_GROUND, IN_AIR
	}

	private static final int MAX_X_SPEED = 10;
	private static final int MAX_Y_SPEED = 20;

	private int xVelocity, yVelocity;

	private int lastX, lastY, lastWidth, lastHeight;
	// These are used in collision to determine how much width/height to remove
	// if this is colliding because it increased in width/height
	private int leftWidthChange, topHeightChange;

	private boolean hasGravity;
	private boolean controlledByPlayer;
	private boolean hasMoved;

	private State state;

	public MovingRectangle(int x, int y, int width, int height) {
		this(x, y, width, height, Colors.BLACK);
	}

	@JsonCreator
	public MovingRectangle(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("color") Colors color) {
		this(x, y, width, height, color, true, ResizeBehavior.MOVE);
	}

	public MovingRectangle(int x, int y, int width, int height,
			Colors colorEnum, boolean hasGravity,
			ResizeBehavior resizeBehavior) {
		super(x, y, width, height, colorEnum, resizeBehavior);
		this.hasGravity = hasGravity;
		xVelocity = 0;
		yVelocity = 0;
		controlledByPlayer = false;
		state = State.IN_AIR;
		hasMoved = false;

		updateLastPosition();

		addAttachment(new GroundingArea(x, y - 1, width),
				AttachmentOption.GLUED_NORTH, AttachmentOption.SAME_WIDTH);
	}

	/**
	 * Updates the stored location of this on the previous frame to be equal to
	 * its current position. This should be called at the start of each frame.
	 */
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
	 * Applies one frame's worth of velocity and clamps to max speeds.
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
	 * opposite direction to its velocity, sets that component of its velocity
	 * to zero.
	 * 
	 * @param xChange amount to move in x direction
	 * @param yChange amount to move in y direction
	 */
	public void moveCollision(int xChange, int yChange) {
		if (xChange == 0 && yChange == 0) {
			return;
		}

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
	 * Calculate whether this intersected with {@code other} in the x direction
	 * on the previous frame.
	 * 
	 * @param other other {@code Rectangle}
	 * 
	 * @return true if they used to intersect in the x direction
	 */
	@Override
	public boolean usedToIntersectX(Rectangle other) {
		boolean usedToBeInBoundsX = (lastX <= other.getLastX()
				&& other.getLastX() < lastX + lastWidth)
				|| (lastX < other.getLastX() + other.getLastWidth()
						&& other.getLastX() + other.getLastWidth() <= lastX
								+ lastWidth)
				|| (other.getLastX() < lastX
						&& lastX < other.getLastX() + other.getLastWidth());
		return canInteract(other) && other.canInteract(this)
				&& usedToBeInBoundsX;
	}

	/**
	 * Calculate whether this intersected with {@code other} in the y direction
	 * on the previous frame.
	 * 
	 * @param other other {@code Rectangle}
	 * 
	 * @return true if they used to intersect in the y direction
	 */
	@Override
	public boolean usedToIntersectY(Rectangle other) {
		boolean usedToBeInBoundsY = (lastY <= other.getLastY()
				&& other.getLastY() < lastY + lastHeight)
				|| (lastY < other.getLastY() + other.getLastHeight()
						&& other.getLastY() + other.getLastHeight() <= lastY
								+ lastHeight)
				|| (other.getLastY() < lastY
						&& lastY < other.getLastY() + other.getLastHeight());
		return canInteract(other) && other.canInteract(this)
				&& usedToBeInBoundsY;
	}

	/**
	 * Change the width by {@code change}.
	 * <p>
	 * If {@code addToLeft} is {@code true}, the left edge of this will move and
	 * the right edge will stay in place. Otherwise, the right edge will move
	 * and the left edge will stay in place.
	 * 
	 * @param change    amount to adjust width by
	 * @param addToLeft {@code true} if the left edge should move
	 */
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

	/**
	 * Change the height by {@code change}.
	 * <p>
	 * If {@code addToTop} is {@code true}, the top edge of this will move and
	 * the bottom edge will stay in place. Otherwise, the bottom edge will move
	 * and the top edge will stay in place.
	 * 
	 * @param change   amount to adjust height by
	 * @param addToTop {@code true} if the top edge should move
	 */
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

	/**
	 * Returns where this was on the previous frame.
	 * 
	 * @return {@code x} position on the previous frame
	 */
	@Override
	public int getLastX() {
		return lastX;
	}

	/**
	 * Returns where this was on the previous frame.
	 * 
	 * @return {@code y} position on the previous frame
	 */
	@Override
	public int getLastY() {
		return lastY;
	}

	/**
	 * Returns how wide this was on the previous frame.
	 * 
	 * @return {@code width} on the previous frame
	 */
	@Override
	public int getLastWidth() {
		return lastWidth;
	}

	/**
	 * Returns how tall this was on the previous frame.
	 * 
	 * @return {@code height} on the previous frame
	 */
	@Override
	public int getLastHeight() {
		return lastHeight;
	}

	/**
	 * Returns how much width was added to the left side of this since the last
	 * frame.
	 * 
	 * @return amount of width added to the left
	 */
	public int getLeftWidthChange() {
		return leftWidthChange;
	}

	/**
	 * Returns how much height was added to the top side of this since the last
	 * frame.
	 * 
	 * @return amount of height added to the top
	 */
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
