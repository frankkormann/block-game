package blockgame.input;

import blockgame.gui.MainFrame.Direction;

/**
 * Something which can be resized.
 * 
 * @author Frank Kormann
 */
public interface Resizable {

	/**
	 * Resizes this by {@code change} in {@code direction}.
	 * <p>
	 * Importantly, if {@code direction} is {@code NORTH} or {@code WEST}, the x
	 * or y position of this will also change.
	 * 
	 * @param change    difference between the new length and the old length
	 * @param direction which way to resize
	 */
	public void resize(int change, Direction direction);

}
