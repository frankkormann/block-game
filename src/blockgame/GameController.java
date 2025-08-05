package blockgame;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.SystemInfo;

import blockgame.gui.ErrorDialog;
import blockgame.gui.HintRectangle;
import blockgame.gui.MainFrame;
import blockgame.gui.MenuBar;
import blockgame.gui.MainFrame.Direction;
import blockgame.gui.MenuBar.MetaInput;
import blockgame.input.ColorMapper;
import blockgame.input.GameInputHandler;
import blockgame.input.InputMapper;
import blockgame.input.ParameterMapper;
import blockgame.input.ValueChangeListener;
import blockgame.input.GameInputHandler.MovementInput;
import blockgame.input.ParameterMapper.Parameter;
import blockgame.physics.Area;
import blockgame.physics.GoalArea;
import blockgame.physics.MovingRectangle;
import blockgame.physics.PhysicsSimulator;
import blockgame.physics.Rectangle;
import blockgame.physics.SwitchArea;
import blockgame.physics.SwitchRectangle;
import blockgame.physics.WallRectangle;
import blockgame.util.Pair;
import blockgame.util.SaveManager;

/**
 * Coordinates {@code MainFrame}, {@code PhysicsSimulator}, and
 * {@code InputHandler}.
 * <p>
 * Level data is read from JSON files. The JSON is used to fill the fields in
 * {@link Level}, so it should have data for each of that class's public
 * attributes.
 * <p>
 * If the environment variable stored in {@link #DIRECTORY_ENV_VAR} is set, it
 * will be used as the path to put save files. Otherwise, a default directory is
 * used.
 * 
 * @author Frank Kormann
 */
public class GameController extends WindowAdapter
		implements ValueChangeListener {

	public static final String DIRECTORY_ENV_VAR = "BLOCKGAME_DIRECTORY";

	private static final String FIRST_LEVEL = "/level_1.json";

	private MainFrame mainFrame;
	private PhysicsSimulator physicsSimulator;
	private GameInputHandler gameInputHandler;
	private MenuBar menuBar;

	private String currentLevel;
	private String currentSolution;
	private List<HintRectangle> hints;

	private ByteArrayOutputStream currentLevelOutputStream;
	private TimerTask newFrameTask;

	private boolean paused;

	public static void main(String[] args) {
		FlatLightLaf.setup();
		UIManager.put("TitlePane.embeddedForeground",
				UIManager.get("TitlePane.foreground"));
		SaveManager.setDirectory(System.getenv(DIRECTORY_ENV_VAR));
		// Stolen from https://www.formdev.com/flatlaf/window-decorations/
		if (SystemInfo.isLinux) {
			// enable custom window decorations
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
		}

		new GameController();
	}

	/**
	 * Creates a {@code GameController} and starts the game.
	 */
	public GameController() {
		InputMapper inputMapper = new InputMapper();
		ColorMapper colorMapper = new ColorMapper();
		ParameterMapper paramMapper = new ParameterMapper();

		Rectangle.setColorMapper(colorMapper);
		Rectangle.setParameterMapper(paramMapper);

		gameInputHandler = new GameInputHandler(inputMapper, paramMapper);
		// physicsSimulator is instantiated when the first level is loaded
		mainFrame = new MainFrame(gameInputHandler, paramMapper);
		menuBar = new MenuBar(inputMapper, colorMapper, paramMapper, this);

		currentLevel = "";
		currentSolution = "";
		currentLevelOutputStream = null;
		hints = new ArrayList<>();

		mainFrame.addWindowListener(this);
		mainFrame.setJMenuBar(menuBar);

		paused = false;

		paramMapper.addListener(this);

		startGame(SaveManager.getValue("CURRENT_LEVEL", FIRST_LEVEL),
				paramMapper.getInt(Parameter.GAME_SPEED));
	}

	/**
	 * Loads {@code firstLevel} and begins a repeating {@code TimerTask} to
	 * process each frame.
	 * 
	 * @param firstLevel          resource name of first level to load
	 * @param millisBetweenFrames number of milliseconds between each frame
	 */
	public void startGame(String firstLevel, int millisBetweenFrames) {
		loadLevel(firstLevel);
		mainFrame.setVisible(true);

		if (SaveManager.getValue("NEW_SAVE", "true").equals("true")) {
			JOptionPane.showMessageDialog(mainFrame,
					"You can change colors and controls in the Options menu at any time.",
					"Message", JOptionPane.INFORMATION_MESSAGE);
			SaveManager.putValue("NEW_SAVE", "false");
		}

		newFrameTask = new TimerTask() {
			public void run() {
				newFrameTaskAction();
			}
		};
		new Timer().scheduleAtFixedRate(newFrameTask, 0, millisBetweenFrames);
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
		Level level;

		try {
			ObjectMapper mapper = new ObjectMapper();
			level = mapper.readValue(
					getClass().getResourceAsStream(levelResource), Level.class);
		}
		catch (Exception e) {
			e.printStackTrace();
			new ErrorDialog("Error",
					"Could not load level '" + levelResource
							+ "', file is corrupt or does not exist",
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
		menuBar.reset();
		hints.clear();

		currentSolution = level.solution;
		currentLevel = levelResource;
		loadObjectsFromLevel(level);
		SaveManager.putValue("CURRENT_LEVEL", levelResource);

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

		Map<SwitchRectangle, String> switchRects = new HashMap<>();

		for (MovingRectangle rect : level.movingRectangles) {
			if (rect instanceof SwitchRectangle) {
				SwitchRectangle switchRect = (SwitchRectangle) rect;
				switchRects.put(switchRect, switchRect.getKey());
				mainFrame.add(rect, 1);
			}
			else {
				mainFrame.add(rect, 2);
			}
			physicsSimulator.add(rect);
			for (Area attached : rect.getAttachments()) {
				physicsSimulator.add(attached);
				mainFrame.add(attached, 0);
			}
		}

		for (WallRectangle wall : level.walls) {
			physicsSimulator.add(wall);
			mainFrame.add(wall, 3);
			for (Area attached : wall.getAttachments()) {
				physicsSimulator.add(attached);
				mainFrame.add(attached, 0);
			}
		}

		for (Area area : level.areas) {
			if (area instanceof SwitchArea) {
				SwitchArea switchArea = (SwitchArea) area;
				for (Entry<SwitchRectangle, String> entry : switchRects
						.entrySet()) {
					if (entry.getValue().equals(switchArea.getKey())) {
						switchArea.addChild(entry.getKey());
					}
				}
				physicsSimulator.add(switchArea);
			}
			else {
				physicsSimulator.add(area);
			}
			mainFrame.add(area, 0);
		}

		for (GoalArea goal : level.goals) {
			physicsSimulator.add(goal);
			mainFrame.add(goal, 0);
		}

		for (HintRectangle hint : level.hints) {
			mainFrame.add(hint, 3);
			hints.add(hint);
		}

	}

	private void newFrameTaskAction() {
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
	};

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
	private void nextFrame() {
		Pair<Map<Direction, Integer>, Set<MovementInput>> allInputs = gameInputHandler
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

		currentLevelOutputStream = new ByteArrayOutputStream();
		gameInputHandler.beginWriting(currentLevelOutputStream);
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
					"Couldn't open solution file '" + currentSolution + "'",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		reloadLevel();
		JOptionPane.showMessageDialog(mainFrame,
				"Stop playback at any time in the title bar: Recordings > Stop.",
				"Seen Enough?", JOptionPane.INFORMATION_MESSAGE);

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
					currentLevelOutputStream
							.writeTo(new FileOutputStream(file));
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
		SaveManager.putValue("CURRENT_LEVEL", currentLevel);
		System.exit(0);
	}

	@Override
	public void valueChanged(Enum<?> key, Object newValue) {
		if (key == Parameter.GAME_SPEED) {
			newFrameTask.cancel();
			newFrameTask = new TimerTask() {
				public void run() {
					newFrameTaskAction();
				}
			};
			new Timer().scheduleAtFixedRate(newFrameTask, 0,
					((Number) newValue).intValue());
		}
	}

	@Override
	public void valueRemoved(Enum<?> key) {}

}
