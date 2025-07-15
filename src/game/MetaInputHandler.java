package game;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Reports player input which is not directly related to playing the game, for
 * example pausing or reloading the level.
 * <p>
 * If an input requires multiple pieces to be acted on, this will interrupt the
 * game to get the extra information. For instance, loading a file requires
 * asking the player which file to load.
 * 
 * @author Frank Kormann
 */
public class MetaInputHandler extends KeyAdapter {

	public enum MetaInput {
		PAUSE(KeyEvent.VK_K, 0), FRAME_ADVANCE(KeyEvent.VK_L, 0),
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

	/**
	 * Creates a new {@code MetaInputHandler} which passes events to
	 * {@code listener}.
	 * 
	 * @param listener {@code GameController} to receive events
	 */
	public MetaInputHandler(GameController listener) {
		this.listener = listener;

		fileChooser = new JFileChooser();  // global file chooser so it remembers which
											  // directory the user was in if they open
											  // it multiple times
		fileChooser.setFileFilter(
				new FileNameExtensionFilter("Recording Files (.rec)", "rec"));
	}

	@Override
	public void keyPressed(KeyEvent e) {
		for (MetaInput inp : MetaInput.values()) {
			if (e.getKeyCode() == inp.keyCode && (e.getModifiersEx() ^ inp.mask) == 0) {
				handleInput(inp);
				break;
			}
		}
	}

	private void handleInput(MetaInput input) {
		switch (input) {
			case FRAME_ADVANCE:
			case PAUSE:
			case RELOAD_LEVEL:
			case STOP_RECORDING:
			case TOGGLE_HINTS:
			case PLAY_SOLUTION:
				listener.processMetaInput(input);
				break;
			case SAVE_RECORDING: {
				File saveFile = promptFileSaveLocation();
				if (saveFile == null) {
					return;
				}
				listener.processMetaInput(input, saveFile);
				break;
			}
			case PLAY_RECORDING: {
				File openFile = promptFileOpenLocation();
				if (openFile == null) {
					return;
				}
				listener.processMetaInput(input, openFile);
				break;
			}
		}
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

			if (fileChooserResult == JFileChooser.APPROVE_OPTION && saveFile.exists()) {
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

}
