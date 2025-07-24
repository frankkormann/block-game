package game;

import java.awt.Color;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Disables a {@code MovingRectangle}'s gravity on enter and re-enables it on
 * exit.
 * 
 * @author Frank Kormann
 */
public class AntigravityArea extends Area {

	public static final Color DEFAULT_COLOR = new Color(255, 119, 255, 96);

	public AntigravityArea() {
		this(0, 0, 0, 0);
	}

	@JsonCreator
	public AntigravityArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height) {
		super(x, y, width, height, DEFAULT_COLOR);
	}

	/**
	 * Removes {@code rect}'s gravity.
	 * 
	 * @param rect {@code MovingRectangle} to affect
	 */
	@Override
	protected void onEnter(MovingRectangle rect) {
		rect.setHasGravity(false);
	}

	/**
	 * Gives {@code rect} gravity.
	 * 
	 * @param rect {@code MovingRectangle} to affect
	 */
	@Override
	protected void onExit(MovingRectangle rect) {
		rect.setHasGravity(true);
	}

	/**
	 * Removes {@code rect}'s gravity.
	 * 
	 * @param rect {@code MovingRectangle} to affect
	 */
	@Override
	protected void everyFrame(MovingRectangle rect) {
		rect.setHasGravity(false);
	}

}
