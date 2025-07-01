package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
	 * {@code MOVE} - Be pushed by window edges
	 * <p>
	 * {@code PREVENT} - Stop window edges from passing through this
	 * <p>
	 * {@code STAY} - Do not interact with window edges
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

	/**
	 * Draws an arrow pointing at ({@code tipX}, {@code tipY}) in {@code direction}.
	 * <p>
	 * Note that {@code headLength}, {@code headWidth}, {@code tailLength}, and
	 * {@code tailWidth} always correspond to the same parts of the arrow, no matter
	 * which direction it is pointing in. For example, {@code headWidth} is a
	 * vertical distance on an easterly arrow but a horizontal distance on a
	 * northerly arrow.
	 * 
	 * @param g          {@code Graphics} instance; must be able to be cast to
	 *                   Graphics2D
	 * @param tipX       X coordinate arrow is pointing at
	 * @param tipY       Y coordinate arrow is pointing at
	 * @param headLength distance from arrow tip to beginning of tail
	 * @param headWidth  size of base of head
	 * @param tailLength distance from beginning to end of tail
	 * @param tailWidth  size of base of tail
	 * @param direction  orientation arrow tip is pointing towards
	 */
	protected void drawArrow(Graphics g, int tipX, int tipY, int headLength,
			int headWidth, int tailLength, int tailWidth,
			MainFrame.Direction direction) {

		Graphics2D g2d = (Graphics2D) g.create();

		int[] headX = { tipX - headWidth / 2, tipX, tipX + headWidth / 2 };
		int[] headY = { tipY + headLength, tipY, tipY + headLength };
		int tailX = tipX - tailWidth / 2;
		int tailY = tipY + headLength;

		switch (direction) {
			case NORTH:
				// already transformed
				break;
			case SOUTH:
				g2d.rotate(Math.PI, tipX, tipY);
				break;
			case WEST:
				g2d.rotate(-Math.PI / 2, tipX, tipY);
				break;
			case EAST:
				g2d.rotate(Math.PI / 2, tipX, tipY);
				break;
		}

		g2d.fillPolygon(headX, headY, 3);
		g2d.fillRect(tailX, tailY, tailWidth, tailLength);

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
	 * Returns true if this can push other {@code Rectangle}s in the x direction.
	 * 
	 * @return {@code true} if this can push {@code Rectangle}s in the x direction
	 */
	public boolean canPushX() {
		return true;
	}

	/**
	 * Returns true if this can push other {@code Rectangle}s in the y direction.
	 * 
	 * @return {@code true} if this can push {@code Rectangle}s in the y direction
	 */
	public boolean canPushY() {
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

	public void addAttachments(List<Area> attachments) {
		for (Area a : attachments) {
			attachedAreas.add(a);
		}
	}

	public List<Area> getAttachments() {
		return attachedAreas;
	}

}
