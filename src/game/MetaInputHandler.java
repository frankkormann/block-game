package game;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MetaInputHandler extends KeyAdapter {

	private GameController actionListener;
	private List<HintRectangle> hints;

	private JFileChooser fileChooser;

	public MetaInputHandler(GameController actionListener) {
		this.actionListener = actionListener;
		hints = new ArrayList<>();

		fileChooser = new JFileChooser();  // global file chooser so it remembers which
											  // directory the user was in if they open
											  // it multiple times
		fileChooser.setFileFilter(
				new FileNameExtensionFilter("Recording Files (.rec)", "rec"));
	}

	@Override
	public void keyPressed(KeyEvent e) {

		int shiftCtrlMask = KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK;

		switch (e.getKeyCode()) {
			case KeyEvent.VK_E:
				actionListener.endPlayback();
				break;
			case KeyEvent.VK_K:
				actionListener.setPaused(!actionListener.isPaused());
				break;
			case KeyEvent.VK_L:
				if (actionListener.isPaused()) {
					actionListener.nextFrame();
				}
				break;
			case KeyEvent.VK_R:
				actionListener.reloadLevel();
				break;
			case KeyEvent.VK_S:
				if ((e.getModifiersEx() & shiftCtrlMask) == shiftCtrlMask) {
					boolean wasPaused = actionListener.isPaused();
					actionListener.setPaused(true);
					File saveFile = promptFileSaveLocation();
					if (saveFile != null) {
						actionListener.saveRecording(saveFile);
					}
					actionListener.setPaused(wasPaused);
				}
				break;
			case KeyEvent.VK_P:
				if ((e.getModifiersEx() & shiftCtrlMask) == shiftCtrlMask) {
					boolean wasPaused = actionListener.isPaused();
					actionListener.setPaused(true);
					File openFile = promptFileOpenLocation();
					if (openFile != null) {
						actionListener.startPlayback(openFile);
					}
					actionListener.setPaused(wasPaused);
				}
				break;
			case KeyEvent.VK_H:
				hints.forEach(h -> h.toggleVisible());
				break;
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
