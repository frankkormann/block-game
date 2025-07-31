package game;

import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import game.MovingRectangle.Colors;

/**
 * Advances to the next level when a {@code MovingRectangle} controlled by the
 * player has stayed within it for a long enough time.
 * 
 * @author Frank Kormann
 */
public class GoalArea extends Area {

	private static final int TIMEOUT = 100;

	private int timer;
	private boolean used;
	private String nextLevel;

	@JsonCreator
	public GoalArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("nextLevel") String nextLevel) {
		super(x, y, width, height, Colors.TRANSLUCENT_YELLOW);
		timer = 0;
		used = false;
		this.nextLevel = nextLevel;

		if (nextLevel == "") {
			System.err.println(
					"GoalArea at " + x + ", " + y + ": nextLevel is nothing");
		}
	}

	public boolean hasWon() {
		return timer >= TIMEOUT && !used;
	}

	@Override
	public void draw(Graphics g) {
		super.draw(g);
		g = g.create();
		// Create a loading bar effect as timer approaches TIMEOUT
		g.setColor(getColor().darker());
		int fillHeight = getHeight() * timer / TIMEOUT;
		g.fillRect(getX(), getY() + getHeight() - fillHeight, getWidth(),
				fillHeight);

		g.dispose();
	}

	/**
	 * Not implemented.
	 * 
	 * @param rect unused
	 */
	@Override
	protected void onEnter(MovingRectangle rect) {}

	/**
	 * Resets the internal timer to {@code 0} if
	 * {@code rect.isControlledByPlayer() == true}.
	 * 
	 * @param rect {@code MovingRectangle} to consider
	 */
	@Override
	protected void onExit(MovingRectangle rect) {
		if (rect.isControlledByPlayer()) {
			timer = 0;
		}
		used = false;
	}

	/**
	 * If {@code rect.isControlledByPlayer() == true}, increase the internal
	 * timer that determines if this is activated or not.
	 * 
	 * @param rect {@code MovingRectangle} to consider
	 */
	@Override
	protected void everyFrame(MovingRectangle rect) {
		if (rect.isControlledByPlayer()) {
			timer++;
		}
	}

	/**
	 * Marks that this has been used and should not be considered won anymore.
	 * <p>
	 * Until the player leaves and re-enters, calls to {@code hasWon()} will
	 * return false.
	 */
	public void markUsed() {
		used = true;
	}

	public String getNextLevel() {
		return nextLevel;
	}

}
