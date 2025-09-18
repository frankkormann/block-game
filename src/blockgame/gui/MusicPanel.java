package blockgame.gui;

import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import blockgame.input.SoundMapper;
import blockgame.sound.MusicPlayer;
import blockgame.sound.MusicPlayer.Song;

/**
 * {@code JPanel} which allows the user to select which song to play.
 * 
 * @author Frank Kormann
 */
public class MusicPanel extends JPanel {

	private static final String MUSIC_LABEL = "Music";
	private static final String NULL_SONG_NAME = "None";
	private static final int EDGE_SPACE = 3;

	private MusicPlayer player;

	/**
	 * Creates a {@code MusicPanel} to set the song in {@code player}.
	 * 
	 * @param rootPane    {@code JRootPane} of the {@code Window} this will be
	 *                    added to
	 * @param soundMapper {@code SoundMapper} to take volume settings from
	 * @param player      {@code MusicPlayer} to alter
	 */
	public MusicPanel(JRootPane rootPane, SoundMapper soundMapper,
			MusicPlayer player) {
		this.player = player;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(Box.createVerticalStrut(EDGE_SPACE));
		add(createMusicSelector());
		add(new SoundChangerPanel(rootPane, soundMapper));
	}

	private JPanel createMusicSelector() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JComboBox<String> selector = new JComboBox<>();
		Map<String, Song> nameToSong = new HashMap<>();

		selector.addItem(NULL_SONG_NAME);
		for (Song song : Song.values()) {
			if (getClass().getResource(song.resource) == null) {
				continue;
			}
			selector.addItem(song.name);
			nameToSong.put(song.name, song);
		}
		selector.setSelectedItem(
				player.getCurrentSong() == null ? NULL_SONG_NAME
						: player.getCurrentSong().name);

		selector.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				if (e.getItem().equals(NULL_SONG_NAME)) {
					player.stop();
				}
				else {
					player.play(nameToSong.get(e.getItem()));
				}
			}
		});

		panel.add(Box.createHorizontalStrut(EDGE_SPACE));
		panel.add(new JLabel(MUSIC_LABEL));
		panel.add(Box.createHorizontalGlue());
		panel.add(selector);

		return panel;
	}

}
