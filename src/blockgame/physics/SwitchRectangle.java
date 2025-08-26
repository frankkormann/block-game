package blockgame.physics;

import java.awt.Color;
import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code MovingRectangle} with activity and a key. If this is not "active",
 * then it cannot interact with other {@code Rectangle}s (except
 * {@code SideRectangle}s, {@code WallRectangle}s, and {@code SwitchArea}s with
 * the same key) and it cannot move due to gravity. The key exists to pair it
 * with a {@code SwitchArea}.
 * 
 * @author Frank Kormann
 */
public class SwitchRectangle extends MovingRectangle {

	private static final int DASH_SIZE = 5;
	private static final int BORDER_THICKNESS = 2;

	private boolean isActive;
	private boolean wasActive;
	private String key;

	@JsonCreator
	public SwitchRectangle(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("color") Colors color,
			@JsonProperty("key") String key) {
		super(x, y, width, height, color);
		this.key = key;
		isActive = false;
	}

	@Override
	public void draw(Graphics g) {
		g = g.create();

		Color color = new Color(getColor().getRed(), getColor().getGreen(),
				getColor().getBlue(), isActive ? getColor().getAlpha() : 0);

		g.setColor(getBorderColor());
		drawDashedRectangle(g, color, DASH_SIZE, BORDER_THICKNESS, getX(),
				getY(), getWidth(), getHeight());

		g.setColor(color);
		g.fillRect(getX() + BORDER_THICKNESS, getY() + BORDER_THICKNESS,
				getWidth() - 2 * BORDER_THICKNESS,
				getHeight() - 2 * BORDER_THICKNESS);

		g.dispose();
	}

	@Override
	public void updateLastPosition() {
		super.updateLastPosition();
		wasActive = isActive;
	}

	@Override
	public boolean canInteract(Rectangle other) {
		return isActive || canAlwaysInteractWith(other);
	}

	@Override
	public boolean hasGravity() {
		return isActive && super.hasGravity();
	}

	@Override
	public boolean usedToIntersectX(Rectangle other) {
		return (wasActive || canAlwaysInteractWith(other))
				&& super.usedToIntersectX(other);
	}

	@Override
	public boolean usedToIntersectY(Rectangle other) {
		return (wasActive || canAlwaysInteractWith(other))
				&& super.usedToIntersectY(other);
	}

	/**
	 * Checks if this can interact with {@code other} regardless of activity.
	 * 
	 * @param other {@code Rectangle} to test
	 * 
	 * @return {@code true} if this can interact with {@code other}'s type
	 */
	private boolean canAlwaysInteractWith(Rectangle other) {
		if (other instanceof SwitchArea) {
			return ((SwitchArea) other).getKey().equals(key);
		}
		return other instanceof WallRectangle || other instanceof SideRectangle;
	}

	@Override
	public void addAttachment(Area attachment, AttachmentOption... options) {
		super.addAttachment(attachment, options);
		attachment.setActive(isActive);
	}

	/**
	 * Returns whether this is "active" or not. See {@link #setActive(boolean)}
	 * for more about about what "activity" means.
	 * 
	 * @return {@code true} if this is "active"
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * Sets whether this is "active" or not. When this is not "active", it
	 * cannot interact with other {@code Rectangle}s (except
	 * {@code SideRectangle}s, {@code WallRectangle}s, and {@code SwitchArea}s
	 * with the same key) or move due to gravity. This loses all velocity when
	 * set inactive.
	 * 
	 * @param active whether it should be "active"
	 */
	public void setActive(boolean active) {
		isActive = active;
		getAttachments().forEach(a -> a.setActive(active));
		if (!isActive) {
			setYVelocity(0);
		}
	}

	/**
	 * Returns if this just became active.
	 * 
	 * @return {@code true} if this was inactive on the previous frame and is
	 *         now active
	 */
	public boolean becameActive() {
		return !wasActive && isActive;
	}

	public String getKey() {
		return key;
	}

}
