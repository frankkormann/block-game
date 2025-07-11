package game;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;
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

	private static final String FIRST_LEVEL = "/level_1.json";

	private static final int MILLISECONDS_BETWEEN_FRAMES = 20;

	private MainFrame mainFrame;
	private PhysicsSimulator physicsSimulator;
	private GameInputHandler gameInputHandler;
	private MetaInputHandler metaInputHandler;

	private String currentLevel;

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
		loadLevel(FIRST_LEVEL);
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

	private void loadLevel(String levelResource) {

		Level level = null;

		try {
			ObjectMapper mapper = new ObjectMapper();
			level = mapper.readValue(getClass().getResourceAsStream(levelResource),
					Level.class);
		}
		catch (Exception e) {
			physicsSimulator.resetNextlevel();
			JOptionPane.showMessageDialog(mainFrame, "Could not load level\n" + e,
					"Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();

			return;
		}

		physicsSimulator = new PhysicsSimulator();
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
			for (Area attached : wall.getAttachments()) {
				physicsSimulator.addArea(attached);
				mainFrame.add(attached, 0);
			}
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

		currentLevel = levelResource;

		mainFrame.arrangeComponents();
		physicsSimulator.createSides(mainFrame.getNextWidth(),
				mainFrame.getNextHeight(), mainFrame.getNextXOffset(),
				mainFrame.getNextYOffset());

		mainFrame.moveToMiddleOfScreen();

		paused = false;
		beginTempRecording();

	}

	public void nextFrame() {
		Pair<Map<MainFrame.Direction, Integer>, Set<GameInputHandler.GameInput>> allInputs;
		allInputs = gameInputHandler.poll();

		mainFrame.resizeAll(allInputs.first);

		physicsSimulator.updateAndMoveObjects(allInputs.second,
				mainFrame.getNextWidth(), mainFrame.getNextHeight(),
				mainFrame.getNextXOffset(), mainFrame.getNextYOffset());

		if (physicsSimulator.getNextLevel() != "") {
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
		recording = null;  // In case a new file cannot be created, still stop writing
							  // to this one
		try {
			recording = File.createTempFile("blockgame", null);
			gameInputHandler.beginWriting(Files.newOutputStream(recording.toPath()));
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
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public void startPlayback(File file) throws IOException {
		gameInputHandler.beginReading(Files.newInputStream(file.toPath()));
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

	public void saveRecording(File destination) throws IOException {
		if (recording == null) {
			return;
		}
		gameInputHandler.flushWriter();
		Files.copy(recording.toPath(), destination.toPath(),
				StandardCopyOption.REPLACE_EXISTING);
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
