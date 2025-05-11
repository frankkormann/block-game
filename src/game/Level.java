package game;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Container for level information while reading level JSON.
 * 
 * @author Frank Kormann
 */

public class Level {

	private String name;
	private int width, height;
	private List<Rectangle> rectangles;

	@JsonCreator
	public Level(@JsonProperty("name") String name, @JsonProperty("width") int width,
			@JsonProperty("height") int height) {
		this.name = name;
		this.width = width;
		this.height = height;
		rectangles = null;
	}

	public void setRectangles(List<Rectangle> rectangles) {
		this.rectangles = rectangles;
	}

	public String getName() {
		return name;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public List<Rectangle> getRectangles() {
		return rectangles;
	}

}
