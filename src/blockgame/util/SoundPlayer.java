package blockgame.util;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import blockgame.gui.ErrorDialog;

/**
 * Collection of functions to play sound effects.
 * 
 * @author Frank Kormann
 */
public class SoundPlayer {

	static int playingCount = 0;

	/**
	 * Plays the sound effect referred to by {@code resource}.
	 * 
	 * @param resource name of resource file
	 */
	public static void play(String resource) {
		new Thread(() -> playSound(resource)).start();  // New thread to avoid
														  // impact of delay
														  // when loading sound
														  // for the first time
	}

	private static void playSound(String resource) {
		try (AudioInputStream stream = AudioSystem.getAudioInputStream(
				SoundPlayer.class.getResourceAsStream(resource))) {
			Clip clip = AudioSystem.getClip();
			clip.open(stream);
			if (AudioSystem.isLineSupported(clip.getLineInfo())) {
				clip.addLineListener(e -> {
					if (e.getType() == LineEvent.Type.START) {
						playingCount++;
					}
					if (e.getType() == LineEvent.Type.STOP) {
						playingCount--;
					}
				});
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

	/**
	 * Invokes {@code play(resource)} unless another sound is already playing.
	 * 
	 * @param resource name of resource file
	 * 
	 * @see #play(String)
	 */
	public static void playNoOverlap(String resource) {
		if (playingCount > 0) {
			return;
		}
		play(resource);
	}

}
