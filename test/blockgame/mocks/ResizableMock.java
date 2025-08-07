package blockgame.mocks;

import java.util.HashMap;
import java.util.Map;

import blockgame.gui.MainFrame.Direction;
import blockgame.input.Resizable;

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
