package game;

import java.awt.Color;

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
	public void onEnter(MovingRectangle rect) {}

	@Override
	public void onExit(MovingRectangle rect) {}

	/**
	 * Grow {@code rect} until it is as tall/wide as this
	 */
	@Override
	public void everyFrame(MovingRectangle rect) {
		if (rect.getX() > getX()) {
			rect.changeWidth(xGrowth, true);
		}
		if (rect.getX() + rect.getWidth() < getX() + getWidth()) {
			rect.changeWidth(xGrowth, false);
		}
		if (rect.getY() > getY()) {
			rect.changeHeight(yGrowth, true);
		}
		if (rect.getY() + rect.getHeight() < getY() + getHeight()) {
			rect.changeHeight(yGrowth, false);
		}
	}

}
