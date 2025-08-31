package blockgame.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blockgame.gui.MainFrame.Direction;
import blockgame.input.GameInputHandler.MovementInput;

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
	// SwitchRectangles should also be put into movingRectangles
	private List<SwitchRectangle> switchRectangles;
	private List<WallRectangle> walls;
	private List<Area> areas;
	private List<SwitchArea> switchAreas;
	private List<GoalArea> goals;
	private Map<Direction, SideRectangle> sides;

	private List<Area> areasToAdd;

	private Map<Direction, Integer> sideRectangleResizes;

	private String nextLevel;

	/**
	 * Creates an empty {@code PhysicsSimulator}.
	 */
	public PhysicsSimulator() {
		super();

		movingRectangles = new ArrayList<>();
		switchRectangles = new ArrayList<>();
		walls = new ArrayList<>();
		areas = new ArrayList<>();
		switchAreas = new ArrayList<>();
		goals = new ArrayList<>();
		sides = new HashMap<>();

		areasToAdd = new ArrayList<>();

		sideRectangleResizes = new HashMap<>();

		nextLevel = "";
	}

	/**
	 * Sets up {@code SideRectangles} that represent window edges and sets
	 * initial status of {@code SwitchRectangle}s.
	 * 
	 * @param width   Width of play area
	 * @param height  Height of play area
	 * @param xOffset X coordinate of top left corner
	 * @param yOffset Y coordinate of top left corner
	 */
	public void setUp(int width, int height, int xOffset, int yOffset) {
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

		applySwitchAreas();
	}

	public void add(MovingRectangle rect) {
		movingRectangles.add(rect);
		if (rect instanceof SwitchRectangle) {
			switchRectangles.add((SwitchRectangle) rect);
		}
	}

	public void add(WallRectangle wall) {
		walls.add(wall);
	}

	/**
	 * Adds {@code Area} at the beginning of the next frame.
	 * 
	 * @param area {@code Area} to add
	 */
	public void add(Area area) {
		areasToAdd.add(area);
	}

	private void addArea(Area area) {
		if (area instanceof SwitchArea) {
			switchAreas.add((SwitchArea) area);
		}
		else if (area instanceof GoalArea) {
			goals.add((GoalArea) area);
		}
		else {
			areas.add(area);
		}
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
		areasToAdd.forEach(a -> addArea(a));
		areasToAdd.clear();

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

			if (movementInputs.contains(MovementInput.UP) && rect.canJump()) {
				newYVelocity = PLAYER_JUMP_VELOCITY;
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

		// Sort by distance from top (tiebreak distance from left) of screen for
		// consistency
		movingRectangles.sort((r1, r2) -> r1.getX() - r2.getX());
		movingRectangles.sort((r1, r2) -> r1.getY() - r2.getY());

		for (SwitchRectangle rect : switchRectangles) {
			if (rect.becameActive()) {
				new CollisionPropagator(rect, movingRectangles, walls, sides)
						.propagateCollision();
			}
		}

		for (MovingRectangle rect : movingRectangles) {
			applyAreas(rect);
			applyNaturalForces(rect);

			if (rect.getXVelocity() == 0 && rect.getYVelocity() == 0
					&& !rect.hasMoved()) {
				continue;
			}

			rect.moveVelocity();

			new CollisionPropagator(rect, movingRectangles, walls, sides)
					.propagateCollision();
		}
	}

	/**
	 * Tests if any {@code MovingRectangle}s intersect any {@code SwitchArea}s
	 * to update the activity of {@code SwitchRectangle}s if necessary.
	 */
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
		sides.values().forEach(s -> s.setActLikeWall(false));

		for (SideRectangle side : sides.values()) {
			Direction direction = side.getDirection();
			int difference = 0;
			switch (direction) {
				// Width/height are super high to prevent bug where
				// MovingRectangles could phase through the floor because the
				// floor was not wide enough
				case NORTH:
					movingRectangles.sort((r1, r2) -> r1.getY() - r2.getY());
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
		sides.values().forEach(s -> s.setActLikeWall(true));

	}

	/**
	 * Resizes a side and handles its collision.
	 * <p>
	 * {@code movingRectangles} must be sorted by ascending distance from the
	 * side.
	 * 
	 * @param side      {@code SideRectangle} to move
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

		int[] pushedBack = new CollisionPropagator(side, movingRectangles,
				walls, sides).propagateCollision();

		sides.get(side.getDirection().getOpposite()).setActLikeWall(false);

		if (pushedBack[0] != 0) {  // Infer side's direction based on how it
			return pushedBack[0];  // collided
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
		goals.stream().filter(g -> g.hasWon()).forEach(g -> g.markUsed());
	}

	public Map<Direction, Integer> getResizes() {
		return sideRectangleResizes;
	}

}
