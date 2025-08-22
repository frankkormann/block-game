package blockgame.physics;

import java.awt.Graphics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import blockgame.gui.ParticleExplosion;

/**
 * Advances to the next level when a {@code MovingRectangle} controlled by the
 * player has stayed within it for a long enough time.
 * 
 * @author Frank Kormann
 */
public class GoalArea extends Area {

	private static final int TIMEOUT = 100;
	private static final int PARTICLE_EFFECT_LENGTH = 30;
	private static final int PARTICLE_COUNT = 50;
	private static final int PARTICLE_SIZE = 5;

	private int timer;
	private boolean used;
	private String nextLevel;
	private boolean hasParticles;
	private ParticleExplosion particleExplosion;

	@JsonCreator
	public GoalArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("nextLevel") String nextLevel,
			@JsonProperty("hasParticles") boolean hasParticles) {
		super(x, y, width, height, Colors.TRANSLUCENT_YELLOW);
		timer = 0;
		used = false;
		this.nextLevel = nextLevel;
		this.hasParticles = hasParticles;
		particleExplosion = hasParticles ? new ParticleExplosion() : null;

		if (nextLevel == "") {
			System.err.println("In GoalArea.java constructor: GoalArea at " + x
					+ ", " + y + ": nextLevel is nothing");
		}
	}

	public boolean hasWon() {
		int fullTimeout = TIMEOUT;
		if (hasParticles) {
			fullTimeout += PARTICLE_EFFECT_LENGTH;
		}
		return timer >= fullTimeout && !used;
	}

	@Override
	public void draw(Graphics g) {
		super.draw(g);
		g = g.create();

		g.setColor(getColor().darker());
		int fillHeight = getHeight() * timer / TIMEOUT;
		fillHeight = Math.min(fillHeight, getHeight());
		g.fillRect(getX(), getY() + getHeight() - fillHeight, getWidth(),
				fillHeight);

		if (hasParticles) {
			final Graphics g2 = g;
			g2.setColor(getColor());
			particleExplosion.draw(g2);
			g2.dispose();
		}

		g.dispose();
	}

	/**
	 * Not implemented.
	 * 
	 * @param rect unused
	 */
	@Override
	public void onEnter(MovingRectangle rect) {}

	/**
	 * Resets the internal timer to {@code 0} if
	 * {@code rect.isControlledByPlayer() == true}.
	 * 
	 * @param rect {@code MovingRectangle} to consider
	 */
	@Override
	public void onExit(MovingRectangle rect) {
		if (rect.isControlledByPlayer()) {
			timer = 0;
			if (hasParticles) {
				particleExplosion.stop();
			}
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
	public void everyFrame(MovingRectangle rect) {
		if (rect.isControlledByPlayer()) {
			timer++;
			if (hasParticles) {
				if (timer == TIMEOUT) {
					particleExplosion.start(PARTICLE_COUNT, PARTICLE_SIZE,
							getX() + getWidth() / 2, getY() + getHeight() / 2,
							-5, 5, -5, 2);
				}
				particleExplosion.nextFrame();
			}
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

	public boolean hasParticles() {
		return hasParticles;
	}

}
