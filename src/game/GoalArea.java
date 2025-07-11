package game;

import java.awt.Color;
import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Advances to the next level when a {@code MovingRectangle} controlled by the
 * player has stayed within it for a long enough time.
 * 
 * @author Frank Kormann
 */
public class GoalArea extends Area {

	public static final Color DEFAULT_COLOR = new Color(246, 190, 0, 128);

	private static final int TIMEOUT = 100;

	private int timer;
	private boolean used;
	private String nextLevel;

	@JsonCreator
	public GoalArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width, @JsonProperty("height") int height,
			@JsonProperty("nextLevel") String nextLevel) {
		super(x, y, width, height, DEFAULT_COLOR);
		timer = 0;
		used = false;
		this.nextLevel = nextLevel;

		if (nextLevel == "") {
			System.err
					.println("GoalArea at " + x + ", " + y + ": nextLevel is nothing");
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
		g.fillRect(getX(), getY() + getHeight() - fillHeight, getWidth(), fillHeight);

		g.dispose();
	}

	@Override
	protected void onEnter(MovingRectangle rect) {}

	@Override
	protected void onExit(MovingRectangle rect) {
		if (rect.isControlledByPlayer()) {
			timer = 0;
		}
		used = false;
	}

	@Override
	protected void everyFrame(MovingRectangle rect) {
		if (rect.isControlledByPlayer()) {
			timer++;
		}
	}

	/**
	 * Marks that this has been used and should not be considered won anymore.
	 * <p>
	 * Until the player leaves and re-enters, calls to {@code hasWon()} will return
	 * false.
	 */
	public void markUsed() {
		used = true;
	}

	public String getNextLevel() {
		return nextLevel;
	}

}
