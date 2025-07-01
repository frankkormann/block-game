package game;

import java.awt.Color;
import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple {@code Rectangle} to represent immovable walls. Default
 * {@code ResizeBehavior} is {@code PREVENT}.
 * 
 * @author Frank Kormann
 */
public class WallRectangle extends Rectangle {

	public static final Color STAY_COLOR = new Color(229, 229, 229, 255);
	public static final Color PREVENT_COLOR = new Color(85, 85, 85, 255);

	@JsonCreator
	public WallRectangle(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width, @JsonProperty("height") int height,
			@JsonProperty("resizeBehavior") Rectangle.ResizeBehavior resizeBehavior) {
		this(x, y, width, height, STAY_COLOR, resizeBehavior);

		getAttachments().add(new GroundingArea(x, y - 1, width, 1));
	}

	@Override
	public void draw(Graphics g) {
		super.draw(g);
		g.setColor(PREVENT_COLOR);
		if (getResizeBehavior() == Rectangle.ResizeBehavior.PREVENT_X) {
			g.drawLine(getX(), getY(), getX(), getY() + getHeight());
			g.drawLine(getX() + getWidth(), getY(), getX() + getWidth(),
					getY() + getHeight());
			for (int y = getY(); y < getY() + getHeight(); y += 5) {
				g.drawLine(getX() + 5, y, getX(), y + 5);
				g.drawLine(getX() + getWidth(), y, getX() + getWidth() - 5, y + 5);
			}
		}
		if (getResizeBehavior() == Rectangle.ResizeBehavior.PREVENT_Y) {
			g.drawLine(getX(), getY(), getX() + getWidth(), getY());
			g.drawLine(getX(), getY() + getHeight(), getX() + getWidth(),
					getY() + getHeight());
			for (int x = getX(); x < getX() + getWidth(); x += 5) {
				g.drawLine(x, getY() + 5, x + 5, getY());
				g.drawLine(x, getY() + getHeight(), x + 5, getY() + getHeight() - 5);
			}
		}
	}

	public WallRectangle(int x, int y, int width, int height, Color color,
			Rectangle.ResizeBehavior resizeBehavior) {
		super(x, y, width, height, color, resizeBehavior);
	}

}
