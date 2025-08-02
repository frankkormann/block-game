package game;

import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import game.MainFrame.Direction;

/**
 * Decreases a {@code MovingRectangle}'s width and/or height to match the
 * width/height of this.
 * 
 * @see GrowArea
 * 
 * @author Frank Kormann
 */
public class ShrinkArea extends Area {

	private static final int MINIMUM_SIZE = 5;

	private static final int ARROW_INSET = 5;
	private static final int ARROW_HEAD_WIDTH = 20;
	private static final int ARROW_HEAD_LENGTH = 10;
	private static final int ARROW_TAIL_WIDTH = 8;

	private int xShrink, yShrink;

	/**
	 * Note: {@code xShrink} and {@code yShrink} effect each side of the
	 * {@code MovingRectangle} independently. Therefore, if both sides are
	 * shrinking, the {@code MovingRectangle}'s {@code width} will decrease at
	 * twice the nominal rate.
	 * 
	 * @param x       x position
	 * @param y       y position
	 * @param width   width
	 * @param height  height
	 * @param xShrink amount to shrink the left/right sides by each frame
	 * @param yShrink amount to shrink the top/bottom sides by each frame
	 */
	@JsonCreator
	public ShrinkArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("xShrink") int xShrink,
			@JsonProperty("yShrink") int yShrink) {
		super(x, y, width, height, Colors.TRANSLUCENT_RED);
		this.xShrink = xShrink;
		this.yShrink = yShrink;
	}

	@Override
	public void draw(Graphics g) {
		super.draw(g);
		g = g.create();

		g.setColor(getColor().darker());
		if (xShrink != 0) {
			drawArrow(g, getX() + getWidth() / 2, getY() + getHeight() / 2,
					ARROW_HEAD_LENGTH, ARROW_HEAD_WIDTH,
					getWidth() / 2 - ARROW_INSET - ARROW_HEAD_LENGTH,
					ARROW_TAIL_WIDTH, Direction.WEST);
			drawArrow(g, getX() + getWidth() / 2, getY() + getHeight() / 2,
					ARROW_HEAD_LENGTH, ARROW_HEAD_WIDTH,
					getWidth() / 2 - ARROW_INSET - ARROW_HEAD_LENGTH,
					ARROW_TAIL_WIDTH, Direction.EAST);
		}
		if (yShrink != 0) {
			drawArrow(g, getX() + getWidth() / 2, getY() + getHeight() / 2,
					ARROW_HEAD_LENGTH, ARROW_HEAD_WIDTH,
					getHeight() / 2 - ARROW_INSET - ARROW_HEAD_LENGTH,
					ARROW_TAIL_WIDTH, Direction.SOUTH);
			drawArrow(g, getX() + getWidth() / 2, getY() + getHeight() / 2,
					ARROW_HEAD_LENGTH, ARROW_HEAD_WIDTH,
					getHeight() / 2 - ARROW_INSET - ARROW_HEAD_LENGTH,
					ARROW_TAIL_WIDTH, Direction.NORTH);
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
	 * Shrink rect until it is as tall/wide as this.
	 * 
	 * @param rect {@code MovingRectangle} to shrink
	 */
	@Override
	protected void everyFrame(MovingRectangle rect) {

		shrinkRect(rect, getY() - rect.getY(), Direction.NORTH);
		shrinkRect(rect, rect.getY() + rect.getHeight() - getY() - getHeight(),
				Direction.SOUTH);
		shrinkRect(rect, getX() - rect.getX(), Direction.WEST);
		shrinkRect(rect, rect.getX() + rect.getWidth() - getX() - getWidth(),
				Direction.EAST);

	}

	/**
	 * Shrinks {@code rect} in {@code direction}, bounded by
	 * {@code possibleChange} and respecting {@code MINIMUM_SIZE}.
	 * 
	 * @param rect           {@code MovingRectangle} to shrink
	 * @param possibleChange maximum amount {@code rect} is allowed to shrink
	 * @param direction      {@code Direction} to remove width from {@code rect}
	 *                       in
	 */
	private void shrinkRect(MovingRectangle rect, int possibleChange,
			Direction direction) {
		int actualChange;

		if (direction == Direction.WEST || direction == Direction.EAST) {
			actualChange = Math.min(xShrink,
					Math.min(possibleChange, rect.getWidth() - MINIMUM_SIZE));
		}
		else {
			actualChange = Math.min(yShrink,
					Math.min(possibleChange, rect.getHeight() - MINIMUM_SIZE));
		}

		if (actualChange > 0) {
			switch (direction) {
				case NORTH:
					rect.changeHeight(-actualChange, true);
					break;
				case SOUTH:
					rect.changeHeight(-actualChange, false);
					break;
				case WEST:
					rect.changeWidth(-actualChange, true);
					break;
				case EAST:
					rect.changeWidth(-actualChange, false);
					break;
			}
		}
	}

}
