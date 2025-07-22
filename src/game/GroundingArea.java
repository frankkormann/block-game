package game;

import java.awt.Color;

import game.MovingRectangle.State;

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

	private static int HEIGHT = 4;

	public GroundingArea(int x, int y, int width) {
		super(x, y, width, HEIGHT, new Color(0, 0, 0, 0));
	}

	/**
	 * Sets {@code rect}'s {@code State} to {@code ON_GROUND}.
	 * 
	 * @param rect {@code MovingRectangle} to change the state of
	 */
	@Override
	protected void onEnter(MovingRectangle rect) {
		rect.setState(State.ON_GROUND);
	}

	/**
	 * Sets {@code rect}'s {@code State} to {@code IN_AIR}.
	 * 
	 * @param rect {@code MovingRectangle} to change the state of
	 */
	@Override
	protected void onExit(MovingRectangle rect) {
		rect.setState(State.IN_AIR);
	}

	/**
	 * Sets {@code rect}'s {@code State} to {@code ON_GROUND}.
	 * 
	 * @param rect {@code MovingRectangle} to change the state of
	 */
	@Override
	protected void everyFrame(MovingRectangle rect) {
		rect.setState(State.ON_GROUND);
	}

}
