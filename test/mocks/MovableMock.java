package mocks;

import game.Movable;

public class MovableMock implements Movable {

	public int totalXChange, totalYChange;

	public MovableMock() {
		totalXChange = 0;
		totalYChange = 0;
	}

	@Override
	public void move2(int xDifference, int yDifference) {
		totalXChange += xDifference;
		totalYChange += yDifference;
	}

}
