package game;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Handles inputs that are not directly related to playing the game.
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

	private GameController actionListener;
	private List<HintRectangle> hints;
	private String solutionResource;

	private JFileChooser fileChooser;

	/**
	 * Creates a new {@code MetaInputHandler} which affects {@code actionListener}.
	 * 
	 * @param actionListener {@code GameController} to receive events
	 */
	public MetaInputHandler(GameController actionListener) {
		this.actionListener = actionListener;
		hints = new ArrayList<>();
		solutionResource = "";

		fileChooser = new JFileChooser();  // global file chooser so it remembers which
											  // directory the user was in if they open
											  // it multiple times
		fileChooser.setFileFilter(
				new FileNameExtensionFilter("Recording Files (.rec)", "rec"));
	}

	/**
	 * Registers {@code hint} with this. {@code HintRectangle}s which are registered
	 * will be toggled visible or not visible.
	 * 
	 * @param hint {@code HintRectangle} to add
	 */
	public void addHint(HintRectangle hint) {
		hints.add(hint);
	}

	/**
	 * Sets the solution recording to play. {@code solutionResource} must be able to
	 * be found by {@link java.lang.Class#getResourceAsStream(String)}.
	 * 
	 * @param solutionResource name of resource
	 */
	public void setSolution(String solutionResource) {
		this.solutionResource = solutionResource;
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
				if (actionListener.isPaused()) {
					actionListener.nextFrame();
				}
				break;
			case PAUSE:
				actionListener.setPaused(!actionListener.isPaused());
				break;
			case PLAY_RECORDING: {
				playRecording();
				break;
			}
			case RELOAD_LEVEL:
				actionListener.reloadLevel();
				break;
			case SAVE_RECORDING: {
				saveRecording();
				break;
			}
			case STOP_RECORDING:
				actionListener.endPlayback();
				break;
			case TOGGLE_HINTS:
				hints.forEach(h -> h.toggleVisible());
				break;
			case PLAY_SOLUTION:
				if (solutionResource != "") {
					actionListener.reloadLevel();
					actionListener.startPlayback(solutionResource);
				}
				break;
		}
	}

	private void playRecording() {
		boolean wasPaused = actionListener.isPaused();
		actionListener.setPaused(true);
		File openFile = promptFileOpenLocation();
		if (openFile != null) {
			try {
				actionListener.startPlayback(openFile);
			}
			catch (IOException e) {
				e.printStackTrace();
				new ErrorDialog("Error", "Could not load file '" + openFile + "'", e)
						.setVisible(true);
			}
		}
		actionListener.setPaused(wasPaused);
	}

	private void saveRecording() {
		boolean wasPaused = actionListener.isPaused();
		actionListener.setPaused(true);
		File saveFile = promptFileSaveLocation();
		if (saveFile != null) {
			try {
				actionListener.saveRecording(saveFile);
			}
			catch (IOException e) {
				e.printStackTrace();
				new ErrorDialog("Error", "Could not save file '" + saveFile + "'", e)
						.setVisible(true);
			}
		}
		actionListener.setPaused(wasPaused);
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
