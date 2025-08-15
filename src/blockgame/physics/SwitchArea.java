package blockgame.physics;

import java.awt.Color;
import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code Area} that reports to {@code SwitchController}. As long as there is a
 * {@code MovingRectangle} within this, all the children of its
 * {@code SwitchContoller} will be active.
 *
 * @author Frank Kormann
 */
public class SwitchArea extends Area {

	private static final int DASH_SIZE = 10;
	private static final int DASH_THICKNESS = 3;
	private static final float INNER_RECT_DARKNESS = 1.2f;

	private String key;
	private SwitchController controller;
	private int numberInside;

	@JsonCreator
	public SwitchArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("color") Colors colorEnum,
			@JsonProperty("key") String key) {
		super(x, y, width, height, colorEnum);
		this.key = key;
		numberInside = 0;
	}

	@Override
	public void draw(Graphics g) {
		super.draw(g);
		g = g.create();

		Color innerRectColor = new Color(
				(int) (getColor().getRed() / INNER_RECT_DARKNESS),
				(int) (getColor().getGreen() / INNER_RECT_DARKNESS),
				(int) (getColor().getBlue() / INNER_RECT_DARKNESS),
				getColor().getAlpha());
		g.setColor(innerRectColor);
		int quarterWidth = getWidth() / 4;
		int quarterHeight = getHeight() / 4;
		// Calculate the width/height by subtracting 2 quarters so that there is
		// equal distance from each side of the inner rectangle to the side of
		// the outer rectangle, taking into account float rounding
		drawDashedRectangle(g, new Color(0, 0, 0, 0), DASH_SIZE, DASH_THICKNESS,
				getX() + quarterWidth, getY() + quarterHeight,
				getWidth() - 2 * quarterWidth, getHeight() - 2 * quarterHeight);

		g.dispose();
	}

	/**
	 * Sets all {@code SwitchRectangle} children to be active if there were no
	 * {@code MovingRectangle}s within this previously.
	 * 
	 * @param rect {@code MovingRectangle} which entered
	 */
	@Override
	public void onEnter(MovingRectangle rect) {
		numberInside++;
		if (numberInside == 1) {
			controller.areaActivated();
		}
	}

	/**
	 * Sets all {@code SwitchRectangle} children to be inactive if there area no
	 * {@code MovingRectangle}s within this anymore.
	 * 
	 * @param rect {@code MovingRectangle} which exited
	 */
	@Override
	public void onExit(MovingRectangle rect) {
		numberInside--;
		if (numberInside == 0) {
			controller.areaDeactivated();
		}
	}

	/**
	 * Not implemented.
	 * 
	 * @param rect unused
	 */
	@Override
	public void everyFrame(MovingRectangle rect) {}

	public String getKey() {
		return key;
	}

	public void setController(SwitchController controller) {
		this.controller = controller;
	}

}
