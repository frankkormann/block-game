package mocks;

import java.awt.Color;
import java.awt.Graphics;

import game.Drawable;

public class DrawableMock implements Drawable {

	public int x, y, width, height;
	public Color color;

	public DrawableMock(int x, int y, int width, int height, Color color) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = color;
	}

	@Override
	public void draw(Graphics g) {
		g = g.create();
		g.setColor(color);
		g.drawRect(x, y, width, height);
		g.dispose();
	}

}
