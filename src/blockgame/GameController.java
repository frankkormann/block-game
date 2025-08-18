package blockgame;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.SystemInfo;

import blockgame.gui.ErrorDialog;
import blockgame.gui.HintRectangle;
import blockgame.gui.ImageArea;
import blockgame.gui.MainFrame;
import blockgame.gui.MainFrame.Direction;
import blockgame.gui.MenuBar;
import blockgame.gui.MenuBar.MetaInput;
import blockgame.input.ColorMapper;
import blockgame.input.GameInputHandler;
import blockgame.input.GameInputHandler.MovementInput;
import blockgame.input.InputMapper;
import blockgame.input.ParameterMapper;
import blockgame.input.ParameterMapper.Parameter;
import blockgame.input.ValueChangeListener;
import blockgame.physics.Area;
import blockgame.physics.MovingRectangle;
import blockgame.physics.PhysicsSimulator;
import blockgame.physics.Rectangle;
import blockgame.physics.SwitchArea;
import blockgame.physics.SwitchController;
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

	private static final String FIRST_TITLE_SCREEN = "/title_0.json";
	private static final String FIRST_LEVEL = "/level_1-1.json";

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
		UIManager.put("TitlePane.menuBarTitleMinimumGap", 0);
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
		ImageArea.setColorMapper(colorMapper);

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

		startGame(SaveManager.getValue("title_screen", FIRST_TITLE_SCREEN),
				paramMapper.getInt(Parameter.GAME_SPEED));
	}

	/**
	 * Loads {@code titleScreen} and begins a repeating {@code TimerTask} to
	 * process each frame.
	 * 
	 * @param titleScreen         resource name of title screen to load
	 * @param millisBetweenFrames number of milliseconds between each frame
	 */
	public void startGame(String titleScreen, int millisBetweenFrames) {
		load(titleScreen);
		mainFrame.setVisible(true);

		if (SaveManager.getValue("new_save", "true").equals("true")) {
			JOptionPane.showMessageDialog(mainFrame,
					"You can change colors and controls in the Options menu at any time.",
					"Message", JOptionPane.INFORMATION_MESSAGE);
			SaveManager.putValue("new_save", "false");
		}

		newFrameTask = new TimerTask() {
			public void run() {
				newFrameTaskAction();
			}
		};
		new Timer().schedule(newFrameTask, 0, millisBetweenFrames);
	}

	/**
	 * Computes the next frame.
	 */
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
	}

	/**
	 * Restarts and reloads the current level, and stops reading from the
	 * recording file if there is one.
	 */
	private void reloadLevel() {
		gameInputHandler.endReading();
		load(currentLevel);
	}

	/**
	 * Loads the resource named {@code resource} into {@code physicsSimulator}
	 * and {@code mainFrame} as a level.
	 * 
	 * @param resource name of resource to load
	 */
	private void load(String resource) {
		paused = true;
		Level level;

		try {
			ObjectMapper mapper = new ObjectMapper();
			level = mapper.readValue(getClass().getResourceAsStream(resource),
					Level.class);
		}
		catch (Exception e) {
			e.printStackTrace();
			new ErrorDialog("Error",
					"Could not load level '" + resource
							+ "', file is corrupt or does not exist",
					e).setVisible(true);

			if (mainFrame.isVisible()) {
				physicsSimulator.resetNextlevel();
			}
			else {
				if (!resource.equals(FIRST_TITLE_SCREEN)) {
					System.err.println("Loading first title screen");
					load(FIRST_TITLE_SCREEN);
				}
				else {
					System.exit(1);
				}
			}

			paused = false;
			return;
		}

		physicsSimulator = new PhysicsSimulator();
		menuBar.reset();
		hints.clear();

		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(() -> mainFrame.setUpLevel(level));
			}
			catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
				new ErrorDialog("Error", "Something went wrong", e)
						.setVisible(true);
			}
		}
		else {
			mainFrame.setUpLevel(level);
		}

		currentSolution = level.solution;
		currentLevel = resource;
		if (level.newTitle != "") {
			SaveManager.putValue("title_screen", level.newTitle);
		}
		loadObjects(level);

		menuBar.showHintsMenu(hints.size() > 0 || !level.solution.equals(""));

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
	private void loadObjects(Level level) {

		List<SwitchArea> switchAreas = new ArrayList<>();
		List<SwitchRectangle> switchRects = new ArrayList<>();

		for (MovingRectangle rect : level.movingRectangles) {
			if (rect instanceof SwitchRectangle) {
				mainFrame.add(rect, 2);
			}
			else {
				mainFrame.add(rect, 3);
			}
			physicsSimulator.add(rect);
			if (rect instanceof SwitchRectangle) {
				switchRects.add((SwitchRectangle) rect);
			}
			for (Area attached : rect.getAttachments()) {
				level.areas.add(attached);
			}
		}

		for (WallRectangle wall : level.walls) {
			physicsSimulator.add(wall);
			mainFrame.add(wall, 4);
			for (Area attached : wall.getAttachments()) {
				level.areas.add(attached);
			}
		}

		for (Area area : level.areas) {
			physicsSimulator.add(area);
			if (area instanceof SwitchArea) {
				switchAreas.add((SwitchArea) area);
			}
			if (area instanceof ImageArea) {
				ImageArea imgArea = (ImageArea) area;
				mainFrame.add(imgArea, 0);
				if (imgArea.getImitatedArea() instanceof SwitchArea) {
					switchAreas.add((SwitchArea) imgArea.getImitatedArea());
				}
			}
			else {
				mainFrame.add(area, 1);
			}
		}

		for (HintRectangle hint : level.hints) {
			mainFrame.add(hint, 5);
			hints.add(hint);
		}

		linkSwitchAreasAndRects(switchAreas, switchRects);
	}

	/**
	 * Pairs each {@code SwitchArea} with all the {@code SwitchRectangle}s that
	 * share its key.
	 * 
	 * @param areas {@code List} of {@code SwitchArea}s
	 * @param rects {@code List} of {@code SwitchRectangle}s
	 */
	private void linkSwitchAreasAndRects(List<SwitchArea> areas,
			List<SwitchRectangle> rects) {
		Map<String, SwitchController> controllers = new HashMap<>();

		for (SwitchRectangle rect : rects) {
			if (!controllers.containsKey(rect.getKey())) {
				controllers.put(rect.getKey(), new SwitchController());
			}
			controllers.get(rect.getKey()).addSwitchRectangle(rect);
		}

		for (SwitchArea area : areas) {
			if (!controllers.containsKey(area.getKey())) {
				continue;
			}
			area.setController(controllers.get(area.getKey()));
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
	private void nextFrame() {
		Pair<Map<Direction, Integer>, Set<MovementInput>> allInputs = gameInputHandler
				.poll();

		mainFrame.resizeAll(allInputs.first);
		physicsSimulator.updateAndMoveObjects(allInputs.second,
				mainFrame.getNextWidth(), mainFrame.getNextHeight(),
				mainFrame.getNextXOffset(), mainFrame.getNextYOffset());

		if (physicsSimulator.getNextLevel() != "") {
			String nextLevel = physicsSimulator.getNextLevel();
			if (nextLevel.startsWith("$")) {
				nextLevel = SaveManager.getValue(nextLevel.substring(1),
						FIRST_LEVEL);
			}
			load(nextLevel);
			SaveManager.putValue("current_level", nextLevel);
			return;
		}

		mainFrame.resizeAll(physicsSimulator.getResizes());
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities
						.invokeAndWait(() -> mainFrame.incorporateChanges());
			}
			catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
				new ErrorDialog("Error", "Something went wrong", e)
						.setVisible(true);
			}
		}
		else {
			mainFrame.incorporateChanges();
		}
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

		String[] options = { "Show solution", "Cancel" };
		int proceedChoice = JOptionPane.showOptionDialog(mainFrame,
				"This option is intended to be used if you are absolutely stuck. Please give the puzzle a good effort before watching the solution.\n\nIf you have seen enough and want to stop the recording partway through, select Recordings > Stop in the title bar.",
				"Continue?", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		if (proceedChoice != JOptionPane.OK_OPTION) {
			return;
		}

		reloadLevel();

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
					OutputStream out = new FileOutputStream(file);
					currentLevelOutputStream.writeTo(out);
					out.close();
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

	@Override
	public void valueChanged(Enum<?> key, Object newValue) {
		if (key == Parameter.GAME_SPEED) {
			newFrameTask.cancel();
			newFrameTask = new TimerTask() {
				public void run() {
					newFrameTaskAction();
				}
			};
			new Timer().schedule(newFrameTask, 0,
					((Number) newValue).intValue());
		}
	}

	@Override
	public void valueRemoved(Enum<?> key) {}

}
