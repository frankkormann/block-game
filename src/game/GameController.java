package game;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.UIManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * Coordinates {@code MainFrame}, {@code PhysicsSimulator}, and
 * {@code InputHandler}.
 * <p>
 * Level data is read from JSON files. The JSON is used to fill the fields in
 * {@link Level}, so it should have data for each of that class's public
 * attributes.
 * 
 * @author Frank Kormann
 */
public class GameController extends WindowAdapter {

	public static final String FIRST_LEVEL = "/level_1.json";

	public static final int MILLISECONDS_BETWEEN_FRAMES = 20;

	private MainFrame mainFrame;
	private PhysicsSimulator physicsSimulator;
	private GameInputHandler gameInputHandler;
	private MetaInputHandler metaInputHandler;

	private URL currentLevel;

	private File recording;

	private boolean paused;

	public static void main(String[] args) {
		FlatLightLaf.setup();
		UIManager.put("TitlePane.buttonSize", new Dimension(0, 0));
		new GameController().startGame();
	}

	public GameController() {
		gameInputHandler = new GameInputHandler();
		metaInputHandler = new MetaInputHandler(this);
		// physicsSimulator is instantiated when the first level is loaded
		mainFrame = new MainFrame(gameInputHandler);

		mainFrame.addWindowListener(this);
		mainFrame.addKeyListener(metaInputHandler);

		paused = false;
	}

	public void startGame() {
		loadLevel(getClass().getResource(FIRST_LEVEL));
		mainFrame.setVisible(true);
		new Timer().scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if (!paused) {
					nextFrame();
				}
			}
		}, 0, MILLISECONDS_BETWEEN_FRAMES);
	}

	public void reloadLevel() {
		gameInputHandler.endReading();
		loadLevel(currentLevel);
	}

	private void loadLevel(URL url) {
		physicsSimulator = new PhysicsSimulator();
		paused = false;

		try {
			ObjectMapper mapper = new ObjectMapper();
			Level level = mapper.readValue(url, Level.class);
			mainFrame.setUpLevel(level);

			metaInputHandler.setSolution(level.solution);

			for (MovingRectangle rect : level.movingRectangles) {
				physicsSimulator.addMovingRectangle(rect);
				mainFrame.add(rect, 1);
				for (Area attached : rect.getAttachments()) {
					physicsSimulator.addArea(attached);
					mainFrame.add(attached, 0);
				}
			}
			for (WallRectangle wall : level.walls) {
				physicsSimulator.addWall(wall);
				mainFrame.add(wall, 2);
			}
			for (Area area : level.areas) {
				physicsSimulator.addArea(area);
				mainFrame.add(area, 0);
			}
			for (GoalArea goal : level.goals) {
				physicsSimulator.addGoalArea(goal);
				mainFrame.add(goal, 0);
			}
			for (HintRectangle hint : level.hints) {
				mainFrame.add(hint, 3);
				metaInputHandler.addHint(hint);
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

	public void nextFrame() {
		mainFrame.resizeAll(gameInputHandler.getResizes());

		physicsSimulator.updateAndMoveObjects(gameInputHandler.getInputs(),
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
		gameInputHandler.endWriting();
		try {
			recording = File.createTempFile("blockgame", null);
			gameInputHandler.beginWriting(recording);
			recording.deleteOnExit();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Replay a recording {@code File} {@code file}.
	 * 
	 * @param file {@code File} to replay
	 */
	public void startPlayback(File file) {
		try {
			gameInputHandler.beginReading(Files.newInputStream(file.toPath()));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Replay a recording stored as a resource. The resource must be able to be
	 * found by {@code java.lang.Class.getResourceAsStream(resource)}.
	 * 
	 * @param resource name of resource
	 */
	public void startPlayback(String resource) {
		gameInputHandler.beginReading(getClass().getResourceAsStream(resource));
	}

	public void endPlayback() {
		gameInputHandler.endReading();
	}

	public void saveRecording(File destination) {
		if (recording == null) {
			return;
		}
		try {
			gameInputHandler.flushWriter();
			Files.copy(recording.toPath(), destination.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public boolean isPaused() {
		return paused;
	}

	@Override
	public void windowClosing(WindowEvent e) {
		gameInputHandler.endReading();
		gameInputHandler.endWriting();
		System.exit(0);
	}

}
