package blockgame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
				playSound(LEVEL_COMPLETE_SOUND);
			}
		});
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
