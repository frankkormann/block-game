package game;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code Area} that sets the activity of {@code SwitchRectangle}s. As long as
 * there is at least one {@code MovingRectangle} within this, each of its
 * children will be active. Otherwise, they will all be inactive.
 * <p>
 * Children are usually added by matching this's {@code key} with their
 * {@code key}.
 *
 * @author Frank Kormann
 */
public class SwitchArea extends Area {

	private Set<SwitchRectangle> children;
	private int numberInside;
	private String key;

	@JsonCreator
	public SwitchArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("color") Colors colorEnum,
			@JsonProperty("key") String key) {
		super(x, y, width, height, colorEnum);
		this.key = key;
		children = new HashSet<>();
	}

	@Override
	protected void onEnter(MovingRectangle rect) {
		numberInside++;
		if (numberInside == 1) {
			children.forEach(r -> r.setActive(true));
		}
	}

	@Override
	protected void onExit(MovingRectangle rect) {
		numberInside--;
		if (numberInside == 0) {
			children.forEach(r -> r.setActive(false));
		}
	}

	@Override
	protected void everyFrame(MovingRectangle rect) {}

	public String getKey() {
		return key;
	}

	public Set<SwitchRectangle> getChildren() {
		return children;
	}

	public void addChild(SwitchRectangle child) {
		children.add(child);
	}

}
