package blockgame.physics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import blockgame.util.Pair;

/**
 * Calculates how to move all other {@code MovingRectangle}s when a
 * {@code MovingRectangle} moves. Takes into account {@code WallRectangle}s and
 * {@code SideRectangle}s which are acting like a wall.
 * <p>
 * The intended use of this class is to populate it with {@code Rectangle}s,
 * call {@link #propagateCollision()} to compute the collision, then throw this
 * instance away. Calling {@code propagateCollision} multiple times is not
 * supported and will result in an {@code IllegalStateException}.
 */
public class CollisionPropagator {

	private static int WALL_COLLISION_LEEWAY_X = 4;
	private static int WALL_COLLISION_LEEWAY_Y = 5;

	private MovingRectangle initialRect;
	private boolean completed;

	private Collection<MovingRectangle> colliders;
	private Collection<WallRectangle> walls;
	private Collection<SideRectangle> sides;

	/**
	 * Creates a {@code CollisionCalculator} with the given {@code walls} and
	 * {@code sides}.
	 * <p>
	 * {@code colliders}, {@code walls}, and {@code sides} will be unaltered,
	 * although the {@code MovingRectangle}s within {@code colliders} may be
	 * moved.
	 * 
	 * @param thatMoved {@code MovingRectangle} which collision should be
	 *                  propagated from
	 * @param colliders {@code MovingRectangle}s for {@code thatMoved} to push
	 * @param walls     {@code WallRectangle}s for {@code MovingRectangle}s to
	 *                  interact with
	 * @param sides     {@code SideRectangle}s for {@code MovingRectangle}s to
	 *                  interact with
	 */
	public CollisionPropagator(MovingRectangle thatMoved,
			Collection<MovingRectangle> colliders,
			Collection<WallRectangle> walls, Collection<SideRectangle> sides) {
		initialRect = thatMoved;
		completed = false;
		this.colliders = new ArrayList<>(colliders);
		this.walls = walls;
		this.sides = sides;
	}

	/**
	 * Moves {@code rect} so that it does not intersect any
	 * {@code WallRectangles}. Moves other {@code MovingRectangles} so that they
	 * do not intersect {@code rect}. Acts recursively on each
	 * {@code MovingRectangle} moved by {@code rect}.
	 * <p>
	 * This is intended to be called only once. Repeated calls will result in an
	 * {@code IllegalStateException}.
	 * 
	 * @param rect {@code MovingRectangle} to propagate collision from
	 * 
	 * @return { Δx, Δy } amount {@code rect} was pushed back
	 */
	public int[] propagateCollision() {
		if (completed) {
			throw new IllegalStateException("Already propagated collision");
		}
		completed = true;
		return propagateCollision(initialRect, new HashMap<>());
	}

	/*
	 * WallRectangle collisions are calculated before MovingRectangle
	 * collisions. If rect collides with two or more MovingRectangles, an extra
	 * alignment step is performed to make sure nothing was moved that should
	 * not have been.
	 * 
	 * collisionMap is used to track which MovingRectangles pushed each other
	 * and how much
	 */
	private int[] propagateCollision(MovingRectangle rect,
			Map<MovingRectangle, Pair<MovingRectangle, int[]>> collisionMap) {

		int[] collisionData;
		int[] pushedAmount = { 0, 0 };
		colliders.remove(rect);

		int[] wallPushback = handleCollisionWithWalls(rect);
		pushedAmount[0] += wallPushback[0];
		pushedAmount[1] += wallPushback[1];

		for (MovingRectangle other : new ArrayList<MovingRectangle>(
				colliders)) {

			collisionData = calculateCollision(rect, other);
			if (collisionData[0] == 0 && collisionData[1] == 0) {
				continue;
			}

			if (collisionData[0] != 0 && collisionData[1] != 0) {
				collisionData[1] = 0;
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

			int[] pushback = propagateCollision(other, collisionMap);

			if (collisionData[0] != 0) {  // rect should only be pushed back in
										  // the direction it pushed other
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
	 * Called by {@link #propagateCollision} to traverse through
	 * {@code collisionMap}. Pull {@code other} back to {@code rect} and pull
	 * the rectangles associated with {@code other} back to {@code other}.
	 * <p>
	 * Undoes the collision between {@code rect} and {@code other} if they
	 * should not have collided.
	 * 
	 * @param rect         {@code Rectangle} to align with
	 * @param other        {@code Rectangle} to pull back
	 * @param collisionMap {@code Map} of each {@code MovingRectangle} to how
	 *                     much it was pushed in each direction
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

		if (Math.abs(xChange) > Math.abs(pushedAmount[0])
				|| (!other.intersectsY(rect)
						&& !other.usedToIntersectY(other))) {
			xChange = -pushedAmount[0];
		}
		if (Math.abs(yChange) > Math.abs(pushedAmount[1])
				|| (!other.intersectsX(rect)
						&& !other.usedToIntersectX(rect))) {
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
	 * Move {@code rect} so that it does not collide with any walls.
	 * 
	 * @param rect {@code MovingRectangle} to move
	 * 
	 * @return { dx, dy } of amount {@code rect} was pushed back
	 */
	private int[] handleCollisionWithWalls(MovingRectangle rect) {

		int[] pushedBack = { 0, 0 };

		Stream.concat(sides.stream().filter(s -> s.isActingLikeWall()),
				walls.stream())
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
		// that would cause a problem are too contrived to worry about
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
		boolean inBoundsX = rect.intersectsX(other) && other.intersectsX(rect);
		boolean inBoundsY = rect.intersectsY(other) && other.intersectsY(rect);
		// "Used to be" values so Rectangles can tell whether they should be
		// moved in x or y direction
		boolean usedToBeInBoundsX = rect.usedToIntersectX(other)
				&& other.usedToIntersectX(rect);
		boolean usedToBeInBoundsY = rect.usedToIntersectY(other)
				&& other.usedToIntersectY(rect);

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
			// side, it must have collided between frames
			int xSign = (int) Math.signum(rect.getX() - other.getX());
			int ySign = (int) Math.signum(rect.getY() - other.getY());
			int xOldSign = (int) Math
					.signum(rect.getLastX() - other.getLastX());
			int yOldSign = (int) Math
					.signum(rect.getLastY() - other.getLastY());
			if ((xSign != xOldSign) && (inBoundsY || usedToBeInBoundsY)
					&& xOldSign != 0) {
				xChange = pullToX(rect, other);
			}
			if ((ySign != yOldSign) && (inBoundsX || usedToBeInBoundsX)
					&& yOldSign != 0) {
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

}
