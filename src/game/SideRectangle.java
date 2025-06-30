package game;

public class SideRectangle extends MovingRectangle {

	private MainFrame.Direction direction;
	private boolean actingLikeWall;

	public SideRectangle(int x, int y, int width, int height,
			MainFrame.Direction direction) {
		super(x, y, width, height);
		this.direction = direction;
		actingLikeWall = true;
	}

	@Override
	public boolean canInteract(Rectangle other) {
		if (other instanceof SideRectangle)
			return false;

		switch (other.getResizeBehavior()) {
			case STAY:
				return false;
			case PREVENT_X:
				return direction == MainFrame.Direction.WEST
						|| direction == MainFrame.Direction.EAST;
			case PREVENT_Y:
				return direction == MainFrame.Direction.NORTH
						|| direction == MainFrame.Direction.SOUTH;
			default:
				return true;
		}
	}

	@Override
	public boolean canPushX() {
		return direction == MainFrame.Direction.WEST
				|| direction == MainFrame.Direction.EAST;
	}

	@Override
	public boolean canPushY() {
		return direction == MainFrame.Direction.NORTH
				|| direction == MainFrame.Direction.SOUTH;
	}

	public MainFrame.Direction getDirection() {
		return direction;
	}

	public boolean isActingLikeWall() {
		return actingLikeWall;
	}

	public void setActLikeWall(boolean actLikeWall) {
		this.actingLikeWall = actLikeWall;
	}

}
