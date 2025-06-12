package game;

import java.awt.Color;
import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

	public static final int ARROW_INSET = 10;
	public static final int ARROW_HEAD_HEIGHT = 20;
	public static final int ARROW_HEAD_WIDTH = 10;
	public static final int ARROW_LINE_HEIGHT = 8;

	private int xGrowth, yGrowth;

	public GrowArea() {
		this(0, 0, 0, 0, 0, 0);
	}

	// xGrowth and yGrowth actually represent half the growth rate (it is applied
	// to each side independently)
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

		// I can't think of a less messy way to draw these arrows
		g.setColor(getColor().darker());
		if (xGrowth != 0) {
			int rightX = getX() + getWidth();
			int middleY = getY() + (getHeight() / 2);
			int[] headY = new int[] { middleY, middleY - (ARROW_HEAD_HEIGHT / 2),
					middleY + (ARROW_HEAD_HEIGHT / 2) };

			g.fillPolygon(new int[] { getX() + ARROW_INSET,
					getX() + ARROW_INSET + ARROW_HEAD_WIDTH,
					getX() + ARROW_INSET + ARROW_HEAD_WIDTH }, headY, 3);
			g.fillPolygon(new int[] { rightX - ARROW_INSET,
					rightX - ARROW_INSET - ARROW_HEAD_WIDTH,
					rightX - ARROW_INSET - ARROW_HEAD_WIDTH }, headY, 3);
			g.fillRect(getX() + ARROW_INSET + ARROW_HEAD_WIDTH,
					middleY - (ARROW_LINE_HEIGHT / 2),
					getWidth() - (2 * ARROW_INSET) - (2 * ARROW_HEAD_WIDTH),
					ARROW_LINE_HEIGHT);
		}
		if (yGrowth != 0) {
			int bottomY = getY() + getHeight();
			int middleX = getX() + (getWidth() / 2);
			int[] headX = new int[] { middleX, middleX - (ARROW_HEAD_HEIGHT / 2),
					middleX + (ARROW_HEAD_HEIGHT / 2) };

			g.fillPolygon(headX,
					new int[] { getY() + ARROW_INSET,
							getY() + ARROW_INSET + ARROW_HEAD_WIDTH,
							getY() + ARROW_INSET + ARROW_HEAD_WIDTH },
					3);
			g.fillPolygon(headX,
					new int[] { bottomY - ARROW_INSET,
							bottomY - ARROW_INSET - ARROW_HEAD_WIDTH,
							bottomY - ARROW_INSET - ARROW_HEAD_WIDTH },
					3);
			g.fillRect(middleX - (ARROW_LINE_HEIGHT / 2),
					getY() + ARROW_INSET + ARROW_HEAD_WIDTH, ARROW_LINE_HEIGHT,
					getHeight() - (2 * ARROW_INSET) - (2 * ARROW_HEAD_WIDTH));
		}
	}

	@Override
	public void onEnter(MovingRectangle rect) {}

	@Override
	public void onExit(MovingRectangle rect) {}

	/**
	 * Grow {@code rect} until it is as tall/wide as this
	 */
	@Override
	public void everyFrame(MovingRectangle rect) {
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
