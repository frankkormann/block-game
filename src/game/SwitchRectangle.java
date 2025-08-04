package game;

import java.awt.Color;
import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import game.ParameterMapper.Parameter;

/**
 * {@code MovingRectangle} with activity and a key. If this is not "active",
 * then it cannot interact with other {@code Rectangle}s (except
 * {@code SideRectangle}s) and it cannot move due to gravity. The key exists to
 * pair it with a {@code SwitchArea}.
 * 
 * @author Frank Kormann
 */
public class SwitchRectangle extends MovingRectangle {

	private static final int TICK_SIZE = 5;
	private static final int BORDER_THICKNESS = 2;
	private static final float BORDER_DARKNESS = 1.2f;

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

		Color color = getColor();

		Color border = new Color((int) (color.getRed() / BORDER_DARKNESS),
				(int) (color.getGreen() / BORDER_DARKNESS),
				(int) (color.getBlue() / BORDER_DARKNESS), color.getAlpha());
		g.setColor(border);
		drawTickedRectangle(g, TICK_SIZE, BORDER_THICKNESS, getX(), getY(),
				getWidth(), getHeight());

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
		return isActive || other instanceof SideRectangle;
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

	@Override
	public boolean intersectsX(Rectangle other) {
		return canInteract(other) && super.intersectsX(other);
	}

	@Override
	public boolean intersectsY(Rectangle other) {
		return canInteract(other) && super.intersectsY(other);
	}

	@Override
	public boolean usedToIntersectX(Rectangle other) {
		return (wasActive || other instanceof SideRectangle)
				&& super.usedToIntersectX(other);
	}

	@Override
	public boolean usedToIntersectY(Rectangle other) {
		return (wasActive || other instanceof SideRectangle)
				&& super.usedToIntersectY(other);
	}

	@Override
	public Color getColor() {
		Color color = super.getColor();
		return new Color(color.getRed(), color.getGreen(), color.getBlue(),
				(int) (color.getAlpha() * (isActive ? 1
						: paramMapper.getFloat(Parameter.OPACITY_MULTIPLIER))));
	}

	/**
	 * Sets whether this is "active" or not. When this is not "active", it
	 * cannot interact with other {@code Rectangle}s (except
	 * {@code SideRectangle}s) or move.
	 * 
	 * @param active whether it should be "active"
	 */
	public void setActive(boolean active) {
		isActive = active;
		getAttachments().forEach(a -> a.setActive(active));
	}

	@Override
	public boolean hasMoved() {
		return super.hasMoved() || (!wasActive && isActive);
	}

}
