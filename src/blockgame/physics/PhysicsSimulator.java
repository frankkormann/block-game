package blockgame.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blockgame.gui.MainFrame.Direction;
import blockgame.input.GameInputHandler.MovementInput;
import blockgame.physics.MovingRectangle.State;

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

	private List<MovingRectangle> movingRectangles;
	private List<WallRectangle> walls;
	private List<Area> areas;
	private List<SwitchArea> switchAreas;
	private List<GoalArea> goals;
	private Map<Direction, SideRectangle> sides;

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
		switchAreas = new ArrayList<>();
		goals = new ArrayList<>();
		sides = new HashMap<>();

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
		sides.put(Direction.NORTH,
				new SideRectangle(xOffset, yOffset, width, 1, Direction.NORTH));
		sides.put(Direction.SOUTH, new SideRectangle(xOffset, yOffset + height,
				width, 1, Direction.SOUTH));
		sides.put(Direction.WEST,
				new SideRectangle(xOffset, yOffset, 1, height, Direction.WEST));
		sides.put(Direction.EAST, new SideRectangle(xOffset + width, yOffset, 1,
				height, Direction.EAST));

		for (SideRectangle side : sides.values()) {
			for (Area attached : side.getAttachments()) {
				areas.add(attached);
			}
		}
	}

	public void add(MovingRectangle rect) {
		movingRectangles.add(rect);
	}

	public void add(WallRectangle wall) {
		walls.add(wall);
	}

	public void add(Area area) {
		areas.add(area);
	}

	public void add(GoalArea goal) {
		goals.add(goal);
	}

	public void add(SwitchArea switchArea) {
		switchAreas.add(switchArea);
	}

	/**
	 * This should be called every frame to calculate the next frame.
	 * <p>
	 * Moves all {@code MovingRectangles} according to player input, their
	 * velocities from the previous frame, and natural forces (gravity,
	 * friction). Also resolves collision between {@code Rectangles} and apply
	 * all {@code Areas} that need to be applied.
	 * 
	 * @param movementInputs {@code Set} of {@code Input}s from the player this
	 *                       frame
	 * @param width          Width of the play area
	 * @param height         Height of the play area
	 * @param xOffset        X coordinate of top left corner
	 * @param yOffset        Y coordinate of top left corner
	 */
	public void updateAndMoveObjects(Set<MovementInput> movementInputs,
			int width, int height, int xOffset, int yOffset) {

		applyInputsToPlayerRectangles(movementInputs);
		moveAllMovingRectangles();

		moveAllSides(width, height, xOffset, yOffset);
	}

	/**
	 * Sets the velocities of all {@code MovingRectangle}s with
	 * {@code isControlledByPlayer() == true} according to the player's
	 * {@code Input}s.
	 * 
	 * @param movementInputs {@code Set} of {@code Input}s which are pressed on
	 *                       this frame
	 */
	private void applyInputsToPlayerRectangles(
			Set<MovementInput> movementInputs) {
		for (MovingRectangle rect : movingRectangles) {
			if (!rect.isControlledByPlayer()) {
				continue;
			}

			int newXVelocity = rect.getXVelocity();
			int newYVelocity = rect.getYVelocity();

			if (movementInputs.contains(MovementInput.RIGHT)) {
				newXVelocity += PLAYER_X_ACCELERATION;
			}
			if (movementInputs.contains(MovementInput.LEFT)) {
				newXVelocity -= PLAYER_X_ACCELERATION;
			}

			if (movementInputs.contains(MovementInput.UP)) {
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
		applySwitchAreas();  // Make sure activity doesn't change mid-frame

		// Sort by distance from bottom of screen for consistency
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

			new CollisionPropagator(walls, sides.values())
					.propagateCollision(rect, movingRectangles, null);
		}

	}

	public void applySwitchAreas() {
		for (SwitchArea area : switchAreas) {
			for (MovingRectangle rect : movingRectangles) {
				area.handle(rect);
			}
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

		sides.values().forEach(s -> s.updateLastPosition());

		for (SideRectangle side : sides.values()) {
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

		sides.get(side.getDirection().getOpposite()).setActLikeWall(true);

		int[] pushedBack = new CollisionPropagator(walls, sides.values())
				.propagateCollision(side, movingRectangles, null);

		sides.get(side.getDirection().getOpposite()).setActLikeWall(false);

		if (pushedBack[0] != 0) {  // Infer side's direction based on how it
									  // collided
			return pushedBack[0];
		}
		return pushedBack[1];
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
