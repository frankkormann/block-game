package game;

import java.awt.Color;
import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Increases a {@code MovingRectangle}'s width and/or height to match the
 * width/height of this.
 * 
 * @see ShrinkArea
 * 
 * @author Frank Kormann
 */
public class GrowArea extends Area {

	public static final Color DEFAULT_COLOR = new Color(22, 245, 41, 128);

	private int xGrowth, yGrowth;

	public GrowArea() {
		this(0, 0, 0, 0, 0, 0);
	}

	// xGrowth and yGrowth actually represent half the growth rate (it is applied
	// to each side independently)
	@JsonCreator
	public GrowArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width, @JsonProperty("height") int height,
			@JsonProperty("xGrowth") int xGrowth,
			@JsonProperty("yGrowth") int yGrowth) {
		super(x, y, width, height, DEFAULT_COLOR);
		this.xGrowth = xGrowth;
		this.yGrowth = yGrowth;
	}

	@Override
	public void draw(Graphics g) {
		super.draw(g);

		int x = getX();
		int y = getY();
		g.setColor(getColor().darker());
		if (xGrowth != 0) {
			drawHorizontalArrow(g, getX() + 10, getY() + (getHeight() / 2), 10, 20,
					getWidth() - 40, 8);
		}
		if (yGrowth != 0) {
			drawVerticalArrow(g, getX() + (getWidth() / 2), getY() + 10, 20, 10, 8,
					getHeight() - 40);
		}
	}

	@Override
	public void onEnter(MovingRectangle rect) {}

	@Override
	public void onExit(MovingRectangle rect) {}

	/**
	 * Grow {@code rect} until it is as tall/wide as this
	 */
	@Override
	public void everyFrame(MovingRectangle rect) {
		int possibleChange;

		possibleChange = rect.getX() - getX();
		if (possibleChange > 0) {
			rect.changeWidth(Math.min(xGrowth, possibleChange), true);
		}

		possibleChange = getX() + getWidth() - rect.getX() - rect.getWidth();
		if (possibleChange > 0) {
			rect.changeWidth(Math.min(xGrowth, possibleChange), false);
		}

		possibleChange = rect.getY() - getY();
		if (possibleChange > 0) {
			rect.changeHeight(Math.min(yGrowth, possibleChange), true);
		}

		possibleChange = getY() + getHeight() - rect.getY() - rect.getHeight();
		if (possibleChange > 0) {
			rect.changeHeight(Math.min(yGrowth, possibleChange), false);

		}
	}

}
