package game;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

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

	private static int WALL_COLLISION_LEEWAY_X = 4;
	private static int WALL_COLLISION_LEEWAY_Y = 3;

	private List<MovingRectangle> movingRectangles;
	private List<WallRectangle> walls;
	private List<Area> areas;
	private List<GoalArea> goals;
	private List<SideRectangle> sideRectangles;

	private Map<MainFrame.Direction, Integer> sideRectangleResizes;

	private URL nextLevel;

	public PhysicsSimulator() {
		super();

		movingRectangles = new ArrayList<>();
		walls = new ArrayList<>();
		areas = new ArrayList<>();
		goals = new ArrayList<>();
		sideRectangles = new ArrayList<>();

		sideRectangleResizes = new HashMap<>();

		nextLevel = null;
	}

	/**
	 * Sets up {@code SideRectangles} that represent window edges.
	 * 
	 * @param width   Width of play area
	 * @param height  Height of play area
	 * @param xOffset X coordinate of top left corner
	 * @param yOffset Y coordinate of top left corner
	 */
	public void createSides(int width, int height, int xOffset, int yOffset) {
		sideRectangles.add(new SideRectangle(xOffset, yOffset, width, 1,
				MainFrame.Direction.NORTH));
		sideRectangles.add(new SideRectangle(xOffset, yOffset + height, width, 1,
				MainFrame.Direction.SOUTH));
		sideRectangles.add(new SideRectangle(xOffset, yOffset, 1, height,
				MainFrame.Direction.WEST));
		sideRectangles.add(new SideRectangle(xOffset + width, yOffset, 1, height,
				MainFrame.Direction.EAST));
	}

	public void addMovingRectangle(MovingRectangle rect) {
		movingRectangles.add(rect);
	}

	public void addWall(WallRectangle wall) {
		walls.add(wall);
	}

	/**
	 * Note: use {@link#addGoalArea(GoalArea)} for {@code GoalArea}s
	 */
	public void addArea(Area area) {
		areas.add(area);
	}

	public void addGoalArea(GoalArea goal) {
		goals.add(goal);
	}

	/**
	 * This should be called every frame to calculate the next frame.
	 * <p>
	 * Moves all {@code MovingRectangles} according to player input, their
	 * velocities from the previous frame, and natural forces (gravity, friction).
	 * Also resolves collision between {@code Rectangles} and apply all
	 * {@code Areas} that need to be applied.
	 * 
	 * @param gameInputs {@code Set} of {@code Input}s from the player this frame
	 * @param width      Width of the play area
	 * @param height     Height of the play area
	 * @param xOffset    X coordinate of top left corner
	 * @param yOffset    Y coordinate of top left corner
	 */
	public void updateAndMoveObjects(Set<GameInputHandler.GameInput> gameInputs,
			int width, int height, int xOffset, int yOffset) {

		int[] playerVelocityChanges = calculatePlayerVelocityChanges(gameInputs);
		moveAllMovingRectangles(playerVelocityChanges);

		moveAllSides(width, height, xOffset, yOffset);
	}

	/**
	 * Determines how the velocity should change for each {@code MovingRectangle}
	 * with {@code isControlledByPlayer() == true}.
	 * <p>
	 * Only keyboard input is taken into account. Other factors that affect each
	 * particular {@code MovingRectangle} may augment or override the velocity
	 * changes returned here.
	 * 
	 * @param gameInputs {@code Set} of {@code Input}s from the player
	 * 
	 * @return int array in format { change in x-velocity, change in y-velocity }
	 */
	private int[] calculatePlayerVelocityChanges(
			Set<GameInputHandler.GameInput> gameInputs) {
		int xVelocityChange = 0;
		int yVelocityChange = 0;

		if (gameInputs.contains(GameInputHandler.GameInput.RIGHT)) {
			xVelocityChange += PLAYER_X_ACCELERATION;
		}

		if (gameInputs.contains(GameInputHandler.GameInput.LEFT)) {
			xVelocityChange -= PLAYER_X_ACCELERATION;
		}
		if (gameInputs.contains(GameInputHandler.GameInput.UP)) {
			yVelocityChange = PLAYER_JUMP_VELOCITY;
		}

		return new int[] { xVelocityChange, yVelocityChange };
	}

	/**
	 * For each {@code MovingRectangle}, applies keyboard input if appropriate,
	 * applies {@code Area} effects, applies friction and gravity, applies movement
	 * from velocity, then computes collision.
	 * 
	 * @param playerVelocityChanges int array in format { change in x-velocity,
	 *                              change in y-velocity } for changes to velocity
	 *                              of {@code MovingRectangle}s with
	 *                              {@code isControlledByPlayer() == true}
	 */
	private void moveAllMovingRectangles(int[] playerVelocityChanges) {
		movingRectangles.forEach(r -> r.updateLastPosition());

		// sort by distance from bottom of screen for consistency
		movingRectangles.sort(
				(r1, r2) -> r2.getY() + r2.getHeight() - r1.getY() - r1.getHeight());

		for (MovingRectangle rect : movingRectangles) {
			if (rect.isControlledByPlayer()) {
				rect.setXVelocity(rect.getXVelocity() + playerVelocityChanges[0]);
				if (rect.getState() == MovingRectangle.State.ON_GROUND) {
					rect.setYVelocity(rect.getYVelocity() + playerVelocityChanges[1]);
				}
			}

			applyNaturalForcesToMovingRectangle(rect);
			applyAreasToMovingRectangle(rect);
			applyGoalAreas(rect);
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

	/**
	 * Tests if {@code rect} intersects any {@code Area}s and applies effects of any
	 * it does intersect.
	 * <p>
	 * Note that this method does not check against {@code GoalArea}s.
	 * 
	 * @param rect {@code MovingRectangle} to consider
	 */
	private void applyAreasToMovingRectangle(MovingRectangle rect) {
		for (Area area : areas) {
			area.handle(rect);
		}
	}

	/**
	 * Tests {@code rect} against {@code GoalArea}s and updates {@code nextLevel} if
	 * necessary.
	 * 
	 * @param rect {@code MovingRectangle} to consider
	 */
	private void applyGoalAreas(MovingRectangle rect) {
		for (GoalArea goal : goals) {
			goal.handle(rect);

			if (goal.hasWon()) {
				nextLevel = goal.getNextLevel();
			}
		}
	}

	/**
	 * Accelerates {@code rect} downward due to gravity and reduces x-velocity due
	 * to friction if appropriate.
	 *
	 * @param rect {@code MovingRectangle} to consider
	 */
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

		sideRectangles.forEach(s -> s.updateLastPosition());

		for (SideRectangle side : sideRectangles) {
			side.setActLikeWall(false);
			MainFrame.Direction direction = side.getDirection();
			int difference = 0;
			switch (direction) {
				// Width/height are super high to prevent bug where MovingRectangles
				// could phase through the floor because the floor was not wide enough
				case NORTH:
					movingRectangles.sort((r1, r2) -> r2.getY() - r1.getY());
					difference = calculateCollisionForSide(side, xOffset - 50 * width,
							yOffset - side.getHeight(), 101 * width, side.getHeight());
					break;
				case SOUTH:
					movingRectangles.sort((r1, r2) -> r2.getY() + r2.getHeight()
							- r1.getY() - r1.getHeight());
					difference = calculateCollisionForSide(side, xOffset - 50 * width,
							yOffset + height, 101 * width, side.getHeight());
					break;
				case WEST:
					movingRectangles.sort((r1, r2) -> r1.getX() - r2.getX());
					difference = calculateCollisionForSide(side,
							xOffset - side.getWidth(), yOffset - 50 * height,
							side.getWidth(), 101 * height);
					break;
				case EAST:
					movingRectangles.sort((r1, r2) -> r2.getX() + r2.getWidth()
							- r1.getX() - r1.getWidth());
					difference = calculateCollisionForSide(side, xOffset + width,
							yOffset - 50 * height, side.getWidth(), 101 * height);
					break;
			}
			side.setActLikeWall(true);
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
	private int calculateCollisionForSide(SideRectangle side, int newX, int newY,
			int newWidth, int newHeight) {
		side.setX(newX);
		side.setY(newY);
		side.setWidth(newWidth);
		side.setHeight(newHeight);

		int[] pushedBack = propagateCollision(side, movingRectangles, null);

		if (pushedBack[0] != 0) {  // Infer side's direction based on how it collided
			return pushedBack[0];
		}
		return pushedBack[1];
	}

	/**
	 * Moves {@code rect} so that it does not intersect any {@code WallRectangles}.
	 * Moves other {@code MovingRectangles} so that they do not intersect
	 * {@code rect}. Acts recursively on each {@code MovingRectangle} moved by
	 * {@code rect}.
	 * <p>
	 * {@code WallRectangle} collisions are calculated before
	 * {@code MovingRectangle} collisions. If {@code rect} collides with two or more
	 * {@code MovingRectangles}, an extra alignment step is performed to make sure
	 * nothing was moved that should not have been.
	 * <p>
	 * Note: WallRectangles are read from global {@code List<WallRectangle> walls}
	 * 
	 * @param rect         {@code MovingRectangle} to propagate collision from
	 * @param colliders    {@code List} of {@code MovingRectangles} to calculate
	 *                     collision with
	 * @param collisionMap Set to {@code null}
	 * 
	 * @return { Δx, Δy } amount {@code rect} was pushed back
	 */
	// parameter collisionMap is used to track which Rectangles pushed each other
	// and how much
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
			collisionData = calculateCollision(rect, other);
			if (collisionData[0] == 0 && collisionData[1] == 0) {
				continue;
			}

			if (collisionData[0] != 0 && collisionData[1] != 0) {
				collisionData[0] = 0;  // ignore x movement in a corner collision
			}

			collisionData[0] = -correctGrowthForCollision(rect, -collisionData[0],
					true);
			collisionData[1] = -correctGrowthForCollision(rect, -collisionData[1],
					false);

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
		int startingX = rect.getX();
		int startingY = rect.getY();

		Stream.concat(sideRectangles.stream().filter(s -> s.isActingLikeWall()),
				walls.stream()).forEach(w -> collideWithWall(rect, w));

		return new int[] { rect.getX() - startingX, rect.getY() - startingY };
	}

	/**
	 * Computes how to move {@code rect} so that it no longer collides with
	 * {@code wall}.
	 * 
	 * @param rect {@code MovingRectangle} which is colliding with {@code wall}
	 * @param wall {@code Rectangle} to represent the wall. Usually a
	 *             {@code WallRectangle} or {@code SideRectangle}
	 */
	private void collideWithWall(MovingRectangle rect, Rectangle wall) {
		int[] collisionData = calculateCollision(wall, rect);
		if (collisionData[0] == 0 && collisionData[1] == 0) {
			return;
		}

		if (collisionData[0] != 0 && collisionData[1] != 0) {
			if (collisionData[1] > 0) {
				collisionData[1] = 0;
			}
			else {
				collisionData[0] = 0;
			}
		}

		collisionData[0] = correctGrowthForCollision(rect, collisionData[0], true);
		collisionData[1] = correctGrowthForCollision(rect, collisionData[1], false);

		if (collisionData[0] != 0) {
			fudgeCollision(collisionData, wall.getY() - rect.getY() - rect.getHeight(),
					WALL_COLLISION_LEEWAY_Y + GRAVITY, false);  // Need to add gravity
																  // here because
																  // grounded rectangles
																  // have not been
																  // pushed up to cancel
																  // it yet
			fudgeCollision(collisionData, wall.getY() + wall.getHeight() - rect.getY(),
					WALL_COLLISION_LEEWAY_Y, false);
		}
		else if (collisionData[1] > 0) {
			fudgeCollision(collisionData, wall.getX() - rect.getX() - rect.getWidth(),
					WALL_COLLISION_LEEWAY_X, true);
			fudgeCollision(collisionData, wall.getX() + wall.getWidth() - rect.getX(),
					WALL_COLLISION_LEEWAY_X, true);
		}

		rect.moveCollision(collisionData[0], collisionData[1]);
	}

	/**
	 * If {@code |sliverSize| <= threshold}, alters {@code movement} to be {@code 0}
	 * in one direction and {@code sliverSize} in the other.
	 * <p>
	 * In the context of a collision, let Rectangle A be pushing out Rectangle B. If
	 * Rectangle B is only colliding with a small sliver of Rectangle A, this
	 * changes the movement of Rectangle B from one direction (pushed out by the
	 * sliver) into another (bumped on top of the sliver).
	 * 
	 * @param movement   {@code int[]} containing movement information in x and y
	 *                   directions
	 * @param sliverSize amount of Rectangle A that Rectangle B is colliding with
	 * @param threshold  maximum amount of leeway
	 * @param isX        {@code true} if {@code sliverSize} and {@code threshold}
	 *                   represent values in the x direction
	 */
	private void fudgeCollision(int[] movement, int sliverSize, int threshold,
			boolean isX) {
		if (Math.abs(sliverSize) <= threshold) {
			movement[isX ? 0 : 1] = sliverSize;
			movement[isX ? 1 : 0] = 0;
		}
	}

	/**
	 * Calculate how to move {@code other} so that it does not intersedct
	 * {@code rect}. Returns { 0, 0 } if {@code rect == other}.
	 * <p>
	 * Usually, this will return a movement in only one direction (x or y) and the
	 * other direction will be {@code 0}. However, if {@code other} was not
	 * intersecting {@code rect} at all on the previous frame and is now
	 * intersecting {@code rect} on both the x and y axes, this will return a
	 * movement in both directions. This will usually happen when {@code other}
	 * clips the corner of {@code rect}. Moving {@code other} in either direction
	 * will be sufficient to resolve the collision with {@code rect}. The caller
	 * should decide which direction is appropriate for the situation.
	 * 
	 * @param rect  {@code Rectangle} that is considered stationary
	 * @param other {@code Rectangle} that will move
	 * 
	 * @return { Δx, Δy } amount to move {@code other} to resolve collision with
	 *         {@code rect}
	 */
	private int[] calculateCollision(Rectangle rect, Rectangle other) {
		if (rect == other) {
			return new int[] { 0, 0 };
		}

		int xChange = 0;
		int yChange = 0;
		boolean inBoundsX = rect.intersectsX(other);
		boolean inBoundsY = rect.intersectsY(other);
		// "Used to be" values so Rectangles can tell whether they should be moved in x
		// or y direction
		boolean usedToBeInBoundsX = rect.usedToIntersectX(other);
		boolean usedToBeInBoundsY = rect.usedToIntersectY(other);

		if (inBoundsX && inBoundsY) {
			if (usedToBeInBoundsX) {
				yChange = pullToY(rect, other);
			}
			if (usedToBeInBoundsY) {
				xChange = pullToX(rect, other);
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

		if (!rect.canPushX()) {
			xChange = 0;
		}
		if (!rect.canPushY()) {
			yChange = 0;
		}

		return new int[] { xChange, yChange };
	}

	/**
	 * Reduces the growth of {@code rect} if necessary and returns the new amount to
	 * move it by.
	 * 
	 * @param rect     {@code MovingRectangle} to consider
	 * @param movement amount {@code rect} is supposed to move by
	 * @param isX      {@code true} if {@code movement} represents a change in x
	 *                 position
	 * 
	 * @return how much to move {@code rect} in the given direction
	 */
	private int correctGrowthForCollision(MovingRectangle rect, int movement,
			boolean isX) {

		int lowerSideChange, upperSideChange;

		if (isX) {
			lowerSideChange = rect.getLeftWidthChange();
			upperSideChange = rect.getWidth() - rect.getLastWidth()
					- rect.getLeftWidthChange();
		}
		else {
			lowerSideChange = rect.getTopHeightChange();
			upperSideChange = rect.getHeight() - rect.getLastHeight()
					- rect.getTopHeightChange();
		}

		if (movement > 0 && lowerSideChange > 0) {
			movement -= lowerSideChange;
			if (isX) {
				rect.changeWidth(-lowerSideChange, true);
			}
			else {
				rect.changeHeight(-lowerSideChange, true);
			}
		}

		if (movement < 0 && upperSideChange > 0) {
			movement += upperSideChange;
			if (isX) {
				rect.changeWidth(-upperSideChange, false);
			}
			else {
				rect.changeHeight(-upperSideChange, false);
			}
		}

		return movement;
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
