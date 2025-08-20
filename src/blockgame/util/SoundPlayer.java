package blockgame.util;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import blockgame.gui.ErrorDialog;

/**
 * Collection of functions to play sound effects.
 * 
 * @author Frank Kormann
 */
public class SoundPlayer {

	/**
	 * Plays the sound effect referred to by {@code resource}.
	 * 
	 * @param resource name of resource file
	 */
	public static void play(String resource) {
		try (AudioInputStream stream = AudioSystem.getAudioInputStream(
				SoundPlayer.class.getResourceAsStream(resource))) {
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
