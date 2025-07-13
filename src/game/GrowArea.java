package game;

import java.awt.Color;
import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import game.MainFrame.Direction;

/**
 * Increases a {@code MovingRectangle}'s width and/or height to match the
 * width/height of this.
 * 
 * @see ShrinkArea
 * 
 * @author Frank Kormann
 */
public class GrowArea extends Area {

	public static final Color DEFAULT_COLOR = new Color(22, 245, 41, 128);

	public static final int ARROW_INSET = 5;
	public static final int ARROW_HEAD_WIDTH = 20;
	public static final int ARROW_HEAD_LENGTH = 10;
	public static final int ARROW_TAIL_WIDTH = 8;

	private int xGrowth, yGrowth;

	public GrowArea() {
		this(0, 0, 0, 0, 0, 0);
	}

	/**
	 * Note: {@code xGrowth} and {@code yGrowth} effect each side of the
	 * {@code MovingRectangle} independently. Therefore, if both sides are growing,
	 * the {@code MovingRectangle}'s {@code width} will increase at twice the
	 * nominal rate.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param xGrowth amount to grow the left/right sides by each frame
	 * @param yGrowth amount to grow the top/bottom sides by each frame
	 */
	@JsonCreator
	public GrowArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width, @JsonProperty("height") int height,
			@JsonProperty("xGrowth") int xGrowth,
			@JsonProperty("yGrowth") int yGrowth) {
		super(x, y, width, height, DEFAULT_COLOR);
		this.xGrowth = xGrowth;
		this.yGrowth = yGrowth;
	}

	@Override
	public void draw(Graphics g) {
		super.draw(g);
		g = g.create();

		g.setColor(getColor().darker());
		if (xGrowth != 0) {
			drawArrow(g, getX() + ARROW_INSET, getY() + getHeight() / 2,
					ARROW_HEAD_LENGTH, ARROW_HEAD_WIDTH,
					getWidth() / 2 - ARROW_INSET - ARROW_HEAD_LENGTH, ARROW_TAIL_WIDTH,
					Direction.WEST);
			drawArrow(g, getX() + getWidth() - ARROW_INSET, getY() + getHeight() / 2,
					ARROW_HEAD_LENGTH, ARROW_HEAD_WIDTH,
					getWidth() / 2 - ARROW_INSET - ARROW_HEAD_LENGTH, ARROW_TAIL_WIDTH,
					Direction.EAST);
		}
		if (yGrowth != 0) {
			drawArrow(g, getX() + getWidth() / 2, getY() + ARROW_INSET,
					ARROW_HEAD_LENGTH, ARROW_HEAD_WIDTH,
					getHeight() / 2 - ARROW_INSET - ARROW_HEAD_LENGTH, ARROW_TAIL_WIDTH,
					Direction.NORTH);
			drawArrow(g, getX() + getWidth() / 2, getY() + getHeight() - ARROW_INSET,
					ARROW_HEAD_LENGTH, ARROW_HEAD_WIDTH,
					getHeight() / 2 - ARROW_INSET - ARROW_HEAD_LENGTH, ARROW_TAIL_WIDTH,
					Direction.SOUTH);
		}

		g.dispose();
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
	 * Grows {@code rect} until it is as tall/wide as this.
	 * 
	 * @param rect {@code MovingRectangle} to grow
	 */
	@Override
	protected void everyFrame(MovingRectangle rect) {
		int possibleChange;

		possibleChange = rect.getX() - getX();
		if (possibleChange > 0) {
			rect.changeWidth(Math.min(xGrowth, possibleChange), true);
		}

		possibleChange = getX() + getWidth() - rect.getX() - rect.getWidth();
		if (possibleChange > 0) {
			rect.changeWidth(Math.min(xGrowth, possibleChange), false);
		}

		possibleChange = rect.getY() - getY();
		if (possibleChange > 0) {
			rect.changeHeight(Math.min(yGrowth, possibleChange), true);
		}

		possibleChange = getY() + getHeight() - rect.getY() - rect.getHeight();
		if (possibleChange > 0) {
			rect.changeHeight(Math.min(yGrowth, possibleChange), false);

		}
	}

}
