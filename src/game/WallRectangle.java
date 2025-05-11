package game;

import java.awt.Color;

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
		this(x, y, width, height,
				resizeBehavior == Rectangle.ResizeBehavior.PREVENT ? PREVENT_COLOR
						: STAY_COLOR,
				resizeBehavior);
	}

	public WallRectangle(int x, int y, int width, int height, Color color,
			Rectangle.ResizeBehavior resizeBehavior) {
		super(x, y, width, height, color, resizeBehavior);
	}

}
