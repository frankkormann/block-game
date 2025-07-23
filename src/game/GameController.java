package game;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatLightLaf;

import game.GameInputHandler.GameInput;
import game.MainFrame.Direction;
import game.MetaInputHandler.MetaInput;

/**
 * Coordinates {@code MainFrame}, {@code PhysicsSimulator}, and
 * {@code InputHandler}.
 * <p>
 * Level data is read from JSON files. The JSON is used to fill the fields in
 * {@link Level}, so it should have data for each of that class's public
 * attributes.
 * <p>
 * If there are arguments passed to {@code main}, the first argument will be
 * used as the level number of the first level. Otherwise, a default first level
 * will be used.
 * 
 * @author Frank Kormann
 */
public class GameController extends WindowAdapter {

	private static final String FIRST_LEVEL = "/level_1.json";

	private static final int MILLISECONDS_BETWEEN_FRAMES = 19;

	private MainFrame mainFrame;
	private PhysicsSimulator physicsSimulator;
	private GameInputHandler gameInputHandler;
	private MetaInputHandler metaInputHandler;

	private String currentLevel;
	private String currentSolution;
	private List<HintRectangle> hints;

	private File recording;

	private boolean paused;

	public static void main(String[] args) {
		FlatLightLaf.setup();
		new GameController().startGame(
				args.length == 0 ? FIRST_LEVEL : "/level_" + args[0] + ".json");
	}

	/**
	 * Creates a {@code GameController} and populates it with all classes it
	 * depends on.
	 */
	public GameController() {
		gameInputHandler = new GameInputHandler();
		metaInputHandler = new MetaInputHandler(this);
		// physicsSimulator is instantiated when the first level is loaded
		mainFrame = new MainFrame(gameInputHandler);

		currentLevel = "";
		currentSolution = "";
		hints = new ArrayList<>();

		mainFrame.addWindowListener(this);
		mainFrame.addKeyListener(metaInputHandler);

		paused = false;
	}

	/**
	 * Loads {@code firstLevel} and begins a repeating {@code TimerTask} to
	 * process each frame.
	 * 
	 * @param firstLevel resource name of first level to load
	 */
	public void startGame(String firstLevel) {
		loadLevel(firstLevel);
		mainFrame.setVisible(true);

		new Timer().schedule(new TimerTask() {
			public void run() {
				try {
					if (!paused && mainFrame.isFocused()) {
						nextFrame();
					}
					mainFrame.repaint();
				}
				catch (Exception e) {
					e.printStackTrace();
					new ErrorDialog("Error", "Something went wrong", e)
							.setVisible(true);
				}
			}
		}, 0, MILLISECONDS_BETWEEN_FRAMES);
	}

	/**
	 * Restarts and reloads the current level, and stops reading from the
	 * recording file if there is one.
	 */
	private void reloadLevel() {
		gameInputHandler.endReading();
		loadLevel(currentLevel);
	}

	private void loadLevel(String levelResource) {

		paused = true;

		Level level = null;

		try {
			ObjectMapper mapper = new ObjectMapper();
			level = mapper.readValue(
					getClass().getResourceAsStream(levelResource), Level.class);
		}
		catch (Exception e) {
			e.printStackTrace();
			new ErrorDialog("Error",
					"Could not load level '" + levelResource
							+ "', file probably does not exist",
					e).setVisible(true);

			if (mainFrame.isVisible()) {
				physicsSimulator.resetNextlevel();
			}
			else {
				System.exit(1);
			}

			paused = false;

			return;
		}

		physicsSimulator = new PhysicsSimulator();
		mainFrame.setUpLevel(level);
		hints.clear();

		currentSolution = level.solution;
		currentLevel = levelResource;
		loadObjectsFromLevel(level);

		physicsSimulator.createSides(mainFrame.getNextWidth(),
				mainFrame.getNextHeight(), mainFrame.getNextXOffset(),
				mainFrame.getNextYOffset());

		mainFrame.moveToMiddleOfScreen();

		beginTempRecording();
		paused = false;

	}

	/**
	 * Adds all {@code MovingRectangle}s, {@code WallRectangle}s, {@code Area}s,
	 * {@code GoalArea}s, and {@code HintRectangle}s from {@code level} to
	 * {@code physicsPanel}, {@code mainFrame}, and/or {@code hints}.
	 * 
	 * @param level {@code Level} to take objects from
	 */
	private void loadObjectsFromLevel(Level level) {

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
			hints.add(hint);
		}

	}

	/**
	 * Processes the next frame.
	 * <p>
	 * Performs these steps in order:
	 * <ul>
	 * <li>Polls {@code GameInputHandler} for inputs
	 * <li>Resizes {@code MainFrame} according to inputs
	 * <li>Updates the game state through {@code PhysicsSimulator}
	 * <li>Loads the next level if necessary
	 * <li>Resizes {@code MainFrame} according to {@code PhysicsSimulator}
	 * <li>Redraws {@code MainFrame}
	 * </ul>
	 */
	public void nextFrame() {
		Pair<Map<Direction, Integer>, Set<GameInput>> allInputs = gameInputHandler
				.poll();

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
		recording = null;  // In case a new file cannot be created, still stop
							  // writing to this one
		try {
			recording = File.createTempFile("blockgame", null);
			gameInputHandler
					.beginWriting(Files.newOutputStream(recording.toPath()));
			recording.deleteOnExit();
		}
		catch (IOException e) {
			e.printStackTrace();
			// Don't pop up an ErrorDialog because the user probably doesn't
			// care, and it would pop up on every level
		}
	}

	/**
	 * Processes a {@code MetaInput} by taking the action expected of it.
	 * <p>
	 * The {@code MetaInput} is expected to not require any extra data to
	 * process. {@code MetaInput}s which require a {@code File} should be passed
	 * to {@link #processMetaInput(MetaInput, File)} instead.
	 * 
	 * @param input {@code MetaInput} to process
	 * 
	 * @throws IllegalArgumentException if {@code input} requires extra data or
	 *                                  is invalid
	 * 
	 * @see #processMetaInput(MetaInput, File)
	 */
	public void processMetaInput(MetaInput input)
			throws IllegalArgumentException {
		switch (input) {
			case PAUSE:
				paused = !paused;
				break;
			case FRAME_ADVANCE:
				if (paused) {
					nextFrame();
				}
				break;
			case RELOAD_LEVEL:
				reloadLevel();
				break;
			case TOGGLE_HINTS:
				hints.forEach(h -> h.toggleVisible());
				break;
			case PLAY_SOLUTION:
				playSolution();
				break;
			case STOP_RECORDING:
				gameInputHandler.endReading();
				break;
			default:
				throw new IllegalArgumentException("Invalid MetaInput");
		}
	}

	private void playSolution() {
		if (currentSolution == "") {
			JOptionPane.showMessageDialog(mainFrame,
					"Current level has no solution file", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		InputStream solutionStream = getClass()
				.getResourceAsStream(currentSolution);

		if (solutionStream == null) {
			JOptionPane.showMessageDialog(mainFrame,
					"Couldn't open solution '" + currentSolution + "'", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		reloadLevel();
		JOptionPane.showMessageDialog(mainFrame,
				"Press S to stop playback at any time.", "Seen Enough?",
				JOptionPane.INFORMATION_MESSAGE);

		gameInputHandler
				.beginReading(getClass().getResourceAsStream(currentSolution));
	}

	/**
	 * Processes a {@code MetaInput} which requires a {@code File} by taking the
	 * action expected of it.
	 * <p>
	 * The {@code MetaInput} must require a {@code File} to be processed. If it
	 * does not, it should be passed to {@link #processMetaInput(MetaInput)}
	 * instead.
	 * 
	 * @param input {@code MetaInput} to process
	 * @param file  {@code File} which is involved
	 * 
	 * @throws IllegalArgumentException if {@code input} does not require a
	 *                                  {@code File} or is invalid
	 * 
	 * @see #processMetaInput(MetaInput)
	 */
	public void processMetaInput(MetaInput input, File file)
			throws IllegalArgumentException {

		String errorWord = "";

		try {
			switch (input) {
				case SAVE_RECORDING:
					errorWord = "save";
					gameInputHandler.flushWriter();
					Files.copy(recording.toPath(), file.toPath(),
							StandardCopyOption.REPLACE_EXISTING);
					break;
				case PLAY_RECORDING:
					errorWord = "open";
					gameInputHandler
							.beginReading(Files.newInputStream(file.toPath()));
					break;
				default:
					throw new IllegalArgumentException("Invalid MetaInput");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error",
					"Could not " + errorWord + " file '" + file + "'", e)
					.setVisible(true);
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		gameInputHandler.endReading();
		gameInputHandler.endWriting();
		System.exit(0);
	}

}
