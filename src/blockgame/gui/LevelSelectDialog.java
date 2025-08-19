package blockgame.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
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
import blockgame.util.Pair;

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

	private GameController gameController;

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

		JTabbedPane tabbedPane = new JTabbedPane();
		Map<String, Map<String, Pair<String, Integer>>> levelMap = loadLevelMap();
		for (Entry<String, Map<String, Pair<String, Integer>>> world : levelMap
				.entrySet()) {
			tabbedPane.addTab(world.getKey(),
					createWorldPanel(world.getValue()));
		}
		// Manually chosen so that no tabs overflow
		// TODO Automatically detect the right size
		tabbedPane.setPreferredSize(
				new Dimension(UIScale.scale(489), UIScale.scale(171)));
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
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Failed to read level index data", e)
					.setVisible(true);

			return null;
		}
	}

	private JPanel createWorldPanel(Map<String, Pair<String, Integer>> levels) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		for (Entry<String, Pair<String, Integer>> level : levels.entrySet()) {
			panel.add(Box.createVerticalStrut(SPACE));
			panel.add(createLevelButtonPanel(level.getKey(),
					level.getValue().first));
		}

		for (Component comp : panel.getComponents()) {
			if (comp instanceof JComponent) {
				((JComponent) comp).setAlignmentX(CENTER_ALIGNMENT);
			}
		}

		return panel;
	}

	private JPanel createLevelButtonPanel(String name, String path) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JButton loadButton = new JButton("Load");
		loadButton.addActionListener(e -> {
			gameController.loadLevel(path);
			dispose();
		});

		panel.add(Box.createHorizontalStrut(SPACE));
		panel.add(new JLabel(name));
		panel.add(Box.createHorizontalGlue());
		panel.add(loadButton);

		return panel;
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
