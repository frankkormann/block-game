package game;

import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calculate the next position for all {@code Rectangles} every frame.
 * <p>
 * Before the first frame, {@code createSides} needs to be called to set up the
 * initial positions of the window edges.
 * <p>
 * Every frame, {@code updateAndMoveObjects} should be called to calculate the
 * frame.
 * <p>
 * {@code Rectangles} are added to the simulation by passing a reference to
 * {@code add(Rectangle)}. They are updated in-place.
 * 
 * @author Frank Kormann
 */
public class PhysicsSimulator {

	private static int FRICTION = -1;
	private static int GRAVITY = 2;
	private static int PLAYER_X_ACCELERATION = 2;
	private static int PLAYER_JUMP_VELOCITY = -20;

	private List<MovingRectangle> movingRectangles;
	private List<WallRectangle> walls;
	private List<Area> areas;
	private Map<MovingRectangle, MainFrame.Direction> sideRects;

	private Map<MainFrame.Direction, Integer> sideRectangleResizes;

	private URL nextLevel;

	public PhysicsSimulator() {
		super();

		movingRectangles = new ArrayList<>();
		walls = new ArrayList<>();
		areas = new ArrayList<>();
		sideRects = new HashMap<>();

		sideRectangleResizes = new HashMap<>();

		nextLevel = null;
	}

	/**
	 * Set up {@code MovingRectangles} that represent window edges.
	 * 
	 * @param width   Width of play area
	 * @param height  Height of play area
	 * @param xOffset X coordinate of top left corner
	 * @param yOffset Y coordinate of top left corner
	 */
	public void createSides(int width, int height, int xOffset, int yOffset) {
		sideRects.put(new MovingRectangle(xOffset, yOffset, width, 1),
				MainFrame.Direction.NORTH);
		sideRects.put(new MovingRectangle(xOffset, yOffset + height, width, 1),
				MainFrame.Direction.SOUTH);
		sideRects.put(new MovingRectangle(xOffset, yOffset, 1, height),
				MainFrame.Direction.WEST);
		sideRects.put(new MovingRectangle(xOffset + width, yOffset, 1, height),
				MainFrame.Direction.EAST);
	}

	/**
	 * Adds a {@code Rectangle} to this simulation.
	 * 
	 * @param rectangle Rectangle to add
	 */
	public void add(Rectangle rectangle) {
		// sort rectangle into the correct list
		if (rectangle instanceof MovingRectangle) {
			movingRectangles.add((MovingRectangle) rectangle);
		}
		else if (rectangle instanceof WallRectangle) {
			walls.add((WallRectangle) rectangle);
		}
		else if (rectangle instanceof Area) {
			areas.add((Area) rectangle);
		}
	}

	/**
	 * This should be called every frame to calculate the next frame.
	 * <p>
	 * Moves all {@code MovingRectangles} according to player input, their
	 * velocities from the previous frame, and natural forces (gravity, friction).
	 * Also resolves collision between {@code Rectangles} and apply all
	 * {@code Areas} that need to be applied.
	 * 
	 * @param keysPressed Set of keycodes for user input this frame
	 * @param width       Width of the play area
	 * @param height      Height of the play area
	 * @param xOffset     X coordinate of top left corner
	 * @param yOffset     Y coordinate of top left corner
	 */
	public void updateAndMoveObjects(Set<Integer> keysPressed, int width, int height,
			int xOffset, int yOffset) {

		int[] playerVelocityChanges = calculatePlayerVelocityChanges(keysPressed);
		moveAllMovingRectangles(playerVelocityChanges);

		moveAllSides(width, height, xOffset, yOffset);
	}

	private int[] calculatePlayerVelocityChanges(Set<Integer> keysPressed) {
		int xVelocityChange = 0;
		int yVelocityChange = 0;

		if (keysPressed.contains(KeyEvent.VK_D)
				|| keysPressed.contains(KeyEvent.VK_RIGHT)) {
			xVelocityChange += PLAYER_X_ACCELERATION;
		}

		if (keysPressed.contains(KeyEvent.VK_A)
				|| keysPressed.contains(KeyEvent.VK_LEFT)) {
			xVelocityChange -= PLAYER_X_ACCELERATION;
		}
		if (keysPressed.contains(KeyEvent.VK_W) || keysPressed.contains(KeyEvent.VK_UP)
				|| keysPressed.contains(KeyEvent.VK_SPACE)) {
			yVelocityChange = PLAYER_JUMP_VELOCITY;
		}

		return new int[] { xVelocityChange, yVelocityChange };
	}

	private void moveAllMovingRectangles(int[] playerVelocityChanges) {
		movingRectangles.forEach(r -> r.updateLastPosition());

		for (MovingRectangle rect : movingRectangles) {
			if (rect.isControlledByPlayer()) {
				rect.setXVelocity(rect.getXVelocity() + playerVelocityChanges[0]);
				if (rect.getState() == MovingRectangle.State.ON_GROUND) {
					rect.setYVelocity(rect.getYVelocity() + playerVelocityChanges[1]);
				}
			}

			applyAreasToMovingRectangle(rect);
			applyNaturalForcesToMovingRectangle(rect);
			rect.setState(MovingRectangle.State.IN_AIR);

			// No need to do collision if it didn't move
			if (rect.getXVelocity() == 0 && rect.getYVelocity() == 0
					&& rect.getWidth() - rect.getLastHeight() == 0
					&& rect.getHeight() - rect.getLastHeight() == 0) {
				continue;
			}

			rect.moveVelocity();

			propagateCollision(rect, movingRectangles, null);
		}
	}

	private void applyAreasToMovingRectangle(MovingRectangle rect) {
		for (Area area : areas) {
			area.handle(rect);
			if (area instanceof GoalArea && ((GoalArea) area).hasWon()) {
				nextLevel = ((GoalArea) area).getNextLevel();
			}
		}
	}

	private void applyNaturalForcesToMovingRectangle(MovingRectangle rect) {
		if (rect.hasGravity()) {
			rect.setYVelocity(rect.getYVelocity() + GRAVITY);
		}
		if (rect.getXVelocity() > 0) {
			rect.setXVelocity(rect.getXVelocity() + FRICTION);
		}
		else if (rect.getXVelocity() < 0) {
			rect.setXVelocity(rect.getXVelocity() - FRICTION);
		}
	}

	/**
	 * Calculate new positions for the window sides and calculate collision from
	 * them. Add appropriate resize values to {@code sideRectangleResizes} if the
	 * current window size is too small.
	 * 
	 * @param width   Width of play area
	 * @param height  Height of play area
	 * @param xOffset X coordinate of top left corner
	 * @param yOffset Y coordinate of top left corner
	 */
	private void moveAllSides(int width, int height, int xOffset, int yOffset) {

		sideRectangleResizes.clear();

		sideRects.keySet().forEach(s -> s.updateLastPosition());

		for (MovingRectangle side : sideRects.keySet()) {
			MainFrame.Direction direction = sideRects.get(side);
			int difference = 0;
			switch (direction) {
				case NORTH:
					movingRectangles.sort((r1, r2) -> r2.getY() - r1.getY());
					difference = calculateCollisionForSide(side, xOffset - width,
							yOffset - side.getHeight(), 3 * width, side.getHeight());
					break;
				case SOUTH:
					movingRectangles.sort((r1, r2) -> r2.getY() - r1.getY());
					difference = calculateCollisionForSide(side, xOffset - width,
							yOffset + height, 3 * width, side.getHeight());
					break;
				case WEST:
					movingRectangles.sort((r1, r2) -> r1.getX() - r2.getX());
					difference = calculateCollisionForSide(side,
							xOffset - side.getWidth(), yOffset - height,
							side.getWidth(), 3 * height);
					break;
				case EAST:
					movingRectangles.sort((r1, r2) -> r2.getX() - r1.getX());
					difference = calculateCollisionForSide(side, xOffset + width,
							yOffset - height, side.getWidth(), 3 * height);
					break;
			}
			sideRectangleResizes.put(direction, difference);
		}
	}

	/**
	 * Resizes a side and handles its collision.
	 * <p>
	 * {@code movingRectangles} must be sorted by ascending distance from the side.
	 * 
	 * @param side      Side to move
	 * @param newX      New x position
	 * @param newY      New y position
	 * @param newWidth  New width
	 * @param newHeight New height
	 * 
	 * @return Amount side was pushed back during collision
	 */
	private int calculateCollisionForSide(MovingRectangle side, int newX, int newY,
			int newWidth, int newHeight) {
		side.setX(newX);
		side.setY(newY);
		side.setWidth(newWidth);
		side.setHeight(newHeight);

		int[] pushedBack = propagateCollision(side, movingRectangles, null);

		if (pushedBack[0] != 0) { // Infer side's direction based on how it collided
			return pushedBack[0];
		}
		return pushedBack[1];
	}

	/**
	 * Moves {@code rect} so that it does not intersect any {@code Rectangles} or
	 * window edges and moves other {@code Rectangles} so that they do not intersect
	 * {@code rect}.
	 * <p>
	 * {@code collisionMap} should be cleared before this is called.
	 * 
	 * @param rect         {@code Rectangle} to propagate collision from
	 * @param colliders    {@code List} of {@code MovingRectangles} to calculate
	 *                     collision with
	 * @param collisionMap Set to {@code null}
	 * 
	 * @return { Δx, Δy } amount {@code rect} was pushed back
	 */
	// collisionMap is used to track which Rectangles pushed each other and how much
	private int[] propagateCollision(MovingRectangle rect,
			List<MovingRectangle> colliders,
			Map<MovingRectangle, RectangleMapObject> collisionMap) {
		if (collisionMap == null) {
			collisionMap = new HashMap<>();
		}

		int[] collisionData;
		int[] pushedAmount = { 0, 0 };
		int numberCollided = 0;
		// Copy before removing rect
		colliders = new ArrayList<>(colliders);
		colliders.remove(rect);

		int[] wallPushback = handleCollisionWithWalls(rect);
		pushedAmount[0] += wallPushback[0];
		pushedAmount[1] += wallPushback[1];

		for (MovingRectangle other : colliders) {
			if (other.getResizeBehavior() == Rectangle.ResizeBehavior.STAY
					&& sideRects.containsKey(rect)) {
				continue;
			}

			collisionData = calculateCollision(rect, other);
			if (collisionData[0] == 0 && collisionData[1] == 0) {
				continue;
			}

			other.moveCollision(collisionData[0], collisionData[1]);
			collisionMap.put(other, new RectangleMapObject(rect, collisionData));
			numberCollided++;
			// Now compute collision from other moving
			int[] pushback = propagateCollision(other, colliders, collisionMap);
			rect.moveCollision(pushback[0], pushback[1]);
			pushedAmount[0] += pushback[0];
			pushedAmount[1] += pushback[1];
		}

		// Pull back Rectangles that collided to be aligned with the edge of this
		if (numberCollided >= 2) {
			for (MovingRectangle c : collisionMap.keySet()) {
				if (collisionMap.get(c).pushedBy == rect) {
					pullback(rect, c, collisionMap);
				}
			}
		}

		return pushedAmount;
	}

	/**
	 * Move {@code rect} so that it does not collide with any walls.
	 * 
	 * @param rect {@code MovingRectangle} to move
	 * 
	 * @return { dx, dy } of amount {@code rect} was pushed back
	 */
	private int[] handleCollisionWithWalls(MovingRectangle rect) {
		int[] pushedAmount = { 0, 0 };
		for (WallRectangle wall : walls) {
			if (wall.getResizeBehavior() == Rectangle.ResizeBehavior.STAY
					&& sideRects.containsKey(rect)) {
				continue;
			}

			int[] collisionData = calculateCollision(wall, rect);
			if (collisionData[0] == 0 && collisionData[1] == 0) {
				continue;
			}
			rect.moveCollision(collisionData[0], collisionData[1]);
			pushedAmount[0] += collisionData[0];
			pushedAmount[1] += collisionData[1];
		}
		return pushedAmount;
	}

	/**
	 * Calculate how to move {@code other} so that it does not intersect
	 * {@code rect}. Returns { 0, 0 } if {@code rect == other}. Never returns a
	 * movement in both directions (x and y).
	 * 
	 * @param rect  {@code Rectangle} that is considered stationary
	 * @param other {@code Rectangle} that will move
	 * 
	 * @return { Δx, Δy } amount to move {@code rect} to resolve collision with
	 *         {@code other}
	 */
	private int[] calculateCollision(Rectangle rect, Rectangle other) {
		if (rect == other) {
			return new int[] { 0, 0 };
		}

		int xChange = 0;
		int yChange = 0;
		boolean inBoundsX = rect.intersectsX(other);
		boolean inBoundsY = rect.intersectsY(other);
		// "Used to be" values so rectangles can tell whether they should be moved in x
		// or y direction
		boolean usedToBeInBoundsX = rect.usedToIntersectX(other);
		boolean usedToBeInBoundsY = rect.usedToIntersectY(other);

		// Move other so that it is adjacent to rect
		if (inBoundsX && inBoundsY) {
			if (usedToBeInBoundsY) {
				xChange = pullToX(rect, other);
			}
			else {
				yChange = pullToY(rect, other);
			}
		}
		// These account for when a rectangle would pass through another diagonally
		else if (inBoundsX && usedToBeInBoundsY) {
			xChange = pullToX(rect, other);
		}
		else if (inBoundsY && usedToBeInBoundsX) {
			yChange = pullToY(rect, other);
		}
		else {
			// If other started on one side of this and ended up on the opposite side,
			// it must have collided between frames
			int xSign = (int) Math.signum(rect.getX() - other.getX());
			int ySign = (int) Math.signum(rect.getY() - other.getY());
			int xOldSign = (int) Math.signum(rect.getLastX() - other.getLastX());
			int yOldSign = (int) Math.signum(rect.getLastY() - other.getLastY());
			if ((xSign != xOldSign) && (inBoundsY || usedToBeInBoundsY)) {
				xChange = pullToX(rect, other);
			}
			if ((ySign != yOldSign) && (inBoundsX || usedToBeInBoundsX)) {
				yChange = pullToY(rect, other);
			}

		}
		return new int[] { xChange, yChange };
	}

	/**
	 * Called by {@link#propagateCollision} to traverse through
	 * {@code collisionMap}. Pull {@code other} back to {@code rect} and pull the
	 * rectangles associated with {@code other} back to {@code other}.
	 * 
	 * @param rect      {@code Rectangle} to align with
	 * @param other     {@code Rectangle} to pull back
	 * @param maxPull   { maxX, maxY } of maximum amount to pull {@code other} back,
	 *                  even if it doesn't get lined up with {@code rect}
	 * @param direction boolean representing direction; {@code true} for x and
	 *                  {@code false} for y
	 */
	private void pullback(Rectangle rect, MovingRectangle other,
			Map<MovingRectangle, RectangleMapObject> collisionMap) {
		int xChange = 0;
		int yChange = 0;

		int[] pushedAmount = collisionMap.get(other).pushedAmount;

		if (pushedAmount[1] == 0) {  // not pushed in y direction -> x collision
			xChange = pullToX(rect, other);
		}
		else {
			yChange = pullToY(rect, other);
		}

		if (Math.abs(xChange) > Math.abs(pushedAmount[0])) {
			xChange = -pushedAmount[0];
		}
		if (Math.abs(yChange) > Math.abs(pushedAmount[1])) {
			yChange = -pushedAmount[1];
		}
		other.moveCollision(xChange, yChange);

		// TODO: collisionMap needs to contain maxPull data
		for (MovingRectangle c : collisionMap.keySet()) {
			if (collisionMap.get(c).pushedBy == other) {
				pullback(other, c, collisionMap);
			}
		}
	}

	/**
	 * Calculate how far to move {@code other} to be adjacent to {@code rect} in the
	 * x direction.
	 * 
	 * @param rect  {@code Rectangle} that is considered stationary
	 * @param other {@code Rectangle} that will move
	 * 
	 * @return amount to move {@code other}
	 */
	private int pullToX(Rectangle rect, Rectangle other) {
		if (other.getLastX() >= rect.getLastX()) {
			return rect.getX() + rect.getWidth() - other.getX();
		}
		return rect.getX() - other.getX() - other.getWidth();
	}

	/**
	 * Calculate how far to move {@code other} to be adjacent to {@code rect} in the
	 * y direction.
	 * 
	 * @param rect  {@code Rectangle} that is considered stationary
	 * @param other {@code Rectangle} that will move
	 * 
	 * @return amount to move {@code other}
	 */
	private int pullToY(Rectangle rect, Rectangle other) {
		if (other.getLastY() >= rect.getLastY()) {
			return rect.getY() + rect.getHeight() - other.getY();
		}
		return rect.getY() - other.getY() - other.getHeight();
	}

	public URL getNextLevel() {
		return nextLevel;
	}

	public Map<MainFrame.Direction, Integer> getResizes() {
		return sideRectangleResizes;
	}

	/**
	 * Struct-like object to hold which Rectangle a MovingRectangle was pushed by
	 * and how much it was pushed, for use in a Map.
	 */
	private class RectangleMapObject {
		public Rectangle pushedBy;
		public int[] pushedAmount;

		public RectangleMapObject(Rectangle pushedBy, int[] pushedAmount) {
			this.pushedBy = pushedBy;
			this.pushedAmount = pushedAmount;
		}
	}

}
