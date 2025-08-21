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
import blockgame.physics.MovingRectangle.State;

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

	private static final int MIN_FALL_DISTANCE = 20;

	/**
	 * Sound effect containing a {@code Clip} which is pre-loaded with its data.
	 */
	public enum SoundEffect {
		LEVEL_COMPLETE("/level_complete.wav"), GROW("/grow.wav"),
		SHRINK("/shrink.wav"), LAND("/land.wav");

		public final Clip clip;

		private SoundEffect(String resource) {
			clip = loadClip(resource);
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
				new ErrorDialog("Error", "Failed to load sound effect", e)
						.setVisible(true);
				return null;
			}
		}
	}

	private List<MovingRectangle> movingRectangles;
	private List<GoalArea> goals;

	private Map<MovingRectangle, State> rectStates;
	private Map<MovingRectangle, Integer> fallDistances;

	/**
	 * Creates a new {@code SoundEffectMonitor} with no objects.
	 */
	public SoundEffectMonitor() {
		movingRectangles = new ArrayList<>();
		goals = new ArrayList<>();
		rectStates = new HashMap<>();
		fallDistances = new HashMap<>();
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
				SoundEffect.LEVEL_COMPLETE.clip, false, false);
		playIfAnyMatch(movingRectangles,
				r -> r.getWidth() > r.getLastWidth()
						|| r.getHeight() > r.getLastHeight(),
				SoundEffect.GROW.clip, false, true);
		playIfAnyMatch(movingRectangles,
				r -> r.getWidth() < r.getLastWidth()
						|| r.getHeight() < r.getLastHeight(),
				SoundEffect.SHRINK.clip, false, true);
		playIfAnyMatch(movingRectangles,
				r -> fallDistances.containsKey(r)
						&& fallDistances.get(r) >= MIN_FALL_DISTANCE
						&& r.getState() == State.ON_GROUND,
				SoundEffect.LAND.clip, true, false);

		updateFallDistances();
	}

	private <T> void playIfAnyMatch(Collection<T> objects,
			Predicate<T> condition, Clip clip, boolean restartPrevious,
			boolean stopIfNone) {
		if (objects.stream().anyMatch(condition)) {
			if (restartPrevious) {
				clip.stop();
			}
			if (!clip.isRunning() && objects.stream().anyMatch(condition)) {
				clip.flush();
				clip.setFramePosition(0);
				clip.start();
			}
		}
		else if (stopIfNone && clip.isRunning()) {
			clip.stop();
		}
	}

	private void updateFallDistances() {
		for (MovingRectangle rect : movingRectangles) {
			if (rectStates.get(rect) == State.ON_GROUND
					&& rect.getState() == State.IN_AIR) {
				fallDistances.put(rect, 0);
			}
			if (fallDistances.containsKey(rect)) {
				if (rect.getState() == State.ON_GROUND) {
					fallDistances.remove(rect);
					continue;
				}

				int dist = fallDistances.get(rect);
				dist += rect.getY() - rect.getLastY();
				dist = Math.max(dist, 0);
				fallDistances.put(rect, dist);
			}
		}
		movingRectangles.forEach(r -> rectStates.put(r, r.getState()));
	}

}
