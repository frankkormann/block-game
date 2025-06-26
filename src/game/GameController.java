package game;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * Coordinates {@code MainFrame}, {@code PhysicsSimulator}, and
 * {@code InputHandler}.
 * <p>
 * Level data is read from JSON files. See the README for more details.
 * <p>
 * While all in-game controls are handled by {@code InputHandler}, this handles
 * meta-controls. Specifically, this listens to keyboard input to pause the
 * game, advance one frame while paused, restart the current level, and stop
 * reading from an input recording file.
 * 
 * @author Frank Kormann
 */
public class GameController implements KeyListener, WindowListener {

	public static final String FIRST_LEVEL = "/level_1.json";
	public static final String RECORDING = "/test.rec";

	public static final int MILLISECONDS_BETWEEN_FRAMES = 20;

	private MainFrame mainFrame;
	private PhysicsSimulator physicsSimulator;
	private InputHandler inputHandler;

	private URL currentLevel;

	private File recording;
	private JFileChooser fileChooser;

	private boolean running;

	public static void main(String[] args) {
		FlatLightLaf.setup();
		UIManager.put("TitlePane.buttonSize", new Dimension(0, 0));
		new GameController().startGame();
	}

	public GameController() {
		inputHandler = new InputHandler();
		// physicsSimulator is instantiated when the first level is loaded
		mainFrame = new MainFrame(inputHandler);
		mainFrame.addKeyListener(this);

		mainFrame.addWindowListener(this);

		fileChooser = new JFileChooser();  // global file chooser so it remembers which
											  // directory the user was in if they open
											  // it multiple times
		fileChooser.setFileFilter(
				new FileNameExtensionFilter("Recording Files (.rec)", "rec"));

		running = true;
	}

	public void startGame() {
		loadLevel(getClass().getResource(FIRST_LEVEL));
		mainFrame.setVisible(true);
		new Timer().scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if (running) {
					advanceFrame();
				}
			}
		}, 0, MILLISECONDS_BETWEEN_FRAMES);
	}

	private void loadLevel(URL url) {
		physicsSimulator = new PhysicsSimulator();

		try {
			ObjectMapper mapper = new ObjectMapper();
			Level level = mapper.readValue(url, Level.class);
			mainFrame.setUpLevel(level);

			for (Rectangle rect : level.rectangles) {
				addRectangleToGame(rect);
				for (Area attached : rect.getAttachments()) {
					addRectangleToGame(attached);
				}
			}

			currentLevel = url;
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		mainFrame.arrangeComponents();
		physicsSimulator.createSides(mainFrame.getNextWidth(),
				mainFrame.getNextHeight(), mainFrame.getNextXOffset(),
				mainFrame.getNextYOffset());

		mainFrame.moveToMiddleOfScreen();

		beginTempRecording();
	}

	private void addRectangleToGame(Rectangle rectangle) {
		physicsSimulator.add(rectangle);
		mainFrame.add(rectangle);
	}

	private void advanceFrame() {
		mainFrame.resizeAll(inputHandler.getResizes());

		physicsSimulator.updateAndMoveObjects(inputHandler.getInputs(),
				mainFrame.getNextWidth(), mainFrame.getNextHeight(),
				mainFrame.getNextXOffset(), mainFrame.getNextYOffset());
		if (physicsSimulator.getNextLevel() != null) {
			loadLevel(physicsSimulator.getNextLevel());
			return;
		}

		mainFrame.resizeAll(physicsSimulator.getResizes());
		mainFrame.incorporateChanges();
	}

	/**
	 * Begins writing input to a new temp file.
	 */
	private void beginTempRecording() {
		inputHandler.endWriting();
		try {
			recording = File.createTempFile("blockgame", null);
			inputHandler.beginWriting(recording.toURL());
			recording.deleteOnExit();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Opens a file chooser dialogue prompt to save a recording of the current
	 * level.
	 */
	private void saveRecording() {

		int fileChooserResult;
		File saveFile;

		boolean canSave;
		do {
			canSave = true;

			fileChooser.setSelectedFile(new File("Untitled.rec"));

			fileChooserResult = fileChooser.showSaveDialog(mainFrame);
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

		if (fileChooserResult == JFileChooser.APPROVE_OPTION && saveFile != null
				&& recording != null) {

			try {
				inputHandler.flushWriter();
				Files.copy(recording.toPath(), saveFile.toPath(),
						StandardCopyOption.REPLACE_EXISTING);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Opens a file chooser dialogue prompt to start playback of a recording.
	 */
	private void startPlayback() {
		int result = fileChooser.showOpenDialog(mainFrame);
		File openFile = fileChooser.getSelectedFile();

		if (result == JFileChooser.APPROVE_OPTION && openFile != null) {
			try {
				inputHandler.beginReading(openFile.toURI().toURL());
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {

		int shiftCtrlMask = KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK;

		switch (e.getKeyCode()) {
			case KeyEvent.VK_E:
				inputHandler.endReading();
				break;
			case KeyEvent.VK_K:
				running = !running;
				break;
			case KeyEvent.VK_L:
				if (!running) {
					advanceFrame();
				}
				break;
			case KeyEvent.VK_R:
				loadLevel(currentLevel);
				break;
			case KeyEvent.VK_S:
				if ((e.getModifiersEx() & shiftCtrlMask) == shiftCtrlMask) {
					running = false;
					saveRecording();
					running = true;
				}
				break;
			case KeyEvent.VK_P:
				if ((e.getModifiersEx() & shiftCtrlMask) == shiftCtrlMask) {
					running = false;
					startPlayback();
					running = true;
				}
				break;
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		inputHandler.endReading();
		inputHandler.endWriting();
		System.exit(0);
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

}
