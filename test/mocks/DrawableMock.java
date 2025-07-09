package mocks;

import java.awt.Graphics;

import game.Drawable;

public class DrawableMock implements Drawable {

	public int x, y, width, height;

	public DrawableMock(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public void draw(Graphics g) {
		g.drawRect(x, y, width, height);
	}

}
