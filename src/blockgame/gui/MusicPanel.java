package blockgame.gui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import blockgame.input.VolumeChangerPanel;
import blockgame.input.VolumeMapper;
import blockgame.sound.MusicPlayer;
import blockgame.sound.MusicPlayer.Song;

/**
 * {@code JPanel} which allows the user to select which song to play.
 * 
 * @author Frank Kormann
 */
public class MusicPanel extends JPanel {

	private MusicPlayer player;

	/**
	 * Creates a {@code MusicPanel} to set the song in {@code player}.
	 * 
	 * @param player {@code MusicPlayer} to alter
	 */
	public MusicPanel(JRootPane rootPane, VolumeMapper volumeMapper,
			MusicPlayer player) {
		this.player = player;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(createMusicSelector());
		add(new VolumeChangerPanel(rootPane, volumeMapper));
	}

	private JPanel createMusicSelector() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JComboBox<String> selector = new JComboBox<>();
		Map<String, Song> nameToSong = new HashMap<>();

		selector.addItem("None");
		for (Song song : Song.values()) {
			selector.addItem(song.name);
			nameToSong.put(song.name, song);
		}
		selector.setSelectedItem(player.getCurrentSong() == null ? "None"
				: player.getCurrentSong().name);

		selector.addItemListener(e -> {
			if (e.getItem().equals("None")) {
				player.stop();
			}
			else {
				player.play(nameToSong.get(e.getItem()));
			}
		});

		panel.add(new JLabel("Song"));
		panel.add(Box.createHorizontalGlue());
		panel.add(selector);

		return panel;
	}

}
