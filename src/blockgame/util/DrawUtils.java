package blockgame.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import blockgame.gui.MainFrame.Direction;

/**
 * Collection of rectangle-focused methods to draw with {@code Graphics}
 * objects.
 * 
 * @author Frank Kormann
 */
public class DrawUtils {

	/**
	 * Draws an outline of a rectangle with the specified thickness. The outline
	 * does not extend past the specified {@code width} and {@code height}.
	 * 
	 * @param g         {@code Graphics} to draw with
	 * @param thickness width of the outline
	 * @param x         position of rectangle
	 * @param y         position of rectangle
	 * @param width     size of rectangle
	 * @param height    size of rectangle
	 */
	public static void drawRectOutline(Graphics g, int thickness, int x, int y,
			int width, int height) {
		g.fillRect(x, y, thickness, height);
		g.fillRect(x + width - thickness, y, thickness, height);
		g.fillRect(x + thickness, y, width - 2 * thickness, thickness);
		g.fillRect(x + thickness, y + height - thickness, width - 2 * thickness,
				thickness);
	}

	/**
	 * Draws a dashed outline (looks like - - - - -) of a rectangle. Each dash
	 * is drawn with {@code g}'s color.
	 * 
	 * @param g          {@code Graphics} to draw with
	 * @param emptyColor {@code Color} to use for space between dash marks
	 * @param dashSize   length of each dash mark
	 * @param thickness  width of the outline
	 * @param x          position of bounding rectangle
	 * @param y          position of bounding rectangle
	 * @param width      size of bounding rectangle
	 * @param height     size of bounding rectangle
	 */
	public static void drawDashedRectangle(Graphics g, Color emptyColor,
			int dashSize, int thickness, int x, int y, int width, int height) {
		g = g.create();

		boolean isEmptyDash = false;
		Color noramlColor = g.getColor();
		int overflow = 0;
		for (int dashX = x; dashX < x + width; dashX += dashSize) {
			g.setColor(isEmptyDash ? emptyColor : noramlColor);
			isEmptyDash = !isEmptyDash;

			int length = Math.min(dashSize, x + width - dashX);
			overflow = dashSize - length;
			g.fillRect(dashX, y, length, thickness);
		}

		g.fillRect(x + width - thickness, y + thickness, thickness, overflow);

		for (int dashY = y + thickness + overflow; dashY < y + height
				- thickness; dashY += dashSize) {
			g.setColor(isEmptyDash ? emptyColor : noramlColor);
			isEmptyDash = !isEmptyDash;

			int length = Math.min(dashSize, y + height - thickness - dashY);
			overflow = dashSize - length;
			g.fillRect(x + width - thickness, dashY, thickness, length);
		}

		g.fillRect(x + width - overflow, y + height - thickness, overflow,
				thickness);

		for (int dashX = x + width - overflow - dashSize; dashX >= x
				- dashSize; dashX -= dashSize) {
			g.setColor(isEmptyDash ? emptyColor : noramlColor);
			isEmptyDash = !isEmptyDash;

			int length = Math.min(dashSize, dashX + dashSize - x);
			overflow = dashSize - length;
			g.fillRect(Math.max(x, dashX), y + height - thickness, length,
					thickness);
		}

		g.fillRect(x, y + height - thickness - overflow, thickness, overflow);

		for (int dashY = y + height - thickness - overflow
				- dashSize; dashY >= y + thickness
						- dashSize; dashY -= dashSize) {
			g.setColor(isEmptyDash ? emptyColor : noramlColor);
			isEmptyDash = !isEmptyDash;

			int length = Math.min(dashSize, dashY + dashSize - y - thickness);
			g.fillRect(x, Math.max(y + thickness, dashY), thickness, length);
		}

		g.dispose();
	}

	/**
	 * Fills a rectangle bounded by {@code x}, {@code y}, {@code width}, and
	 * {@code height} with stripes. Their color alternates between {@code g}'s
	 * color and {@code altColor}.
	 * 
	 * @param g             {@code Graphics} to draw with
	 * @param altColor      {@code Color} of alternating stripes
	 * @param stripSize     width of main stripes
	 * @param altStripeSize width of alternating stripes
	 * @param x             position of bounding rectangle
	 * @param y             position of bounding rectangle
	 * @param width         size of bounding rectangle
	 * @param height        size of bounding rectangle
	 */
	public static void fillStripes(Graphics g, Color altColor, int stripSize,
			int altStripeSize, int x, int y, int width, int height) {
		g = g.create();
		g.clipRect(x, y, width, height);

		boolean isAltStripe = true;
		Color normalColor = g.getColor();
		int thickness = stripSize;
		// Multiply width/height by 2 to capture both halves of the rectangle
		for (int stripeX = x, stripeY = y; stripeX + thickness < x + width * 2
				|| stripeY + thickness < y + height
						* 2; stripeX += thickness, stripeY += thickness) {

			thickness = isAltStripe ? altStripeSize : stripSize;
			g.setColor(isAltStripe ? altColor : normalColor);
			g.fillPolygon(new int[] { x, stripeX, stripeX + thickness, x },
					new int[] { stripeY, y, y, stripeY + thickness }, 4);

			isAltStripe = !isAltStripe;
		}

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
	public static void drawArrow(Graphics g, int tipX, int tipY, int headLength,
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

		g2d.dispose();
	}

}
