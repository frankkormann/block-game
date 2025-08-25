package blockgame.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.util.UIScale;

import blockgame.GameController;
import blockgame.gui.MenuBar.MetaInput;
import blockgame.util.Pair;
import blockgame.util.SaveManager;

/**
 * {@code JDialog} which allows the user to select which level to load from a
 * list sorted by world.
 * 
 * @author Frank Kormann
 */
public class LevelSelectDialog extends JDialog {

	private static final String TITLE = "Level Select";
	private static final String LEVEL_INDEX = "/level_select_index.json";
	private static final int SPACE = 3;

	private static final String UNVISITED_LEVEL_NAME = "???";
	private static final String COMPLETED_SYMBOL = "🏆";

	private GameController gameController;
	private long levelsCompleted;
	private long levelsVisited;

	/**
	 * Creates a {@code LevelSelectDialog} which will send level loads to
	 * {@code gameController}.
	 * 
	 * @param owner          {@code Window} which owns this
	 * @param gameController {@code GameController} to load levels
	 */
	public LevelSelectDialog(Window owner, GameController gameController) {
		super(owner, TITLE, Dialog.DEFAULT_MODALITY_TYPE);
		this.gameController = gameController;

		JPanel contentPanePanel = new JPanel(); // Ensure that content pane is a
		setContentPane(contentPanePanel);	   // JPanel so it can have a border
		contentPanePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		setLayout(new BorderLayout());
		levelsCompleted = Long
				.valueOf(SaveManager.getValue("completed_levels", "0"));
		levelsVisited = Long
				.valueOf(SaveManager.getValue("visited_levels", "0"));

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		Map<String, Map<String, Pair<String, Integer>>> levelMap = loadLevelMap();
		for (Entry<String, Map<String, Pair<String, Integer>>> world : levelMap
				.entrySet()) {
			boolean noLevelsAreLoadable = world.getValue()
					.values()
					.stream()
					.noneMatch(l -> isLevelLoadable(l.second));
			if (noLevelsAreLoadable) {
				continue;
			}
			tabbedPane.addTab(world.getKey(),
					createWorldPanel(world.getValue()));
		}
		// Manually chosen so that no tabs overflow
		// TODO Automatically detect the right size
		tabbedPane.setPreferredSize(
				new Dimension(UIScale.scale(585), UIScale.scale(171)));
		add(tabbedPane);

		registerDisposeOnKeypress(KeyEvent.VK_ESCAPE);

		pack();
		setLocationRelativeTo(null);
	}

	private Map<String, Map<String, Pair<String, Integer>>> loadLevelMap() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			Map<String, Map<String, Pair<String, Integer>>> map = mapper
					.readValue(getClass().getResourceAsStream(LEVEL_INDEX),
							new TypeReference<Map<String, Map<String, Pair<String, Integer>>>>() {});
			return map;
		}
		catch (IOException | IllegalArgumentException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Failed to read level index data", e)
					.setVisible(true);

			return new HashMap<>();
		}
	}

	private JPanel createWorldPanel(Map<String, Pair<String, Integer>> levels) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		for (Entry<String, Pair<String, Integer>> level : levels.entrySet()) {
			panel.add(Box.createVerticalStrut(SPACE));
			String levelName = level.getValue().first;
			int levelNumber = level.getValue().second;
			panel.add(createLevelButtonPanel(level.getKey(), levelName,
					isLevelLoadable(levelNumber),
					levelInField(levelNumber, levelsCompleted)));
		}

		for (Component comp : panel.getComponents()) {
			if (comp instanceof JComponent) {
				((JComponent) comp).setAlignmentX(CENTER_ALIGNMENT);
			}
		}

		return panel;
	}

	private JPanel createLevelButtonPanel(String name, String path,
			boolean isLevelLoadable, boolean isLevelComplete) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JLabel nameLabel = new JLabel(name);

		JButton loadButton = new JButton("Load");
		loadButton.addActionListener(e -> {
			gameController.processMetaInput(MetaInput.STOP_RECORDING);
			gameController.loadLevel(path);
			dispose();
		});

		if (!isLevelLoadable) {
			nameLabel.setForeground(Color.GRAY);
			nameLabel.setText(
					name.replaceFirst(": .*", ": " + UNVISITED_LEVEL_NAME));
			loadButton.setEnabled(false);
		}

		panel.add(Box.createHorizontalStrut(SPACE));
		panel.add(nameLabel);
		panel.add(Box.createHorizontalGlue());
		if (isLevelComplete) {
			panel.add(new JLabel(COMPLETED_SYMBOL));
			panel.add(Box.createHorizontalStrut(SPACE));
		}
		panel.add(loadButton);

		return panel;
	}

	private boolean isLevelLoadable(int level) {
		return levelInField(level, levelsVisited)
				|| SaveManager.getValue("game_complete", "false")
						.equals("true");
	}

	private boolean levelInField(int level, long field) {
		return (field & (1L << level)) != 0;
	}

	/**
	 * Sets this to close when the key corresponding to {@code keyCode} is
	 * pressed.
	 * 
	 * @param keyCode which key should be pressed
	 */
	private void registerDisposeOnKeypress(int keyCode) {
		AbstractAction dispose = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};

		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(keyCode, 0), "close");
		getRootPane().getActionMap().put("close", dispose);
	}

}
