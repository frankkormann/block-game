package blockgame.sound;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import blockgame.gui.ErrorDialog;
import blockgame.input.ValueChangeListener;
import blockgame.input.VolumeMapper;
import blockgame.input.VolumeMapper.Volume;
import blockgame.util.SaveManager;

/**
 * Loops music continuously. Available songs are enumerated in {@code Song}.
 * <p>
 * The last played song will be stored in {@code SaveManager} under
 * {@code song}, and will be automatically started on subsequent instantiation.
 * 
 * @author Frank Kormann
 */
public class MusicPlayer implements ValueChangeListener {

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
	private SourceDataLine currentLine;
	private VolumeMapper volumeMapper;

	public MusicPlayer(VolumeMapper volumeMapper) {
		currentThread = 0;
		currentSong = null;
		currentLine = null;
		this.volumeMapper = volumeMapper;
		volumeMapper.addListener(this);

		try {
			play(Song.valueOf(Song.class,
					SaveManager.getValue("song", "none")));
		}
		catch (IllegalArgumentException ignored) {}
	}

	/**
	 * Sets {@code song} to loop forever.
	 * 
	 * @param song {@code Song} to play
	 */
	public void play(Song song) {
		try {
			InputStream stream = new BufferedInputStream(
					getClass().getResourceAsStream(song.resource));
			SourceDataLine line = AudioSystem.getSourceDataLine(
					AudioSystem.getAudioFileFormat(stream).getFormat());
			line.open();
			line.start();
			VolumeChanger.setVolume(line,
					volumeMapper.get(Volume.MUSIC).floatValue());

			currentSong = song;
			currentLine = line;
			SaveManager.putValue("song", song.name());
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

	/**
	 * Starts a {@code Thread} to copy audio data from {@code stream} to
	 * {@code line}. The {@code Thread} will terminate when
	 * {@code currentThread} changes.
	 * 
	 * @param line   {@code SourceDataLine} to move audio data into
	 * @param stream {@code InputStream} to take audio data from
	 */
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
		currentLine = null;
		currentSong = null;
		currentThread++;  // The Thread currently playing music will stop
		SaveManager.putValue("song", "none");
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

	@Override
	public void valueChanged(Enum<?> key, Object newValue) {
		if (key == Volume.MUSIC) {
			if (currentLine != null) {
				VolumeChanger.setVolume(currentLine,
						((Number) newValue).floatValue());
			}
		}
	}

	@Override
	public void valueRemoved(Enum<?> key) {}

}
