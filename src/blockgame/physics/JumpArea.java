package blockgame.physics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

	private static final int HEIGHT = 3;
	private static final int COYOTE_FRAMES = 2;

	private int coyoteFrames;

	/**
	 * Creates a {@code JumpArea} with no color or bounds. Useful for attaching
	 * to other {@code Rectangle}s so that things standing on them can jump.
	 */
	public JumpArea() {
		this(0, 0, 0, HEIGHT, Colors.TRANSPARENT, COYOTE_FRAMES);
	}

	@JsonCreator
	public JumpArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height) {
		this(x, y, width, height, Colors.TRANSLUCENT_BLUE, 1);
	}

	private JumpArea(int x, int y, int width, int height, Colors color,
			int coyoteFrames) {
		super(x, y, width, height, color);
		this.coyoteFrames = coyoteFrames;
	}

	/**
	 * Sets {@code rect} to be able to jump.
	 * 
	 * @param rect {@code MovingRectangle} to change the property of
	 */
	@Override
	public void onEnter(MovingRectangle rect) {
		rect.setJumpFramesRemaining(coyoteFrames);
	}

	/**
	 * Not implemented.
	 * <p>
	 * {@code MovingRectangle} automatically turns off its ability to jump when
	 * its jump frames run out, so an implementation here is not necessary.
	 * 
	 * @param rect unused
	 */
	@Override
	public void onExit(MovingRectangle rect) {}

	/**
	 * Sets {@code rect} to be able to jump.
	 * 
	 * @param rect {@code MovingRectangle} to change the property of
	 */
	@Override
	public void everyFrame(MovingRectangle rect) {
		rect.setJumpFramesRemaining(coyoteFrames);
	}

}
