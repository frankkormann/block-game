package blockgame.gui;

import java.awt.Graphics;

/**
 * Something which can be drawn to the screen.
 * 
 * @author Frank Kormann
 */
public interface Drawable {

	/**
	 * Draws this using the given {@code Graphics} object.
	 * <p>
	 * When this returns, {@code g} should be unchanged.
	 * 
	 * @param g {@code Graphics} to draw with
	 */
	public void draw(Graphics g);

}
