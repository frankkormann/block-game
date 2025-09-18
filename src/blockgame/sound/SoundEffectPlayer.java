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
import blockgame.input.SoundMapper;
import blockgame.input.SoundMapper.SoundControl;
import blockgame.physics.GoalArea;
import blockgame.physics.MovingRectangle;
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

	private static final int MIN_LAND_VELOCITY = 10;

	/**
	 * Sound effect containing a {@code Clip} which is pre-loaded with its data.
	 */
	public enum SoundEffect {
		GAME_START("/start_up.wav"), LEVEL_COMPLETE("/level_complete.wav"),
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
				ErrorDialog.showDialog("Failed to load sound effect", e);
				return null;
			}
		}
	}

	private List<MovingRectangle> movingRectangles;
	private List<SwitchRectangle> switchRectangles;
	private List<GoalArea> goals;

	private Map<MovingRectangle, Integer> rectYVelocities;
	private Map<GoalArea, Boolean> goalsActivated;

	SoundMapper soundMapper;

	/**
	 * Creates a new {@code SoundEffectPlayer} with no objects.
	 * 
	 * @param soundMapper {@code SoundMapper} to take volume information from
	 */
	public SoundEffectPlayer(SoundMapper soundMapper) {
		movingRectangles = new ArrayList<>();
		switchRectangles = new ArrayList<>();
		goals = new ArrayList<>();
		rectYVelocities = new HashMap<>();
		goalsActivated = new HashMap<>();
		this.soundMapper = soundMapper;
	}

	public void add(MovingRectangle rect) {
		movingRectangles.add(rect);
		if (rect instanceof SwitchRectangle) {
			switchRectangles.add((SwitchRectangle) rect);
		}
	}

	public void add(GoalArea goal) {
		goals.add(goal);
		goalsActivated.put(goal, false);
	}

	public void clear() {
		movingRectangles.clear();
		switchRectangles.clear();
		goals.clear();
		rectYVelocities.clear();
		goalsActivated.clear();
	}

	/**
	 * Checks all objects this is monitoring and plays any sound effects which
	 * need to be played.
	 */
	public void playSounds() {
		playIfAnyMatch(goals,
				g -> g.playingLevelFinish() && !g.isSpecial()
						&& !goalsActivated.get(g),
				SoundEffect.LEVEL_COMPLETE, false);
		playIfAnyMatch(goals,
				g -> g.playingLevelFinish() && g.isSpecial()
						&& !goalsActivated.get(g),
				SoundEffect.LEVEL_COMPLETE_SPECIAL, false);
		playIfAnyMatch(movingRectangles,
				r -> r.getWidth() > r.getLastWidth()
						|| r.getHeight() > r.getLastHeight(),
				SoundEffect.GROW, false);
		playIfAnyMatch(movingRectangles,
				r -> r.getWidth() < r.getLastWidth()
						|| r.getHeight() < r.getLastHeight(),
				SoundEffect.SHRINK, false);
		playIfAnyMatch(switchRectangles, r -> r.becameActive(),
				SoundEffect.SWITCH_ON, true);
		playIfAnyMatch(movingRectangles,
				r -> rectYVelocities.containsKey(r)
						&& rectYVelocities.get(r) >= MIN_LAND_VELOCITY
						&& r.getYVelocity() == 0,  // So it has now landed
				SoundEffect.LAND, true);

		updateRectVelocities();
		updateGoalsActivated();
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
	 * @param soundEffect     {@code SoundEffect} to play
	 * @param restartPrevious {@code true} if {@code clip} should be restarted
	 *                        if it is already running
	 */
	private <T> void playIfAnyMatch(Collection<T> objects,
			Predicate<T> condition, SoundEffect soundEffect,
			boolean restartPrevious) {
		if (objects.stream().anyMatch(condition)) {
			if (restartPrevious) {
				soundEffect.clip.stop();
			}
			if (!soundEffect.clip.isRunning()) {
				play(soundEffect);
			}
		}
	}

	/**
	 * Plays {@code soundEffect} with the volume set by this's
	 * {@code SoundMapper}.
	 * 
	 * @param soundEffect {@code SoundEffect} to play
	 */
	public void play(SoundEffect soundEffect) {
		Clip clip = soundEffect.clip;
		clip.setFramePosition(0);
		SoundChanger.setVolume(clip,
				soundMapper.get(SoundControl.SFX).floatValue());
		SoundChanger.setLeftRightPosition(clip,
				soundMapper.get(SoundControl.LR_BALANCE).floatValue());
		// Make sure it starts (sometimes it) won't start right away soon after
		// being stopped)
		while (!clip.isRunning()) {
			clip.start();
		}
	}

	private void updateRectVelocities() {
		movingRectangles.forEach(r -> rectYVelocities.put(r, r.getYVelocity()));
	}

	private void updateGoalsActivated() {
		goals.forEach(g -> goalsActivated.put(g, g.playingLevelFinish()));
	}

}
