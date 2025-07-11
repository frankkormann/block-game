package game;

/**
 * Something which can move.
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
	 * @param xDifference change in x direction
	 * @param yDifference change in y direction
	 */
	public void move2(int xDifference, int yDifference);

}
