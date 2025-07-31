package game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Applies a constant force every frame.
 * <p>
 * Specifically, each {@code MovingRectangle} within this will have a change in
 * velocity corresponding to {@code xForce} and {@code yForce} each frame.
 * 
 * @author Frank Kormann
 */
public class ForceArea extends Area {

	private int xForce, yForce;

	@JsonCreator
	public ForceArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("xForce") int xForce,
			@JsonProperty("yForce") int yForce) {
		super(x, y, width, height, Colors.TRANSLUCENT_BLUE);
		this.xForce = xForce;
		this.yForce = yForce;
	}

	/**
	 * Not implemented.
	 * 
	 * @param rect unused
	 */
	@Override
	protected void onEnter(MovingRectangle rect) {}

	/**
	 * Not implemented.
	 * 
	 * @param rect unused
	 */
	@Override
	protected void onExit(MovingRectangle rect) {}

	/**
	 * Applies a constant force to {@code rect}.
	 * 
	 * @param rect {@code MovingRectangle} to move
	 */
	@Override
	protected void everyFrame(MovingRectangle rect) {
		rect.setXVelocity(rect.getXVelocity() + xForce);
		rect.setYVelocity(rect.getYVelocity() + yForce);
	}

}
