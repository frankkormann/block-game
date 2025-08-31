package blockgame.physics;

/**
 * Sets the flag of {@code MovingRectangle}s within this to be able to jump.
 * <p>
 * Each {@code MovingRectangle} and {@code WallRectangle} automatically creates
 * an instance of this attached to their north side, so that other
 * {@code MovingRectangles} are able to jump when they are standing on
 * something.
 * 
 * @author Frank Kormann
 */
public class JumpArea extends Area {

	private static int HEIGHT = 3;

	public JumpArea(int x, int y, int width) {
		super(x, y, width, HEIGHT, Colors.TRANSPARENT);
	}

	/**
	 * Sets {@code rect}'s to be able to jump.
	 * 
	 * @param rect {@code MovingRectangle} to change the property of
	 */
	@Override
	public void onEnter(MovingRectangle rect) {
		rect.setCanJump(true);
	}

	/**
	 * Sets {@code rect}'s to not be able to jump.
	 * 
	 * @param rect {@code MovingRectangle} to change the property of
	 */
	@Override
	public void onExit(MovingRectangle rect) {
		rect.setCanJump(false);
	}

	/**
	 * Sets {@code rect}'s to be able to jump.
	 * 
	 * @param rect {@code MovingRectangle} to change the property of
	 */
	@Override
	public void everyFrame(MovingRectangle rect) {
		rect.setCanJump(true);
	}

}
