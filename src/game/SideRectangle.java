package game;

public class SideRectangle extends MovingRectangle {

	private MainFrame.Direction direction;

	public SideRectangle(int x, int y, int width, int height,
			MainFrame.Direction direction) {
		super(x, y, width, height);
		this.direction = direction;
	}

	@Override
	public boolean canInteract(Rectangle other) {
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

	public MainFrame.Direction getDirection() {
		return direction;
	}

}
