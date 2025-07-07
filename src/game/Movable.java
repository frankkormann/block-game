package game;

/**
 * Minimum of methods for something to be movable.
 * 
 * @author Frank Kormann
 */
public interface Movable {

	/**
	 * Move this such that {@code x += xDifference}, {@code y += yDifference}.
	 * <p>
	 * The name is "move2" to avoid conflict with
	 * {@code java.awt.Component.move(int, int)}.
	 * 
	 * @param xDifference
	 * @param yDifference
	 */
	public void move2(int xDifference, int yDifference);

}
