package game;

import java.awt.Color;
import java.awt.Graphics;
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

	private static final int TICK_SIZE = 10;
	private static final int TICK_THICKNESS = 3;
	private static final float INNER_RECT_DARKNESS = 1.2f;

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
	public void draw(Graphics g) {
		super.draw(g);
		g = g.create();

		Color inenrRectColor = new Color(
				(int) (getColor().getRed() / INNER_RECT_DARKNESS),
				(int) (getColor().getGreen() / INNER_RECT_DARKNESS),
				(int) (getColor().getBlue() / INNER_RECT_DARKNESS),
				getColor().getAlpha());
		g.setColor(inenrRectColor);
		int innerRectX = getX() + getWidth() * 1 / 4;
		int innerRectY = getY() + getHeight() * 1 / 4;
		int innerRectWidth = getWidth() / 2;
		int innerRectHeight = getHeight() / 2;
		drawTickedRectangle(g, new Color(0, 0, 0, 0), TICK_SIZE, TICK_THICKNESS,
				innerRectX, innerRectY, innerRectWidth, innerRectHeight);

		g.dispose();
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
