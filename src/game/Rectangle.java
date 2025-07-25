package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import game.MainFrame.Direction;

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
 * {@code Areas} can be attached to this. Attached {@code Areas} have their
 * position and size updated relative to this depending on the
 * {@code AttachmentOption}s used.
 * <p>
 * {@code Rectangles} (and levels in general) should be created through JSON.
 * See the wiki for more details.
 * <p>
 * Subclasses which can move should override
 * {@link#getLastX()}, {@link#getLastY()}, {@link#getLastWidth()}, and
 * {@link#getLastHeight()}.
 *
 * @author Frank Kormann
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class Rectangle implements Drawable {

	private static final float BORDER_DARKNESS = 1.2f;
	private static final int BORDER_THICKNESS = 1;

	/**
	 * How this interacts when it collides with the edge of the window
	 */
	public enum ResizeBehavior {
		/**
		 * Be pushed by window edges
		 */
		MOVE,
		/**
		 * Do not interact with window edges
		 */
		STAY,
		/**
		 * Stop window edges from passing through this horizontally
		 */
		PREVENT_X,
		/**
		 * Stop window edges from passing through this vertically
		 */
		PREVENT_Y
	}

	/**
	 * Which properties of an attached {@code Area} to maintain relative to this
	 * <p>
	 * At most one {@code GLUED_} value should be used
	 */
	public enum AttachmentOption {
		/**
		 * Keep the {@code Area} above this. Incompatible with
		 * {@code GLUED_SOUTH}, {@code GLUED_WEST}, {@code GLUED_EAST}.
		 */
		GLUED_NORTH,
		/**
		 * Keep the {@code Area} below this. Incompatible with
		 * {@code GLUED_NORTH}, {@code GLUED_WEST}, {@code GLUED_EAST}.
		 */
		GLUED_SOUTH,
		/**
		 * Keep the {@code Area} to the left of this. Incompatible with
		 * {@code GLUED_NORTH}, {@code GLUED_SOUTH}, {@code GLUED_EAST}.
		 */
		GLUED_WEST,
		/**
		 * Keep the {@code Area} to the right of this. Incompatible with
		 * {@code GLUED_NORTH}, {@code GLUED_SOUTH}, {@code GLUED_WEST}.
		 */
		GLUED_EAST,
		/**
		 * Force the {@code Area}'s width to be the same as this
		 */
		SAME_WIDTH,
		/**
		 * Force the {@code Area}'s height to be the same as this
		 */
		SAME_HEIGHT
	}

	private Color color;

	private int x, y, width, height;
	private List<Pair<Area, Set<AttachmentOption>>> attachedAreas;

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
	}

	@Override
	public void draw(Graphics g) {
		g = g.create();

		Color border = new Color((int) (color.getRed() / BORDER_DARKNESS),
				(int) (color.getGreen() / BORDER_DARKNESS),
				(int) (color.getBlue() / BORDER_DARKNESS));
		g.setColor(border);
		g.fillRect(x, y, width, height);

		g.setColor(color);
		g.fillRect(x + BORDER_THICKNESS, y + BORDER_THICKNESS,
				width - 2 * BORDER_THICKNESS, height - 2 * BORDER_THICKNESS);

		g.dispose();
	}

	/**
	 * Draws an arrow pointing at ({@code tipX}, {@code tipY}) in
	 * {@code direction}.
	 * <p>
	 * Note that {@code headLength}, {@code headWidth}, {@code tailLength}, and
	 * {@code tailWidth} always correspond to the same parts of the arrow, no
	 * matter which direction it is pointing in. For example, {@code headWidth}
	 * is a vertical distance on an easterly arrow but a horizontal distance on
	 * a northerly arrow.
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
			int headWidth, int tailLength, int tailWidth, Direction direction) {

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

	/**
	 * Moves and resizes all attached {@code Area}s to conform with their
	 * attachment options.
	 */
	private void updateAttachmentBounds() {

		for (Pair<Area, Set<AttachmentOption>> areaPair : attachedAreas) {
			Area attached = areaPair.first;
			Set<AttachmentOption> options = areaPair.second;

			if (options.contains(AttachmentOption.SAME_WIDTH)) {
				attached.setWidth(width);
			}

			if (options.contains(AttachmentOption.SAME_HEIGHT)) {
				attached.setHeight(height);
			}

			if (options.contains(AttachmentOption.GLUED_NORTH)) {
				attached.setX(x);
				attached.setY(y - attached.getHeight());
			}

			if (options.contains(AttachmentOption.GLUED_SOUTH)) {
				attached.setX(x);
				attached.setY(y + height);
			}

			if (options.contains(AttachmentOption.GLUED_WEST)) {
				attached.setX(x - attached.getWidth());
				attached.setY(y);
			}

			if (options.contains(AttachmentOption.GLUED_EAST)) {
				attached.setX(x + width);
				attached.setY(y);
			}

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
	 * Returns true if this can push other {@code Rectangle}s in the x
	 * direction.
	 * 
	 * @return {@code true} if this can push {@code Rectangle}s in the x
	 *         direction
	 */
	public boolean canPushX() {
		return true;
	}

	/**
	 * Returns true if this can push other {@code Rectangle}s in the y
	 * direction.
	 * 
	 * @return {@code true} if this can push {@code Rectangle}s in the y
	 *         direction
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

	public int getX() {
		return x;
	}

	/**
	 * This method should be overridden in subclasses that can move.
	 * 
	 * @return x position on the previous frame
	 */
	public int getLastX() {
		return getX();
	}

	public void setX(int x) {
		this.x = x;
		updateAttachmentBounds();
	}

	public int getY() {
		return y;
	}

	/**
	 * This method should be overridden in subclasses that can move.
	 * 
	 * @return y position on the previous frame
	 */
	public int getLastY() {
		return getY();
	}

	public void setY(int y) {
		this.y = y;
		updateAttachmentBounds();
	}

	public int getWidth() {
		return width;
	}

	/**
	 * This method should be overridden in subclasses that can move.
	 * 
	 * @return width on the previous frame
	 */
	public int getLastWidth() {
		return getWidth();
	}

	public void setWidth(int width) {
		this.width = width;
		updateAttachmentBounds();
	}

	public int getHeight() {
		return height;
	}

	/**
	 * This method should be overridden in subclasses that can move.
	 * 
	 * @return height on the previous frame
	 */
	public int getLastHeight() {
		return getHeight();
	}

	public void setHeight(int height) {
		this.height = height;
		updateAttachmentBounds();
	}

	public void setResizeBehavior(ResizeBehavior resizeBehavior) {
		this.resizeBehavior = resizeBehavior;
	}

	public ResizeBehavior getResizeBehavior() {
		return resizeBehavior;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void addAttachment(Area attachment, AttachmentOption... options) {
		Set<AttachmentOption> optionsSet = new HashSet<AttachmentOption>(
				Arrays.asList(options));
		attachedAreas.add(
				new Pair<Area, Set<AttachmentOption>>(attachment, optionsSet));
		updateAttachmentBounds();
	}

	@JsonProperty("attachments")
	public void addAllAttachments(
			List<Pair<Area, Set<AttachmentOption>>> attachments) {
		attachedAreas.addAll(attachments);
		updateAttachmentBounds();
	}

	public List<Area> getAttachments() {
		List<Area> result = new ArrayList<>();
		for (Pair<Area, Set<AttachmentOption>> areaPair : attachedAreas) {
			result.add(areaPair.first);
		}

		return result;
	}

}
