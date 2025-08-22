package blockgame.sound;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import blockgame.gui.ErrorDialog;

/**
 * Loops music continuously. Available songs are enumerated in {@code Song}.
 * 
 * @author Frank Kormann
 */
public class MusicPlayer {

	public enum Song {
		ABMU("ABMU", "/music_abmu.wav"),
		WOLF_SYNTH("WolfSynth", "/music_wolf_synth.wav"),
		PV8("PV8", "/music_pv8.wav");

		public String name;
		public String resource;

		private Song(String name, String resource) {
			this.name = name;
			this.resource = resource;
		}
	}

	private int currentThread;
	private Song currentSong;

	public MusicPlayer() {
		currentThread = 0;
		currentSong = null;
	}

	/**
	 * Sets {@code song} to loop forever.
	 * 
	 * @param song {@code Song} to play
	 */
	public void play(Song song) {
		try {
			InputStream stream = getClass().getResourceAsStream(song.resource);
			SourceDataLine line = AudioSystem.getSourceDataLine(
					AudioSystem.getAudioFileFormat(stream).getFormat());
			line.open();
			line.start();

			currentSong = song;
			startThread(line, stream);
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Failed to read audio data", e)
					.setVisible(true);
		}
		catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Song file is not an audio file", e)
					.setVisible(true);
		}
		catch (LineUnavailableException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Can't get a line for playing music", e)
					.setVisible(true);
		}
	}

	private void startThread(SourceDataLine line, InputStream stream) {
		currentThread++;
		int threadNumber = currentThread;
		new Thread(() -> {
			try {
				while (currentThread == threadNumber) {
					stream.mark(Integer.MAX_VALUE);
					while (currentThread == threadNumber
							&& stream.available() > 0) {

						byte[] buffer = new byte[line.available()];
						int num = stream.read(buffer, 0, buffer.length);
						line.write(buffer, 0, num);
					}
					stream.reset();
				}
				line.close();
				stream.close();
			}
			catch (IOException e) {
				e.printStackTrace();
				new ErrorDialog("Error", "Failed to read audio data", e)
						.setVisible(true);
			}
		}).start();
	}

	/**
	 * Stops playing music.
	 */
	public void stop() {
		currentThread++;  // The Thread currently playing music will stop
	}

	/**
	 * The {@code Song} which is currently playing, or {@code null} if there is
	 * nothing playing.
	 * 
	 * @return the {@code Song} which is playing or {@code null}
	 */
	public Song getCurrentSong() {
		return currentSong;
	}

}
