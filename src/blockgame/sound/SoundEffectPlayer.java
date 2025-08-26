package blockgame.sound;

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
import blockgame.input.VolumeMapper;
import blockgame.input.VolumeMapper.Volume;
import blockgame.physics.GoalArea;
import blockgame.physics.MovingRectangle;
import blockgame.physics.MovingRectangle.State;
import blockgame.physics.SwitchRectangle;

/**
 * Monitors the game state and plays sound effects when necessary.
 * <p>
 * After objects have been added to this, {@code playSounds()} should be called
 * every frame. It should be called after {@code PhysicsSimulator} has done its
 * work and before any object's state is changed.
 * 
 * @author Frank Kormann
 */
public class SoundEffectPlayer {

	private static final int MIN_FALL_DISTANCE = 20;

	/**
	 * Sound effect containing a {@code Clip} which is pre-loaded with its data.
	 */
	public enum SoundEffect {
		LEVEL_COMPLETE("/level_complete.wav"),
		LEVEL_COMPLETE_SPECIAL("/nananana.wav"), GROW("/grow.wav"),
		SHRINK("/shrink.wav"), LAND("/land.wav"), SWITCH_ON("/switch_on.wav");

		public final Clip clip;

		private SoundEffect(String resource) {
			clip = loadClip(resource);
		}

		private Clip loadClip(String resource) {
			try (AudioInputStream stream = AudioSystem
					.getAudioInputStream(getClass().getResource(resource))) {
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
	private List<SwitchRectangle> switchRectangles;
	private List<GoalArea> goals;

	private Map<MovingRectangle, State> rectStates;
	private Map<MovingRectangle, Integer> fallDistances;

	VolumeMapper volumeMapper;

	/**
	 * Creates a new {@code SoundEffectPlayer} with no objects.
	 */
	public SoundEffectPlayer(VolumeMapper volumeMapper) {
		movingRectangles = new ArrayList<>();
		switchRectangles = new ArrayList<>();
		goals = new ArrayList<>();
		rectStates = new HashMap<>();
		fallDistances = new HashMap<>();
		this.volumeMapper = volumeMapper;
	}

	public void add(MovingRectangle rect) {
		movingRectangles.add(rect);
		if (rect instanceof SwitchRectangle) {
			switchRectangles.add((SwitchRectangle) rect);
		}
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
		playIfAnyMatch(goals, g -> g.playingLevelFinish() && !g.isSpecial(),
				SoundEffect.LEVEL_COMPLETE.clip, false);
		playIfAnyMatch(goals, g -> g.playingLevelFinish() && g.isSpecial(),
				SoundEffect.LEVEL_COMPLETE_SPECIAL.clip, false);
		playIfAnyMatch(movingRectangles,
				r -> r.getWidth() > r.getLastWidth()
						|| r.getHeight() > r.getLastHeight(),
				SoundEffect.GROW.clip, false);
		playIfAnyMatch(movingRectangles,
				r -> r.getWidth() < r.getLastWidth()
						|| r.getHeight() < r.getLastHeight(),
				SoundEffect.SHRINK.clip, false);
		playIfAnyMatch(switchRectangles, r -> r.becameActive(),
				SoundEffect.SWITCH_ON.clip, true);
		playIfAnyMatch(movingRectangles,
				r -> fallDistances.containsKey(r)
						&& fallDistances.get(r) >= MIN_FALL_DISTANCE
						&& r.getState() == State.ON_GROUND,
				SoundEffect.LAND.clip, true);

		updateFallDistances();
	}

	/**
	 * Starts {@code clip} if anything in {@code objects} matches
	 * {@code condition}.
	 * <p>
	 * Subsequent calls cannot make {@code clip} play multiple times
	 * simultaneously. If {@code restartPrevious == true}, {@code clip} will be
	 * restarted. Otherwise, subsequent calls will have no effect for a running
	 * {@code clip}.
	 * 
	 * @param <T>             type of objects to test
	 * @param objects         {@code Collection} of objects to test
	 * @param condition       {@code Predicate} to test against
	 * @param clip            sound {@code Clip} to play
	 * @param restartPrevious {@code true} if {@code clip} should be restarted
	 *                        if it is already running
	 */
	private <T> void playIfAnyMatch(Collection<T> objects,
			Predicate<T> condition, Clip clip, boolean restartPrevious) {
		if (objects.stream().anyMatch(condition)) {
			if (restartPrevious) {
				clip.stop();
			}
			if (!clip.isRunning()) {
				clip.setFramePosition(0);
				while (!clip.isRunning()) {  // Make sure it starts (sometimes
					clip.start();			  // it won't start right away soon
				}							  // after being stopped)
				VolumeChanger.setVolume(clip,
						volumeMapper.get(Volume.SFX).floatValue());
			}
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
