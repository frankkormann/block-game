package blockgame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
	private List<MovingRectangle> fallingRects;

	/**
	 * Creates a new {@code SoundEffectMonitor} with no objects.
	 */
	public SoundEffectMonitor() {
		movingRectangles = new ArrayList<>();
		goals = new ArrayList<>();
		fallingRects = new ArrayList<>();
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
				SoundEffect.LEVEL_COMPLETE.clip, false);
		playIfAnyMatch(movingRectangles,
				r -> r.getWidth() > r.getLastWidth()
						|| r.getHeight() > r.getLastHeight(),
				SoundEffect.GROW.clip, true);
		playIfAnyMatch(movingRectangles,
				r -> r.getWidth() < r.getLastWidth()
						|| r.getHeight() < r.getLastHeight(),
				SoundEffect.SHRINK.clip, true);
		playIfAnyMatch(movingRectangles,
				r -> fallingRects.contains(r)
						&& r.getState() == MovingRectangle.State.ON_GROUND,
				SoundEffect.LAND.clip, false);

		fallingRects.clear();
		movingRectangles.stream()
				.filter(r -> r.getState() == MovingRectangle.State.IN_AIR)
				.forEach(r -> fallingRects.add(r));
	}

	private <T> void playIfAnyMatch(Collection<T> objects,
			Predicate<T> condition, Clip clip, boolean stopIfNone) {
		if (!clip.isRunning() && objects.stream().anyMatch(condition)) {
			clip.flush();
			clip.setFramePosition(0);
			clip.start();
		}
		else if (stopIfNone && clip.isRunning()
				&& objects.stream().noneMatch(condition)) {
			clip.stop();
		}
	}

}
