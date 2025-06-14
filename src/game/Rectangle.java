package game;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Holds data for drawing and collisions. Knows how to draw itself and calculate
 * if it is intersecting another {@code Rectangle}. No default
 * {@code ResizeBehavior}.
 * <p>
 * In addition to present position/size data, this stores its position/size on
 * the previous frame. This is used for collisions. {@code updateLastPosition()}
 * should be called at the beginning of every frame for every {@code Rectangle}.
 * <p>
 * {@code draw(Graphics,int,int)} and the {@code intersects} family of methods
 * can be overridden to easily change behavior.
 * <p>
 * {@code Areas} can be attached to this. Attached {@code Areas} will have their
 * positions updated to move in sync with this whenever this moves.
 * <p>
 * {@code Rectangles} (and levels in general) should be created through JSON.
 * See the README for more details.
 *
 * @author Frank Kormann
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class Rectangle {

	private static final float BORDER_DARKNESS = 1.2f;
	private static final int BORDER_THICKNESS = 1;

	/**
	 * MOVE - Be pushed by window edges
	 * <p>
	 * PREVENT - Stop window edges from passing through this
	 * <p>
	 * STAY - Do not interact with window edges
	 */
	public enum ResizeBehavior {
		MOVE, STAY, PREVENT_X, PREVENT_Y
	}

	private Color color;

	private int x, y;
	private int lastX, lastY;
	private int width, height;
	private int lastWidth, lastHeight;
	// These are used in collision to determine how much width/height to remove if
	// this is colliding because it increased in width/height
	private int leftWidthChange;
	private int topHeightChange;

	private List<Area> attachedAreas;

	private ResizeBehavior resizeBehavior;

	public Rectangle(int x, int y, int width, int height, Color color,
			ResizeBehavior resizeBehavior) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = color;
		this.resizeBehavior = resizeBehavior;
		attachedAreas = new ArrayList<>();

		updateLastPosition();
	}

	public void draw(Graphics g) {
		g = g.create();
		// Create a darker border
		Color border = new Color((int) (color.getRed() / BORDER_DARKNESS),
				(int) (color.getGreen() / BORDER_DARKNESS),
				(int) (color.getBlue() / BORDER_DARKNESS));
		g.setColor(border);
		g.fillRect(x, y, width, height);
		// Draw main rectangle inside border rectangle
		g.setColor(color);
		g.fillRect(x + BORDER_THICKNESS, y + BORDER_THICKNESS,
				width - 2 * BORDER_THICKNESS, height - 2 * BORDER_THICKNESS);
	}

	// TODO Think of a better arrow-drawing method
	/**
	 * Draws a double-headed horizontal arrow.
	 * <p>
	 * Note: {@code lineWidth} is not the overall width of the arrow. That is, the
	 * arrow will have a total length of {@code lineWidth + 2 *
	 * headWidth}.
	 * 
	 * @param g          Graphics instance
	 * @param x          Coordinate of left arrow tip
	 * @param y          Coordinate of left arrow tip
	 * @param headWidth  Length of a head
	 * @param headHeight Height of a head
	 * @param lineWidth  Length of the connecting line
	 * @param lineHeight Height of the connecting line
	 * @param xOffset    Coordinate of window's top left corner
	 * @param yOffset    Coordinate of window's top left corner
	 * 
	 * @see Rectangle#drawVerticalArrow
	 */
	public void drawHorizontalArrow(Graphics g, int x, int y, int headWidth,
			int headHeight, int lineWidth, int lineHeight) {
		// This is messy, but I don't see a better way to do it
		int[] xVals = { x, x + headWidth, x + headWidth, x + headWidth + lineWidth,
				x + headWidth + lineWidth, x + 2 * headWidth + lineWidth,
				x + headWidth + lineWidth, x + headWidth + lineWidth, x + headWidth,
				x + headWidth, x };
		int[] yVals = { y, y - headHeight / 2, y - lineHeight / 2, y - lineHeight / 2,
				y - headHeight / 2, y, y + headHeight / 2, y + lineHeight / 2,
				y + lineHeight / 2, y + headHeight / 2, y };
		g.fillPolygon(xVals, yVals, xVals.length);
	}

	/**
	 * Draws a double-headed vertical arrow.
	 * <p>
	 * Note: {@code lineHeight} is not the overall height of the arrow. That is, the
	 * arrow will have a total height of {@code lineHeight + 2 *
	 * headHeight}.
	 * 
	 * @param g          Graphics instance
	 * @param x          Coordinate of top arrow tip
	 * @param y          Coordinate of top arrow tip
	 * @param headWidth  Length of a head
	 * @param headHeight Height of a head
	 * @param lineWidth  Length of the connecting line
	 * @param lineHeight Height of the connecting line
	 * @param xOffset    Coordinate of window's top left corner
	 * @param yOffset    Coordinate of window's top left corner
	 * 
	 * @see Rectangle#drawHorizontalArrow
	 */
	public void drawVerticalArrow(Graphics g, int x, int y, int headWidth,
			int headHeight, int lineWidth, int lineHeight) {
		// This is messy, but I don't see a better way to do it
		int[] xVals = { x, x + headWidth / 2, x + lineWidth / 2, x + lineWidth / 2,
				x + headWidth / 2, x, x - headWidth / 2, x - lineWidth / 2,
				x - lineWidth / 2, x - headWidth / 2, x };
		int[] yVals = { y, y + headHeight, y + headHeight, y + headHeight + lineHeight,
				y + headHeight + lineHeight, y + 2 * headHeight + lineHeight,
				y + headHeight + lineHeight, y + headHeight + lineHeight,
				y + headHeight, y + headHeight, y };
		g.drawPolygon(xVals, yVals, xVals.length);
	}

	public void updateLastPosition() {
		lastX = x;
		lastY = y;
		lastWidth = width;
		lastHeight = height;
		leftWidthChange = 0;
		topHeightChange = 0;
		for (Area a : attachedAreas) {
			a.updateLastPosition();
		}
	}

	public void setAttachments(List<Area> attachments) {
		this.attachedAreas = attachments;
	}

	/**
	 * Returns true if this thinks it can interact with {@code other}.
	 * <p>
	 * This method should usually be called both ways. It is possible that
	 * {@code this.canInteract(other)} is {@code true} but
	 * {@code other.canInteract(this)} is {@code false}.
	 * 
	 * @param other {@code Rectangle} which is to be tested against
	 * @return {@code true} if this thinks it can interact with {@code other}
	 */
	public boolean canInteract(Rectangle other) {
		return true;
	}

	/**
	 * Calculate whether this intersects with other in the x direction.
	 * 
	 * @param other Other Rectangle
	 * 
	 * @return true if they intersect in the x direction
	 */
	public boolean intersectsX(Rectangle other) {
		boolean inBoundsX = (x <= other.getX() && other.getX() < x + width)
				|| (x < other.getX() + other.getWidth()
						&& other.getX() + other.getWidth() <= x + width)
				|| (other.getX() < x && x < other.getX() + other.getWidth());
		return canInteract(other) && other.canInteract(this) && inBoundsX;
	}

	/**
	 * Calculate whether this intersects with other in the y direction.
	 * 
	 * @param other Other Rectangle
	 * 
	 * @return true if they intersect in the y direction
	 */
	public boolean intersectsY(Rectangle other) {
		boolean inBoundsY = (y <= other.getY() && other.getY() < y + height)
				|| (y < other.getY() + other.getHeight()
						&& other.getY() + other.getHeight() <= y + height)
				|| (other.getY() <= y && y < other.getY() + other.getHeight());
		return canInteract(other) && other.canInteract(this) && inBoundsY;
	}

	/**
	 * Calculate whether this intersected with other in the x direction on the
	 * previous frame.
	 * 
	 * @param other Other Rectangle
	 * 
	 * @return true if they used to intersect in the x direction
	 */
	public boolean usedToIntersectX(Rectangle other) {
		boolean usedToBeInBoundsX = (lastX <= other.getLastX()
				&& other.getLastX() < lastX + lastWidth)
				|| (lastX < other.getLastX() + other.getLastWidth()
						&& other.getLastX() + other.getLastWidth() <= lastX + lastWidth)
				|| (other.getLastX() < lastX
						&& lastX < other.getLastX() + other.getLastWidth());
		return canInteract(other) && other.canInteract(this) && usedToBeInBoundsX;
	}

	/**
	 * Calculate whether this intersected with other in the y direction on the
	 * previous frame.
	 * 
	 * @param other Other Rectangle
	 * 
	 * @return true if they used to intersect in the y direction
	 */
	public boolean usedToIntersectY(Rectangle other) {
		boolean usedToBeInBoundsY = (lastY <= other.getLastY()
				&& other.getLastY() < lastY + lastHeight)
				|| (lastY < other.getLastY() + other.getLastHeight() && other.getLastY()
						+ other.getLastHeight() <= lastY + lastHeight)
				|| (other.getLastY() < lastY
						&& lastY < other.getLastY() + other.getLastHeight());
		return canInteract(other) && other.canInteract(this) && usedToBeInBoundsY;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		for (Area a : attachedAreas) {
			a.setX(a.getX() + x - this.x);
		}
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		for (Area a : attachedAreas) {
			a.setY(a.getY() + y - this.y);
		}
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public int getLeftWidthChange() {
		return leftWidthChange;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void changeWidth(int change, boolean addToLeft) {
		width += change;
		if (width <= 0) {
			change += 1 - width;
			width = 1;
		}
		if (addToLeft) {
			x -= change;
			leftWidthChange += change;
		}
	}

	public int getHeight() {
		return height;
	}

	public int getTopHeightChange() {
		return topHeightChange;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void changeHeight(int change, boolean addToTop) {
		height += change;
		if (height <= 0) {
			change += 1 - height;
			height = 1;
		}
		if (addToTop) {
			y -= change;
			topHeightChange += change;
		}
	}

	public void setResizeBehavior(ResizeBehavior resizeBehavior) {
		this.resizeBehavior = resizeBehavior;
	}

	public ResizeBehavior getResizeBehavior() {
		return resizeBehavior;
	}

	public int getLastX() {
		return lastX;
	}

	public int getLastY() {
		return lastY;
	}

	public int getLastWidth() {
		return lastWidth;
	}

	public int getLastHeight() {
		return lastHeight;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public List<Area> getAttachments() {
		return attachedAreas;
	}

}
