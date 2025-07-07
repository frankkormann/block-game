package mocks;

import java.util.HashMap;
import java.util.Map;

import game.MainFrame.Direction;
import game.Resizable;

public class ResizableMock implements Resizable {

	public Map<Direction, Integer> resizes;

	public ResizableMock() {
		resizes = new HashMap<>();
	}

	@Override
	public void resize(int change, Direction direction) {
		resizes.put(direction, change);
	}

}
