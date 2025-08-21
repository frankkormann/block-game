package blockgame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import blockgame.gui.ErrorDialog;
import blockgame.physics.GoalArea;
import blockgame.physics.MovingRectangle;

/**
 * Monitors the game state and plays sound effects when necessary.
 * <p>
 * After objects have been added to this, {@code playSounds()} should be called
 * every frame. It should be called after {@code PhysicsSimulator} has done its
 * work and before any objects' states are changed.
 * 
 * @author Frank Kormann
 */
public class SoundEffectMonitor {

	private static final String LEVEL_COMPLETE = "/level_complete.wav";
	private static final String GROW = "/grow.wav";
	private static final String SHRINK = "/shrink.wav";

	private List<MovingRectangle> movingRectangles;
	private List<GoalArea> goals;
	private Map<String, Clip> clips;

	/**
	 * Creates a new {@code SoundEffectMonitor} with no objects.
	 */
	public SoundEffectMonitor() {
		movingRectangles = new ArrayList<>();
		goals = new ArrayList<>();
		clips = new HashMap<>();

		clips.put(LEVEL_COMPLETE, loadClip(LEVEL_COMPLETE));
		clips.put(GROW, loadClip(GROW));
		clips.put(SHRINK, loadClip(SHRINK));
	}

	private Clip loadClip(String resource) {
		try (AudioInputStream stream = AudioSystem.getAudioInputStream(
				getClass().getResourceAsStream(resource))) {
			Clip clip = AudioSystem.getClip();
			clip.open(stream);
			return clip;
		}
		catch (IOException | UnsupportedAudioFileException
				| LineUnavailableException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Failed to play sound effect", e)
					.setVisible(true);
			return null;
		}
	}

	public void add(MovingRectangle rect) {
		movingRectangles.add(rect);
	}

	public void add(GoalArea goal) {
		goals.add(goal);
	}

	public void clear() {
		movingRectangles.clear();
		goals.clear();
	}

	/**
	 * Checks all objects this is monitoring and plays any sound effects which
	 * need to be played.
	 */
	public void playSounds() {
		playIfAnyMatch(goals, g -> g.hasWon() && g.hasParticles(),
				clips.get(LEVEL_COMPLETE));
		playIfAnyMatch(movingRectangles, r -> r.getWidth() > r.getLastWidth()
				|| r.getHeight() > r.getLastHeight(), clips.get(GROW));
		playIfAnyMatch(movingRectangles,
				r -> r.getWidth() < r.getLastWidth()
						|| r.getHeight() < r.getLastHeight(),
				clips.get(SHRINK));
	}

	private <T> void playIfAnyMatch(Collection<T> objects,
			Predicate<T> condition, Clip clip) {
		if (!clip.isRunning() && objects.stream().anyMatch(condition)) {
			clip.flush();
			clip.setFramePosition(0);
			clip.start();
		}
	}

}
