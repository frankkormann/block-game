package blockgame.physics;

import blockgame.gui.MainFrame.Direction;

/**
 * {@code Rectangle} suitable for giving collision to the sides of a window.
 * <p>
 * While this stores whether it is supposed to be acting like a wall and is
 * responsible for knowing how it can interact with other {@code Rectangle}s,
 * the user of this class is responsible for interpreting that knowledge. On its
 * own, this has no safeguards to prevent unwanted behavior — for example, this
 * will happily execute {@code moveCollision} even when it is supposed to acting
 * like a wall.
 * 
 * @author Frank Kormann
 */
public class SideRectangle extends MovingRectangle {

	private Direction direction;
	private boolean actingLikeWall;

	public SideRectangle(int x, int y, int width, int height,
			Direction direction) {
		super(x, y, width, height);
		this.direction = direction;
		actingLikeWall = false;
	}

	@Override
	public boolean canInteract(Rectangle other) {
		if (other instanceof SideRectangle)
			return false;

		switch (other.getResizeBehavior()) {
			case STAY:
				return false;
			case PREVENT_X:
				return direction == Direction.WEST
						|| direction == Direction.EAST;
			case PREVENT_Y:
				return direction == Direction.NORTH
						|| direction == Direction.SOUTH;
			default:
				return true;
		}
	}

	@Override
	public boolean canPushX() {
		return direction == Direction.WEST || direction == Direction.EAST;
	}

	@Override
	public boolean canPushY() {
		return direction == Direction.NORTH || direction == Direction.SOUTH;
	}

	public Direction getDirection() {
		return direction;
	}

	public boolean isActingLikeWall() {
		return actingLikeWall;
	}

	public void setActLikeWall(boolean actLikeWall) {
		this.actingLikeWall = actLikeWall;
	}

}
