package game;

import java.awt.Color;

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
