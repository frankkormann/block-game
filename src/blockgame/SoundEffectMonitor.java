package blockgame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.Clip;

import blockgame.physics.GoalArea;
import blockgame.physics.MovingRectangle;
import blockgame.util.SoundPlayer;

/**
 * Monitors the game state and plays sound effects when necessary.
 * <p>
 * After objects have been added to this, {@code playSounds()} should be called
 * every frame.
 * 
 * @author Frank Kormann
 */
public class SoundEffectMonitor {

	private static final String LEVEL_COMPLETE_SOUND = "/level_complete.wav";
	private static final String GROW_SOUND = "/expand.wav";

	private List<MovingRectangle> rects;
	private List<GoalArea> goals;
	private Map<String, Clip> clips;

	/**
	 * Creates a new {@code SoundEffectMonitor} with no objects.
	 */
	public SoundEffectMonitor() {
		rects = new ArrayList<>();
		goals = new ArrayList<>();
		clips = new HashMap<>();

	}

	public void add(MovingRectangle rect) {
		rects.add(rect);
	}

	public void add(GoalArea goal) {
		goals.add(goal);
	}

	public void clear() {
		rects.clear();
		goals.clear();
	}

	/**
	 * Checks all objects this is monitoring and plays any sound effects which
	 * need to be played.
	 */
	public void playSounds() {
		goals.forEach(g -> {
			if (g.hasWon() && g.hasParticles()) {
				SoundPlayer.play(LEVEL_COMPLETE_SOUND);
			}
		});
	}

}
