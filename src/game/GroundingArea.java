package game;

import java.awt.Color;

/**
 * Sets the {@code State} of {@code MovingRectangle}s -- {@code ON_GROUND} while
 * within this, {@code IN_AIR} when it leaves.
 * <p>
 * Each {@code MovingRectangle} and {@code WallRectangle} automatically creates
 * an instance of this attached to their north side, so that other
 * {@code MovingRectangles} can tell when they are standing on something.
 * 
 * @author Frank Kormann
 */
public class GroundingArea extends Area {

	public GroundingArea(int x, int y, int width, int height) {
		super(x, y, width, height, new Color(0, 0, 0, 0));
	}

	@Override
	public void onEnter(MovingRectangle rect) {
		rect.setState(MovingRectangle.State.ON_GROUND);
	}

	@Override
	public void onExit(MovingRectangle rect) {
		rect.setState(MovingRectangle.State.IN_AIR);
	}

	@Override
	public void everyFrame(MovingRectangle rect) {
		rect.setState(MovingRectangle.State.ON_GROUND);
	}

}
