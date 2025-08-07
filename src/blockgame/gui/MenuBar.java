package blockgame.gui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import blockgame.GameController;
import blockgame.input.ColorMapper;
import blockgame.input.InputMapper;
import blockgame.input.ParameterMapper;
import blockgame.input.ValueChangeListener;
import blockgame.util.Pair;

/**
 * {@code JMenuBar} to handle inputs which are not directly related to playing
 * the game, for example pausing, reloading the level, or saving a recording
 * file.
 * <p>
 * If an input requires multiple pieces to be acted on, this will interrupt the
 * game to get the extra information. For instance, loading a file requires
 * asking the player which file to load.
 * 
 * @author Frank Kormann
 */
public class MenuBar extends JMenuBar implements ValueChangeListener {

	public enum MetaInput {
		PAUSE, FRAME_ADVANCE, RELOAD_LEVEL, TOGGLE_HINTS, PLAY_SOLUTION,
		SAVE_RECORDING, PLAY_RECORDING, STOP_RECORDING
	}

	private GameController listener;
	private InputMapper inputMapper;

	private JFileChooser fileChooser;
	private Map<Enum<?>, JMenuItem> inputToMenuItem;

	private JMenuItem showHintItem;
	private JMenuItem showSolutionItem;
	private JMenuItem pauseItem;
	private JMenuItem frameAdvanceItem;

	/**
	 * Creates a new {@code MenuBar} to handle {@code MetaInput}s. Inputs are
	 * sent to {@code listener}. {@code MetaInput} keybinds are taken from
	 * {@code inputMapper}.
	 * <p>
	 * Includes an {@code OptionsDialog} to change mappings in
	 * {@code inputMapper}, {@code colorMapper}, and {@code paramMaper}.
	 * 
	 * @param inputMapper {@code InputMapper} to take keybinds from and to alter
	 *                    in {@code OptionsDialog}
	 * @param colorMapper {@code ColorMapper} to alter in {@code OptionsDialog}
	 * @param paramMapper {@code ParameterMapper} to alter in
	 *                    {@code OptionsDialog}
	 * @param listener    {@code GameController} which will process inputs
	 */
	public MenuBar(InputMapper inputMapper, ColorMapper colorMapper,
			ParameterMapper paramMapper, GameController listener) {
		this.listener = listener;
		this.inputMapper = inputMapper;
		inputToMenuItem = new HashMap<>();

		fileChooser = new JFileChooser();  // global file chooser so it
											  // remembers which directory the
											  // user was in if they open it
											  // multiple times
		fileChooser.setFileFilter(
				new FileNameExtensionFilter("Recording Files (.rec)", "rec"));

		inputMapper.addListener(this);

		add(createHintMenu());
		add(createRecordingMenu());
		add(createPauseRestartMenu());
		add(createOptionsButton(colorMapper, paramMapper));
	}

	/**
	 * Resets the enabledness and selectedness of certain {@code JMenuItem}s
	 * back to their original states.
	 */
	public void reset() {
		showHintItem.setSelected(false);
		pauseItem.setSelected(false);
		showSolutionItem.setEnabled(false);
		frameAdvanceItem.setEnabled(false);
	}

	private JMenu createHintMenu() {
		JMenu menu = new JMenu("Hint");

		showHintItem = createMenuItem("Show hint", MetaInput.TOGGLE_HINTS,
				this::hintsAction, true);

		showSolutionItem = createMenuItem("Show solution",
				MetaInput.PLAY_SOLUTION, this::showSolutionAction, false);
		showSolutionItem.addPropertyChangeListener("enabled", e -> {
			showSolutionItem.setToolTipText(showSolutionItem.isEnabled() ? null
					: "Please view the hint before using this option");
		});
		showSolutionItem.setEnabled(false);

		menu.add(showHintItem);
		menu.add(showSolutionItem);

		return menu;
	}

	private JMenu createRecordingMenu() {
		JMenu menu = new JMenu("Recordings");

		JMenuItem openItem = createMenuItem("Open", MetaInput.PLAY_RECORDING,
				this::openRecordingAction, false);

		JMenuItem saveItem = createMenuItem("Save current",
				MetaInput.SAVE_RECORDING, this::saveRecordingAction, false);

		JMenuItem stopItem = createMenuItem("Stop", MetaInput.STOP_RECORDING,
				this::stopRecordingAction, false);

		menu.add(openItem);
		menu.add(saveItem);
		menu.add(stopItem);

		return menu;
	}

	private JMenu createPauseRestartMenu() {
		JMenu menu = new JMenu("Pause/Retry");

		pauseItem = createMenuItem("Pause", MetaInput.PAUSE, this::pauseAction,
				true);

		JMenuItem restartItem = createMenuItem("Retry", MetaInput.RELOAD_LEVEL,
				this::restartAction, false);

		frameAdvanceItem = createMenuItem("Frame advance",
				MetaInput.FRAME_ADVANCE, this::frameAdvanceAction, false);

		menu.add(pauseItem);
		menu.add(restartItem);
		menu.add(frameAdvanceItem);

		return menu;
	}

	private JMenu createOptionsButton(ColorMapper colorMapper,
			ParameterMapper paramMapper) {
		JMenu button = new JMenu("Options");
		Runnable openDialog = () -> {
			new OptionsDialog(SwingUtilities.getWindowAncestor(button),
					inputMapper, colorMapper, paramMapper).setVisible(true);
		};

		button.addMenuKeyListener(new MenuKeyListener() {
			@Override
			public void menuKeyPressed(MenuKeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_ENTER
						|| e.getKeyCode() == KeyEvent.VK_SPACE)
						&& button.isSelected()) {
					openDialog.run();
				}
			}

			@Override
			public void menuKeyTyped(MenuKeyEvent e) {}

			@Override
			public void menuKeyReleased(MenuKeyEvent e) {}
		});
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				openDialog.run();
			}
		});

		return button;
	}

	/**
	 * Creates a {@code JMenuItem} with the specified properties. {@code action}
	 * will be run when the item is selected or the shortcut from
	 * {@code metaInput} is pressed.
	 * 
	 * @param text       to display on the menu item
	 * @param metaInput  {@code MetaInput} to pass to pull keybind information
	 *                   from
	 * @param action     {@code Runnable} which will run when this is activated
	 * @param isCheckBox whether to use a {@code JCheckBoxMenuItem} or not
	 * 
	 * @return the {@code JMenuItem}
	 */
	private JMenuItem createMenuItem(String text, MetaInput metaInput,
			Runnable action, boolean isCheckBox) {

		JMenuItem menuItem;
		if (isCheckBox) {
			menuItem = new JCheckBoxMenuItem(text);
		}
		else {
			menuItem = new JMenuItem(text);
		}

		Pair<Integer, Integer> keybind = inputMapper.get(metaInput);
		menuItem.addActionListener(e -> action.run());
		if (keybind != null && keybind.first != 0) {
			menuItem.setAccelerator(
					KeyStroke.getKeyStroke(keybind.first, keybind.second));
		}

		inputToMenuItem.put(metaInput, menuItem);

		return menuItem;
	}

	private File promptFileSaveLocation() {

		int fileChooserResult;
		File saveFile;

		boolean canSave;
		do {
			canSave = true;

			fileChooser.setSelectedFile(new File("Untitled.rec"));

			fileChooserResult = fileChooser
					.showSaveDialog(SwingUtilities.getWindowAncestor(this));
			saveFile = fileChooser.getSelectedFile();
			if (saveFile == null) {
				return null;
			}

			if (!saveFile.getName().matches("^.*\\..*$")) {
				saveFile = new File(saveFile.getPath() + ".rec");
			}

			if (fileChooserResult == JFileChooser.APPROVE_OPTION
					&& saveFile.exists()) {
				String message = saveFile.getName()
						+ " already exists.\nDo you want to replace it?";
				canSave = JOptionPane.showConfirmDialog(fileChooser, message,
						"Confirm save",
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION;
			}
		} while (!canSave);

		if (fileChooserResult == JFileChooser.APPROVE_OPTION) {
			return saveFile;
		}

		return null;
	}

	private File promptFileOpenLocation() {
		int result = fileChooser
				.showOpenDialog(SwingUtilities.getWindowAncestor(this));
		File openFile = fileChooser.getSelectedFile();

		if (result == JFileChooser.APPROVE_OPTION) {
			return openFile;
		}

		return null;
	}

	private void hintsAction() {
		listener.processMetaInput(MetaInput.TOGGLE_HINTS);
		showSolutionItem.setEnabled(true);
	}

	private void showSolutionAction() {
		listener.processMetaInput(MetaInput.PLAY_SOLUTION);
	}

	private void openRecordingAction() {
		File file = promptFileOpenLocation();
		if (file == null) {
			return;
		}
		listener.processMetaInput(MetaInput.PLAY_RECORDING, file);
	}

	private void saveRecordingAction() {
		File saveFile = promptFileSaveLocation();
		if (saveFile == null) {
			return;
		}
		listener.processMetaInput(MetaInput.SAVE_RECORDING, saveFile);
	}

	private void stopRecordingAction() {
		listener.processMetaInput(MetaInput.STOP_RECORDING);
	}

	private void pauseAction() {
		listener.processMetaInput(MetaInput.PAUSE);
		frameAdvanceItem.setEnabled(!frameAdvanceItem.isEnabled());
	}

	private void restartAction() {
		listener.processMetaInput(MetaInput.RELOAD_LEVEL);
	}

	private void frameAdvanceAction() {
		listener.processMetaInput(MetaInput.FRAME_ADVANCE);
	}

	@Override
	public void valueChanged(Enum<?> input, Object newKeybind) {
		if (inputToMenuItem.containsKey(input)) {
			Pair<Integer, Integer> keybind = (Pair<Integer, Integer>) newKeybind;
			inputToMenuItem.get(input)
					.setAccelerator(KeyStroke.getKeyStroke(keybind.first,
							keybind.second));
		}
	}

	@Override
	public void valueRemoved(Enum<?> input) {
		if (inputToMenuItem.containsKey(input)) {
			inputToMenuItem.get(input).setAccelerator(null);
		}
	}

}
