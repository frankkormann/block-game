package game;

import java.awt.Color;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple {@code Area} that applies a constant force.
 * 
 * Specifically, each {@code MovingRectangle} within this will have a change in
 * velocity corresponding to {@code xForce} and {@code yForce} each frame.
 * 
 * @author Frank Kormann
 */
public class ForceArea extends Area {

	public static final Color DEFAULT_COLOR = new Color(21, 137, 255, 96);

	private int xForce, yForce;

	public ForceArea() {
		this(0, 0, 0, 0, 0, 0);
	}

	@JsonCreator
	public ForceArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width, @JsonProperty("height") int height,
			@JsonProperty("xForce") int xForce, @JsonProperty("yForce") int yForce) {
		super(x, y, width, height, DEFAULT_COLOR);
		this.xForce = xForce;
		this.yForce = yForce;
	}

	@Override
	public void onEnter(MovingRectangle rect) {}

	@Override
	public void onExit(MovingRectangle rect) {}

	@Override
	public void everyFrame(MovingRectangle rect) {
		rect.setXVelocity(rect.getXVelocity() + xForce);
		rect.setYVelocity(rect.getYVelocity() + yForce);
	}

}
