package game;

import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * JMenuBar to handle inputs which are not directly related to playing the game,
 * for example pausing, reloading the level, or saving a recording file.
 * <p>
 * If an input requires multiple pieces to be acted on, this will interrupt the
 * game to get the extra information. For instance, loading a file requires
 * asking the player which file to load.
 * 
 * @author Frank Kormann
 */
public class MenuBar extends JMenuBar {

	public enum MetaInput {
		PAUSE(KeyEvent.VK_P, 0), FRAME_ADVANCE(KeyEvent.VK_N, 0),
		RELOAD_LEVEL(KeyEvent.VK_R, 0), TOGGLE_HINTS(KeyEvent.VK_H, 0),
		PLAY_SOLUTION(KeyEvent.VK_H, MetaInput.SHIFT_CONTROL_MASK),
		SAVE_RECORDING(KeyEvent.VK_S, MetaInput.SHIFT_CONTROL_MASK),
		PLAY_RECORDING(KeyEvent.VK_P, MetaInput.SHIFT_CONTROL_MASK),
		STOP_RECORDING(KeyEvent.VK_S, 0);

		private static final int SHIFT_CONTROL_MASK = KeyEvent.CTRL_DOWN_MASK
				| KeyEvent.SHIFT_DOWN_MASK;
		/**
		 * Key code which will trigger this
		 */
		public final int keyCode;
		/**
		 * {@code KeyEvent} modifier key mask which is required to trigger this
		 */
		public final int mask;

		private MetaInput(int keyCode, int mask) {
			this.keyCode = keyCode;
			this.mask = mask;
		}
	}

	private GameController listener;

	private JFileChooser fileChooser;

	private JMenuItem showHintItem;
	private JMenuItem showSolutionItem;
	private JMenuItem pauseItem;
	private JMenuItem frameAdvanceItem;

	/**
	 * Creates a new {@code MenuBar} to handle {@code MetaInput}s. Inputs are
	 * sent to {@code listener}.
	 * 
	 * @param listener {@code GameController} which will process inputs
	 */
	public MenuBar(GameController listener) {
		this.listener = listener;

		fileChooser = new JFileChooser();  // global file chooser so it
											  // remembers which directory the
											  // user was in if they open it
											  // multiple times
		fileChooser.setFileFilter(
				new FileNameExtensionFilter("Recording Files (.rec)", "rec"));

		add(createHintMenu());
		add(createRecordingMenu());
		add(createPauseRestartMenu());
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

	/**
	 * Creates a {@code JMenuItem} with the specified properties. {@code action}
	 * will be run when the item is selected or the shortcut from
	 * {@code metaInput} is pressed.
	 * 
	 * @param text       to display on the menu item
	 * @param keyCode    key code which will activate it, or 0 if no shortcut is
	 *                   to be set
	 * @param metaInput  {@code MetaInput} to pull keyboard shortcut information
	 *                   from
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

		menuItem.addActionListener(e -> action.run());
		if (metaInput.keyCode != 0) {
			menuItem.setAccelerator(
					KeyStroke.getKeyStroke(metaInput.keyCode, metaInput.mask));
		}

		return menuItem;
	}

	private File promptFileSaveLocation() {

		int fileChooserResult;
		File saveFile;

		boolean canSave;
		do {
			canSave = true;

			fileChooser.setSelectedFile(new File("Untitled.rec"));

			fileChooserResult = fileChooser.showSaveDialog(null);
			saveFile = fileChooser.getSelectedFile();

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
		int result = fileChooser.showOpenDialog(null);
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

}
