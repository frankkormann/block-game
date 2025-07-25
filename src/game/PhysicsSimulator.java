package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import game.GameInputHandler.GameInput;
import game.MainFrame.Direction;
import game.MovingRectangle.State;

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
	private static int PLAYER_JUMP_VELOCITY = -18;
	private static int PLAYER_JUMP_CAP = -10;

	private static int WALL_COLLISION_LEEWAY_X = 4;
	private static int WALL_COLLISION_LEEWAY_Y = 5;

	private List<MovingRectangle> movingRectangles;
	private List<WallRectangle> walls;
	private List<Area> areas;
	private List<GoalArea> goals;
	private Map<Direction, SideRectangle> sideRectangles;

	private Map<Direction, Integer> sideRectangleResizes;

	private String nextLevel;

	/**
	 * Creates an empty {@code PhysicsSimulator}.
	 */
	public PhysicsSimulator() {
		super();

		movingRectangles = new ArrayList<>();
		walls = new ArrayList<>();
		areas = new ArrayList<>();
		goals = new ArrayList<>();
		sideRectangles = new HashMap<>();

		sideRectangleResizes = new HashMap<>();

		nextLevel = "";
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
		sideRectangles.put(Direction.NORTH,
				new SideRectangle(xOffset, yOffset, width, 1, Direction.NORTH));
		sideRectangles.put(Direction.SOUTH, new SideRectangle(xOffset,
				yOffset + height, width, 1, Direction.SOUTH));
		sideRectangles.put(Direction.WEST,
				new SideRectangle(xOffset, yOffset, 1, height, Direction.WEST));
		sideRectangles.put(Direction.EAST, new SideRectangle(xOffset + width,
				yOffset, 1, height, Direction.EAST));

		for (SideRectangle side : sideRectangles.values()) {
			for (Area attached : side.getAttachments()) {
				areas.add(attached);
			}
		}
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
	 * velocities from the previous frame, and natural forces (gravity,
	 * friction). Also resolves collision between {@code Rectangles} and apply
	 * all {@code Areas} that need to be applied.
	 * 
	 * @param gameInputs {@code Set} of {@code Input}s from the player this
	 *                   frame
	 * @param width      Width of the play area
	 * @param height     Height of the play area
	 * @param xOffset    X coordinate of top left corner
	 * @param yOffset    Y coordinate of top left corner
	 */
	public void updateAndMoveObjects(Set<GameInput> gameInputs, int width,
			int height, int xOffset, int yOffset) {

		applyInputsToPlayerRectangles(gameInputs);
		moveAllMovingRectangles();

		moveAllSides(width, height, xOffset, yOffset);
	}

	/**
	 * Sets the velocities of all {@code MovingRectangle}s with
	 * {@code isControlledByPlayer() == true} according to the player's
	 * {@code Input}s.
	 * 
	 * @param gameInputs {@code Set} of {@code Input}s which are pressed on this
	 *                   frame
	 */
	private void applyInputsToPlayerRectangles(Set<GameInput> gameInputs) {
		for (MovingRectangle rect : movingRectangles) {
			if (!rect.isControlledByPlayer()) {
				continue;
			}

			int newXVelocity = rect.getXVelocity();
			int newYVelocity = rect.getYVelocity();

			if (gameInputs.contains(GameInput.RIGHT)) {
				newXVelocity += PLAYER_X_ACCELERATION;
			}
			if (gameInputs.contains(GameInput.LEFT)) {
				newXVelocity -= PLAYER_X_ACCELERATION;
			}

			if (gameInputs.contains(GameInput.UP)) {
				if (rect.getState() == State.ON_GROUND) {
					newYVelocity = PLAYER_JUMP_VELOCITY;
				}
			}
			else if (rect.getYVelocity() < PLAYER_JUMP_CAP) {
				newYVelocity = PLAYER_JUMP_CAP;
			}

			rect.setXVelocity(newXVelocity);
			rect.setYVelocity(newYVelocity);
		}
	}

	/**
	 * For each {@code MovingRectangle}, applies {@code Area}s, applies friction
	 * and gravity, applies movement from velocity, and computes collision.
	 */
	private void moveAllMovingRectangles() {

		movingRectangles.forEach(r -> r.updateLastPosition());

		// sort by distance from bottom of screen for consistency
		movingRectangles.sort((r1, r2) -> r2.getY() + r2.getHeight() - r1.getY()
				- r1.getHeight());

		for (MovingRectangle rect : movingRectangles) {

			applyAreas(rect);
			applyNaturalForces(rect);

			if (rect.getXVelocity() == 0 && rect.getYVelocity() == 0
					&& !rect.hasMoved()) {
				continue;
			}

			rect.moveVelocity();

			propagateCollision(rect, movingRectangles, null);
		}

	}

	/**
	 * Tests if {@code rect} intersects any {@code Area}s and applies effects of
	 * any it does intersect.
	 * 
	 * @param rect {@code MovingRectangle} to consider
	 */
	private void applyAreas(MovingRectangle rect) {
		for (Area area : areas) {
			area.handle(rect);
		}
		applyGoalAreas(rect);
	}

	/**
	 * Tests {@code rect} against {@code GoalArea}s and updates
	 * {@code nextLevel} if necessary.
	 * 
	 * @param rect {@code MovingRectangle} to consider
	 */
	private void applyGoalAreas(MovingRectangle rect) {
		for (GoalArea goal : goals) {
			goal.handle(rect);

			if (goal.hasWon()) {
				nextLevel = goal.getNextLevel();
				goal.markUsed();
			}
		}
	}

	/**
	 * Accelerates {@code rect} downward due to gravity and reduces x-velocity
	 * due to friction if appropriate.
	 *
	 * @param rect {@code MovingRectangle} to consider
	 */
	private void applyNaturalForces(MovingRectangle rect) {
		if (rect.hasGravity() && rect.getState() == State.IN_AIR) {
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
	 * them. Add appropriate resize values to {@code sideRectangleResizes} if
	 * the current window size is too small.
	 * 
	 * @param width   Width of play area
	 * @param height  Height of play area
	 * @param xOffset X coordinate of top left corner
	 * @param yOffset Y coordinate of top left corner
	 */
	private void moveAllSides(int width, int height, int xOffset, int yOffset) {

		sideRectangleResizes.clear();

		sideRectangles.values().forEach(s -> s.updateLastPosition());

		for (SideRectangle side : sideRectangles.values()) {
			Direction direction = side.getDirection();
			int difference = 0;
			switch (direction) {
				// Width/height are super high to prevent bug where
				// MovingRectangles
				// could phase through the floor because the floor was not wide
				// enough
				case NORTH:
					movingRectangles.sort((r1, r2) -> r2.getY() - r1.getY());
					difference = calculateCollisionForSide(side,
							xOffset - 50 * width, yOffset - side.getHeight(),
							101 * width, side.getHeight());
					break;
				case SOUTH:
					movingRectangles.sort((r1, r2) -> r2.getY() + r2.getHeight()
							- r1.getY() - r1.getHeight());
					difference = calculateCollisionForSide(side,
							xOffset - 50 * width, yOffset + height, 101 * width,
							side.getHeight());
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
					difference = calculateCollisionForSide(side,
							xOffset + width, yOffset - 50 * height,
							side.getWidth(), 101 * height);
					break;
			}
			sideRectangleResizes.put(direction, difference);
		}

	}

	/**
	 * Resizes a side and handles its collision.
	 * <p>
	 * {@code movingRectangles} must be sorted by ascending distance from the
	 * side.
	 * 
	 * @param side      Side to move
	 * @param newX      New x position
	 * @param newY      New y position
	 * @param newWidth  New width
	 * @param newHeight New height
	 * 
	 * @return Amount side was pushed back during collision
	 */
	private int calculateCollisionForSide(SideRectangle side, int newX,
			int newY, int newWidth, int newHeight) {
		side.setX(newX);
		side.setY(newY);
		side.setWidth(newWidth);
		side.setHeight(newHeight);

		sideRectangles.get(side.getDirection().getOpposite())
				.setActLikeWall(true);

		int[] pushedBack = propagateCollision(side, movingRectangles, null);

		sideRectangles.get(side.getDirection().getOpposite())
				.setActLikeWall(false);

		if (pushedBack[0] != 0) {  // Infer side's direction based on how it
									  // collided
			return pushedBack[0];
		}
		return pushedBack[1];
	}

	/**
	 * Moves {@code rect} so that it does not intersect any
	 * {@code WallRectangles}. Moves other {@code MovingRectangles} so that they
	 * do not intersect {@code rect}. Acts recursively on each
	 * {@code MovingRectangle} moved by {@code rect}.
	 * <p>
	 * {@code WallRectangle} collisions are calculated before
	 * {@code MovingRectangle} collisions. If {@code rect} collides with two or
	 * more {@code MovingRectangles}, an extra alignment step is performed to
	 * make sure nothing was moved that should not have been.
	 * <p>
	 * Note: WallRectangles are read from global
	 * {@code List<WallRectangle> walls}
	 * 
	 * @param rect         {@code MovingRectangle} to propagate collision from
	 * @param colliders    {@code List} of {@code MovingRectangles} to calculate
	 *                     collision with
	 * @param collisionMap Set to {@code null}
	 * 
	 * @return { Δx, Δy } amount {@code rect} was pushed back
	 */
	// parameter collisionMap is used to track which Rectangles pushed each
	// other
	// and how much
	private int[] propagateCollision(MovingRectangle rect,
			List<MovingRectangle> colliders,
			Map<MovingRectangle, Pair<MovingRectangle, int[]>> collisionMap) {

		if (collisionMap == null) {
			collisionMap = new HashMap<>();
		}

		int[] collisionData;
		int[] pushedAmount = { 0, 0 };
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
				if (collisionData[1] < 0) {
					collisionData[1] = 0;
				}
				else {
					collisionData[0] = 0;
				}
			}

			collisionData[0] = -correctGrowthForCollision(rect,
					-collisionData[0], true);
			collisionData[1] = -correctGrowthForCollision(rect,
					-collisionData[1], false);

			collisionData[0] = correctGrowthForCollision(other,
					collisionData[0], true);
			collisionData[1] = correctGrowthForCollision(other,
					collisionData[1], false);

			other.moveCollision(collisionData[0], collisionData[1]);
			collisionMap.put(other,
					new Pair<MovingRectangle, int[]>(rect, collisionData));

			int[] pushback = propagateCollision(other, colliders, collisionMap);

			if (collisionData[0] != 0) {  // rect should only be pushed back in
										  // the
										  // direction it pushed other
				rect.moveCollision(pushback[0], 0);
				pushedAmount[0] += pushback[0];
			}
			else {
				rect.moveCollision(0, pushback[1]);
				pushedAmount[1] += pushback[1];
			}

		}

		// Pull back Rectangles that collided to be aligned with the edge of
		// this
		for (MovingRectangle c : collisionMap.keySet()) {
			if (collisionMap.get(c).first == rect) {
				pullback(rect, c, collisionMap);
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

		int[] pushedBack = { 0, 0 };

		Stream.concat(sideRectangles.values()
				.stream()
				.filter(s -> s.isActingLikeWall()), walls.stream())
				.map(w -> collideWithWall(rect, w))
				.forEach(a -> {
					pushedBack[0] += a[0];
					pushedBack[1] += a[1];
				});

		return pushedBack;
	}

	/**
	 * Computes how to move {@code rect} so that it no longer collides with
	 * {@code wall}.
	 * 
	 * @param rect {@code MovingRectangle} which is colliding with {@code wall}
	 * @param wall {@code Rectangle} to represent the wall. Usually a
	 *             {@code WallRectangle} or {@code SideRectangle}
	 * 
	 * @return { dx, dy } of amount {@code rect} was pushed back
	 */
	private int[] collideWithWall(MovingRectangle rect, Rectangle wall) {

		int[] collisionData = calculateCollision(wall, rect);

		if (collisionData[0] == 0 && collisionData[1] == 0) {
			return new int[] { 0, 0 };
		}

		if (collisionData[0] != 0 && collisionData[1] != 0) {
			if (collisionData[1] > 0) {
				collisionData[1] = 0;
			}
			else {
				collisionData[0] = 0;
			}
		}

		collisionData[0] = correctGrowthForCollision(rect, collisionData[0],
				true);
		collisionData[1] = correctGrowthForCollision(rect, collisionData[1],
				false);

		int[] originalMovement = { collisionData[0], collisionData[1] };

		if (collisionData[0] != 0) {
			fudgeCollision(collisionData,
					wall.getY() - rect.getY() - rect.getHeight(),
					WALL_COLLISION_LEEWAY_Y, false);
			fudgeCollision(collisionData,
					wall.getY() + wall.getHeight() - rect.getY(),
					WALL_COLLISION_LEEWAY_Y, false);
		}
		else if (collisionData[1] > 0) {
			fudgeCollision(collisionData,
					wall.getX() - rect.getX() - rect.getWidth(),
					WALL_COLLISION_LEEWAY_X, true);
			fudgeCollision(collisionData,
					wall.getX() + wall.getWidth() - rect.getX(),
					WALL_COLLISION_LEEWAY_X, true);
		}

		// This does not check whether the fudged collision would push another
		// MovingRectangle into a wall, but any examples I could think of where
		// that
		// would cause a problem are too contrived to worry about
		// Also, implementing that check would be much more difficult
		if (wouldIntersectAWall(rect, collisionData[0], collisionData[1])) {
			collisionData = originalMovement;
		}

		rect.moveCollision(collisionData[0], collisionData[1]);

		return collisionData;
	}

	/**
	 * If {@code |sliverSize| <= threshold}, alters {@code movement} to be
	 * {@code 0} in one direction and {@code sliverSize} in the other.
	 * <p>
	 * In the context of a collision, let Rectangle A be pushing out Rectangle
	 * B. If Rectangle B is only colliding with a small sliver of Rectangle A,
	 * this changes the movement of Rectangle B from one direction (pushed out
	 * by the sliver) into another (bumped on top of the sliver).
	 * 
	 * @param movement   {@code int[]} containing movement information in x and
	 *                   y directions
	 * @param sliverSize amount of Rectangle A that Rectangle B is colliding
	 *                   with
	 * @param threshold  maximum amount of leeway
	 * @param isX        {@code true} if {@code sliverSize} and
	 *                   {@code threshold} represent values in the x direction
	 */
	private void fudgeCollision(int[] movement, int sliverSize, int threshold,
			boolean isX) {
		if (Math.abs(sliverSize) <= threshold) {
			movement[isX ? 0 : 1] = sliverSize;
			movement[isX ? 1 : 0] = 0;
		}
	}

	/**
	 * Returns {@code true} if {@code rect} would intersect a
	 * {@code WallRectangle} given the proposed movement.
	 * 
	 * @param rect    {@code MovingRectangle} to consider
	 * @param xChange proposed change in the x direction
	 * @param yChange proposed change in the y direction
	 * 
	 * @return {@code true} if {@code rect} would intersect a wall
	 */
	private boolean wouldIntersectAWall(MovingRectangle rect, int xChange,
			int yChange) {
		MovingRectangle potentialRect = new MovingRectangle(
				rect.getX() + xChange, rect.getY() + yChange, rect.getWidth(),
				rect.getHeight());

		for (WallRectangle wall : walls) {
			if (wall.intersectsX(potentialRect)
					&& wall.intersectsY(potentialRect)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Calculate how to move {@code other} so that it does not intersect
	 * {@code rect}. Returns <code>{ 0, 0 }</code> if {@code rect == other}.
	 * <p>
	 * Usually, this will return a movement in only one direction (x or y) and
	 * the other direction will be {@code 0}. However, if {@code other} was not
	 * intersecting {@code rect} at all on the previous frame and is now
	 * intersecting {@code rect} on both the x and y axes, this will return a
	 * movement in both directions. This will usually happen when {@code other}
	 * clips the corner of {@code rect}. Moving {@code other} in either
	 * direction will be sufficient to resolve the collision with {@code rect}.
	 * The caller should decide which direction is appropriate for the
	 * situation.
	 * <p>
	 * {@code calculateCollision(rect, other)} will always return the negative
	 * of {@code calculateCollision(other, rect)}.
	 * 
	 * @param rect  {@code Rectangle} that is considered stationary
	 * @param other {@code MovingRectangle} that will move
	 * 
	 * @return { Δx, Δy } amount to move {@code other} to resolve collision with
	 *         {@code rect}
	 */
	private int[] calculateCollision(Rectangle rect, MovingRectangle other) {
		if (rect == other) {
			return new int[] { 0, 0 };
		}

		int xChange = 0;
		int yChange = 0;
		boolean inBoundsX = other.intersectsX(rect);
		boolean inBoundsY = other.intersectsY(rect);
		// "Used to be" values so Rectangles can tell whether they should be
		// moved in x
		// or y direction
		boolean usedToBeInBoundsX = other.usedToIntersectX(rect);
		boolean usedToBeInBoundsY = other.usedToIntersectY(rect);

		if (inBoundsX && inBoundsY) {
			if (usedToBeInBoundsX) {
				yChange = pullToY(rect, other);
			}
			if (usedToBeInBoundsY) {
				xChange = pullToX(rect, other);
			}
			if (!usedToBeInBoundsX && !usedToBeInBoundsY) {
				xChange = pullToX(rect, other);
				yChange = pullToY(rect, other);
			}
		}
		// These account for when a rectangle would pass through another
		// diagonally
		else if (inBoundsX && usedToBeInBoundsY) {
			xChange = pullToX(rect, other);
		}
		else if (inBoundsY && usedToBeInBoundsX) {
			yChange = pullToY(rect, other);
		}
		else {
			// If other started on one side of this and ended up on the opposite
			// side,
			// it must have collided between frames
			int xSign = (int) Math.signum(rect.getX() - other.getX());
			int ySign = (int) Math.signum(rect.getY() - other.getY());
			int xOldSign = (int) Math
					.signum(rect.getLastX() - other.getLastX());
			int yOldSign = (int) Math
					.signum(rect.getLastY() - other.getLastY());
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
	 * Reduces the growth of {@code rect} if necessary and returns the new
	 * amount to move it by.
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
	 * {@code collisionMap}. Pull {@code other} back to {@code rect} and pull
	 * the rectangles associated with {@code other} back to {@code other}.
	 * 
	 * @param rect         {@code Rectangle} to align with
	 * @param other        {@code Rectangle} to pull back
	 * @param collisionMap {@code Map} of each {@code MovingRectangle} to how
	 *                     much it was pushed by in each direction
	 * @param direction    boolean representing direction; {@code true} for x
	 *                     and {@code false} for y
	 */
	private void pullback(Rectangle rect, MovingRectangle other,
			Map<MovingRectangle, Pair<MovingRectangle, int[]>> collisionMap) {
		int xChange = 0;
		int yChange = 0;

		int[] pushedAmount = collisionMap.get(other).second;

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
			if (collisionMap.get(c).first == other) {
				pullback(other, c, collisionMap);
			}
		}
	}

	/**
	 * Calculate how far to move {@code other} to be adjacent to {@code rect} in
	 * the x direction.
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
	 * Calculate how far to move {@code other} to be adjacent to {@code rect} in
	 * the y direction.
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

	/**
	 * Returns the resource for the next level if a {@code GoalArea} has been
	 * activated. Otherwise, returns the empty string.
	 * 
	 * @return resource name of the next level, or the empty string if there is
	 *         no next level yet
	 */
	public String getNextLevel() {
		return nextLevel;
	}

	/**
	 * Sets the next level as returned by {@code getNextLevel()} to the empty
	 * string, even if a {@code GoalArea} has been activated.
	 */
	public void resetNextlevel() {
		nextLevel = "";
	}

	public Map<Direction, Integer> getResizes() {
		return sideRectangleResizes;
	}

}
