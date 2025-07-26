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

import game.MetaInputHandler.MetaInput;

public class MenuBar extends JMenuBar {

	private GameController listener;

	private JFileChooser fileChooser;

	private JMenuItem showHintItem;
	private JMenuItem showSolutionItem;
	private JMenuItem pauseItem;

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

	public void reset() {
		showHintItem.setSelected(false);
		showSolutionItem.setEnabled(false);
		pauseItem.setSelected(false);
	}

	private JMenu createHintMenu() {
		JMenu menu = new JMenu("Hint");

		showHintItem = createMenuItem("Show hint", KeyEvent.VK_H, 0,
				this::hintsAction, true);

		showSolutionItem = createMenuItem("Show solution", KeyEvent.VK_H,
				KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK,
				this::showSolutionAction, false);
		showSolutionItem.setEnabled(false);

		menu.add(showHintItem);
		menu.add(showSolutionItem);

		return menu;
	}

	private JMenu createRecordingMenu() {
		JMenu menu = new JMenu("Recordings");

		JMenuItem openItem = createMenuItem("Open", KeyEvent.VK_P,
				KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK,
				this::openRecordingAction, false);

		JMenuItem saveItem = createMenuItem("Save current", KeyEvent.VK_S,
				KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK,
				this::saveRecordingAction, false);

		JMenuItem stopItem = createMenuItem("Stop", KeyEvent.VK_S, 0,
				this::stopRecordingAction, false);

		menu.add(openItem);
		menu.add(saveItem);
		menu.add(stopItem);

		return menu;
	}

	private JMenu createPauseRestartMenu() {
		JMenu menu = new JMenu("Pause/Retry");

		pauseItem = createMenuItem("Pause", KeyEvent.VK_P, 0, this::pauseAction,
				true);

		JMenuItem restartItem = createMenuItem("Retry", KeyEvent.VK_R, 0,
				this::restartAction, false);

		menu.add(pauseItem);
		menu.add(restartItem);

		return menu;
	}

	/**
	 * Creates a {@code JMenuItem} with the specified properties. {@code toDo}
	 * will be run when the item is selected or {@code keyCode} is pressed.
	 * 
	 * @param text       to display on the menu item
	 * @param keyCode    key code which will activate it, or 0 if no shortcut is
	 *                   to be set
	 * @param modifiers  modifier mask to the key code
	 * @param action     action to perform when selected
	 * @param isCheckBox whether to use a {@code JCheckBoxMenuItem} or not
	 * 
	 * @return the {@code JMenuItem}
	 */
	private JMenuItem createMenuItem(String text, int keyCode, int modifiers,
			Runnable action, boolean isCheckBox) {

		JMenuItem menuItem;
		if (isCheckBox) {
			menuItem = new JCheckBoxMenuItem(text);
		}
		else {
			menuItem = new JMenuItem(text);
		}

		menuItem.addActionListener(e -> action.run());
		if (keyCode != 0) {
			menuItem.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers));
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
	}

	private void restartAction() {
		listener.processMetaInput(MetaInput.RELOAD_LEVEL);
	}

}
