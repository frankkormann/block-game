package game;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.UIManager;

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

	private MainFrame mainFrame;
	private PhysicsSimulator physicsSimulator;
	private InputHandler inputHandler;

	private URL currentLevel;

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

		running = true;

//		inputHandler.beginReading(getClass().getResource(RECORDING));
//		inputHandler.beginWriting(getClass().getResource(RECORDING));
	}

	public void startGame() {
		loadLevel(getClass().getResource(FIRST_LEVEL));
		new Timer().scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if (running) {
					advanceFrame();
				}
			}
		}, 0, 20);
	}

	private void loadLevel(URL url) {
		physicsSimulator = new PhysicsSimulator();

		try {
			ObjectMapper mapper = new ObjectMapper();
			Level level = mapper.readValue(url, Level.class);
			mainFrame.setUpLevel(level);
			for (Rectangle rect : level.getRectangles()) {
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

		mainFrame.pack();
		physicsSimulator.createSides(mainFrame.getNextWidth(),
				mainFrame.getNextHeight(), mainFrame.getNextXOffset(),
				mainFrame.getNextYOffset());

		mainFrame.moveToMiddleOfScreen();
	}

	private void addRectangleToGame(Rectangle rectangle) {
		physicsSimulator.add(rectangle);
		mainFrame.add(rectangle);
	}

	private void advanceFrame() {
		mainFrame.resizeAll(inputHandler.getResizes());

		physicsSimulator.updateAndMoveObjects(inputHandler.getKeysPressed(),
				mainFrame.getNextWidth(), mainFrame.getNextHeight(),
				mainFrame.getNextXOffset(), mainFrame.getNextYOffset());
		if (physicsSimulator.getNextLevel() != null) {
			loadLevel(physicsSimulator.getNextLevel());
			return;
		}

		mainFrame.resizeAll(physicsSimulator.getResizes());
		mainFrame.incorporateChanges();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_S:
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
			case KeyEvent.VK_Y:
				loadLevel(currentLevel);
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
