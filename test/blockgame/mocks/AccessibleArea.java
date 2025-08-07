package blockgame.mocks;

import java.util.HashMap;
import java.util.Map;

import blockgame.physics.Area;
import blockgame.physics.MovingRectangle;
import blockgame.physics.Rectangle;

public class AccessibleArea extends Area {

	public boolean hasEntered;
	public boolean hasExited;
	public Map<Rectangle, Integer> callsToEveryframe;

	public AccessibleArea(int x, int y, int width, int height,
			Colors colorEnum) {
		super(x, y, width, height, colorEnum);
		hasEntered = false;
		hasExited = false;
		callsToEveryframe = new HashMap<>();
	}

	@Override
	public void onEnter(MovingRectangle rect) {
		hasEntered = true;
	}

	@Override
	public void onExit(MovingRectangle rect) {
		hasExited = true;
	}

	@Override
	public void everyFrame(MovingRectangle rect) {
		if (!callsToEveryframe.containsKey(rect)) {
			callsToEveryframe.put(rect, 0);
		}
		callsToEveryframe.put(rect, callsToEveryframe.get(rect) + 1);
	}

}
