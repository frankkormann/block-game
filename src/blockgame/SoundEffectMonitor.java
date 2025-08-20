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
 * every frame.
 * 
 * @author Frank Kormann
 */
public class SoundEffectMonitor {

	private static final String LEVEL_COMPLETE_SOUND = "/level_complete.wav";
	private static final String GROW_SOUND = "/expand.wav";

	private List<MovingRectangle> movingRectangles;
	private List<GoalArea> goals;

	/**
	 * Creates a new {@code SoundEffectMonitor} with no objects.
	 */
	public SoundEffectMonitor() {
		movingRectangles = new ArrayList<>();
		goals = new ArrayList<>();
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
				LEVEL_COMPLETE_SOUND);
		playIfAnyMatch(movingRectangles, r -> r.getWidth() > r.getLastWidth()
				|| r.getHeight() > r.getLastHeight(), GROW_SOUND);
	}

	private <T> void playIfAnyMatch(Collection<T> objects,
			Predicate<T> condition, String resource) {
		if (objects.stream().anyMatch(condition)) {
			playSound(resource);
		}
	}

	private void playSound(String resource) {
		try (AudioInputStream stream = AudioSystem.getAudioInputStream(
				getClass().getResourceAsStream(resource))) {
			Clip clip = AudioSystem.getClip();
			clip.open(stream);
			if (AudioSystem.isLineSupported(clip.getLineInfo())) {
				clip.start();
			}
		}
		catch (IOException | UnsupportedAudioFileException
				| LineUnavailableException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Failed to play sound effect", e)
					.setVisible(true);
		}
	}

}
