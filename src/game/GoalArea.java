package game;

import java.awt.Color;
import java.awt.Graphics;
import java.net.URL;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Advances to the next level when a {@code MovingRectangle }controlled by the
 * player has stayed within it for a long enough time.
 * 
 * @author Frank Kormann
 */
public class GoalArea extends Area {

	public static final Color DEFAULT_COLOR = new Color(246, 190, 0, 128);

	private static final int TIMEOUT = 100;

	private int timer;
	private URL nextLevel;

	@JsonCreator
	public GoalArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width, @JsonProperty("height") int height,
			@JsonProperty("nextLevel") String nextLevel) {
		this(x, y, width, height, GoalArea.class.getResource(nextLevel));
	}

	public GoalArea(int x, int y, int width, int height, URL nextLevel) {
		super(x, y, width, height, DEFAULT_COLOR);
		timer = 0;
		this.nextLevel = nextLevel;
	}

	public boolean hasWon() {
		return timer >= TIMEOUT;
	}

	@Override
	public void draw(Graphics g, int xOffset, int yOffset) {
		super.draw(g, xOffset, yOffset);
		// Create a loading bar effect as timer approaches TIMEOUT
		g.setColor(getColor().darker());
		int fillHeight = getHeight() * timer / TIMEOUT;
		g.fillRect(getX() + xOffset, getY() + yOffset + getHeight() - fillHeight,
				getWidth(), fillHeight);
	}

	@Override
	public void onEnter(MovingRectangle rect) {}

	@Override
	public void onExit(MovingRectangle rect) {
		if (rect.isControlledByPlayer()) {
			timer = 0;
		}
	}

	@Override
	public void everyFrame(MovingRectangle rect) {
		if (rect.isControlledByPlayer()) {
			timer++;
		}
	}

	public URL getNextLevel() {
		return nextLevel;
	}

}
