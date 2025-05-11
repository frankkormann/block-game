package game;

import java.awt.Color;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Decreases a {@code MovingRectangle}'s width and/or height to match the
 * width/height of this.
 * 
 * @see GrowArea
 * 
 * @author Frank Kormann
 */

public class ShrinkArea extends Area {

	public static final Color DEFAULT_COLOR = new Color(246, 34, 23, 128);

	private int xShrink, yShrink;

	public ShrinkArea() {
		this(0, 0, 0, 0, 0, 0);
	}

	// xShrink and yShrink actually represent half the shrink rate (it is applied
	// to each side independently)
	@JsonCreator
	public ShrinkArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width, @JsonProperty("height") int height,
			@JsonProperty("xShrink") int xShrink,
			@JsonProperty("yShrink") int yShrink) {
		super(x, y, width, height, DEFAULT_COLOR);
		this.xShrink = xShrink;
		this.yShrink = yShrink;
	}

	@Override
	public void onEnter(MovingRectangle rect) {}

	@Override
	public void onExit(MovingRectangle rect) {}

	/**
	 * Grow rect until it is as tall/wide as this
	 */
	@Override
	public void everyFrame(MovingRectangle rect) {
		if (rect.getX() < getX()) {
			rect.changeWidth(-xShrink, true);
		}
		if (rect.getX() + rect.getWidth() > getX() + getWidth()) {
			rect.changeWidth(-xShrink, false);
		}
		if (rect.getY() < getY()) {
			rect.changeHeight(-yShrink, true);
		}
		if (rect.getY() + rect.getHeight() > getY() + getHeight()) {
			rect.changeHeight(-yShrink, false);
		}
	}

}
