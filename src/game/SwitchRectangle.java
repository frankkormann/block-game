package game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code MovingRectangle} with activity and a key. If this is not "active",
 * then it cannot interact with other {@code Rectangle}s and it cannot move due
 * to gravity. The key exists to pair it with a {@code SwitchArea}.
 * 
 * @author Frank Kormann
 */
public class SwitchRectangle extends MovingRectangle {

	private String key;
	private boolean isActive;

	@JsonCreator
	public SwitchRectangle(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("color") Colors color,
			@JsonProperty("key") String key) {
		super(x, y, width, height, color);
		this.key = key;
		isActive = true;
	}

	@Override
	public boolean canInteract(Rectangle other) {
		return isActive;
	}

	@Override
	public boolean canPushX() {
		return isActive;
	}

	@Override
	public boolean canPushY() {
		return isActive;
	}

	@Override
	public boolean hasGravity() {
		return super.hasGravity() && isActive;
	}

	public String getKey() {
		return key;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
		getAttachments().forEach(a -> a.setActive(active));
	}

}
