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
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.LineEvent;
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
import blockgame.input.VolumeMapper;
import blockgame.physics.Area;
import blockgame.physics.GoalArea;
import blockgame.physics.MovingRectangle;
import blockgame.physics.PhysicsSimulator;
import blockgame.physics.Rectangle;
import blockgame.physics.RevealingArea;
import blockgame.physics.SwitchArea;
import blockgame.physics.SwitchController;
import blockgame.physics.SwitchRectangle;
import blockgame.physics.WallRectangle;
import blockgame.sound.MusicPlayer;
import blockgame.sound.SoundEffectPlayer;
import blockgame.sound.SoundEffectPlayer.SoundEffect;
import blockgame.util.Pair;
import blockgame.util.SaveManager;

/**
 * Coordinates {@code MainFrame}, {@code PhysicsSimulator},
 * {@code SoundEffectPlayer}, and {@code GameInputHandler}.
 * <p>
 * Level data is read from JSON files. The JSON is used to fill the fields in
 * {@link Level}, so it should have data for each of that class's public
 * attributes.
 * <p>
 * If the "BLOCKGAME_DIRECTORY" environment variable is set, it will be used as
 * the path to put save files. Otherwise, a default directory is used.
 * 
 * @author Frank Kormann
 */
public class GameController extends WindowAdapter
		implements ValueChangeListener {

	private static final String DIRECTORY_ENV_VAR = "BLOCKGAME_DIRECTORY";

	private static final String FIRST_TITLE_SCREEN = "/title_0.json";
	private static final String FIRST_LEVEL = "/level_1-1.json";

	private MainFrame mainFrame;
	private PhysicsSimulator physicsSimulator;
	private SoundEffectPlayer sfxPlayer;
	private GameInputHandler gameInputHandler;
	private MenuBar menuBar;

	private String currentLevel;
	private int currentLevelNumber;
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
		System.setProperty("flatlaf.uiScale.allowScaleDown", "true");
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
		VolumeMapper volumeMapper = new VolumeMapper();

		Rectangle.setColorMapper(colorMapper);
		Rectangle.setParameterMapper(paramMapper);

		MusicPlayer musicPlayer = new MusicPlayer(volumeMapper);
		gameInputHandler = new GameInputHandler(inputMapper, paramMapper);
		// physicsSimulator is instantiated when the first level is loaded
		mainFrame = new MainFrame(gameInputHandler, paramMapper);
		sfxPlayer = new SoundEffectPlayer(volumeMapper);
		menuBar = new MenuBar(inputMapper, colorMapper, paramMapper,
				volumeMapper, musicPlayer, this);
		menuBar.showLevelSelect(
				SaveManager.getValue("level_select_unlocked", "false")
						.equals("true"));

		currentLevel = "";
		currentLevelNumber = -1;
		currentSolution = "";
		currentLevelOutputStream = null;
		hints = new ArrayList<>();

		mainFrame.addWindowListener(this);
		mainFrame.setJMenuBar(menuBar);

		paused = false;

		paramMapper.addListener(this);

		SoundEffect.GAME_START.clip.addLineListener(e -> {
			if (e.getType() == LineEvent.Type.STOP) {
				musicPlayer.playSaved();
			}
		});

		if (isLevelInField("visited_levels", 5)) {  // Update saves from older
													  // versions
			SaveManager.putValue("level_select_unlocked", "true");
		}

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
		sfxPlayer.play(SoundEffect.GAME_START);
		loadTitle(titleScreen);
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
	 * Loads the title screen referred to by {@code resource}. Unlike
	 * {@link #loadLevel(String)}, this does not set {@code current_level} in
	 * the save data.
	 * 
	 * @param resource name of resource to load
	 */
	public void loadTitle(String resource) {
		load(resource);
	}

	/**
	 * Loads the level referred to by {@code resource}. Unlike
	 * {@link #loadTitle(String)}, this sets {@code current_level} in the save
	 * data.
	 * 
	 * @param resource name of resource to load.
	 */
	public void loadLevel(String resource) {
		load(resource);
		SaveManager.putValue("current_level", resource);
	}

	/**
	 * Loads the resource named {@code resource} into {@code physicsSimulator}
	 * and {@code mainFrame} as a level.
	 * 
	 * @param resource name of resource to load
	 */
	private void load(String resource) {
		paused = true;

		Level level = readLevel(resource);
		if (level == null) {
			paused = false;
			return;
		}

		physicsSimulator = new PhysicsSimulator();
		sfxPlayer.clear();
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
		currentLevelNumber = level.number;
		for (Entry<String, String> entry : level.storeValues.entrySet()) {
			SaveManager.putValue(entry.getKey(), entry.getValue());
		}
		menuBar.showLevelSelect(
				(SaveManager.getValue("level_select_unlocked", "false")
						.equals("true")));
		loadObjects(level);

		menuBar.showHintsMenu(hints.size() > 0 || !level.solution.equals(""));

		physicsSimulator.setUp(mainFrame.getNextWidth(),
				mainFrame.getNextHeight(), mainFrame.getNextXOffset(),
				mainFrame.getNextYOffset());

		mainFrame.moveToMiddleOfScreen();
		beginTempRecording();

		paused = false;

		if (!level.popup.equals("")
				&& !isLevelInField("visited_levels", level.number)) {
			JOptionPane.showMessageDialog(mainFrame, level.popup);
		}
		markLevelInField("visited_levels", level.number);
	}

	/**
	 * Reads the JSON data in the resource file as a {@code Level} object.
	 * <p>
	 * If the resource can't be read and the game just started, tries to read
	 * {@code FIRST_TITLE_SCREEN}. If the player is in a level, calls
	 * {@code physicsSimulator.resetNextLevel()} and returns {@code null}.
	 * 
	 * @param resource name of resource to read
	 * 
	 * @return the {@code Level}
	 */
	private Level readLevel(String resource) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(getClass().getResourceAsStream(resource),
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
					System.err.println(
							"In GameController.java#readLevel: Can't load resource: Loading first title screen");
					return readLevel(FIRST_TITLE_SCREEN);
				}
				else {
					System.exit(1);
				}
			}
			return null;
		}
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
			sfxPlayer.add(rect);
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
			if (area instanceof GoalArea) {
				sfxPlayer.add((GoalArea) area);
			}
			if (area instanceof SwitchArea) {
				switchAreas.add((SwitchArea) area);
			}
			if (area instanceof RevealingArea) {
				((RevealingArea) area).setRevealAction(a -> {
					physicsSimulator.addSafe(a);
					mainFrame.add(a, 1);
				});
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
				continue;  // Not necessary to create a SwitchController for it
							  // because it would affect no SwitchRectangles
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
		sfxPlayer.playSounds();

		if (!physicsSimulator.getNextLevel().equals("")) {
			String nextLevel = physicsSimulator.getNextLevel();
			if (nextLevel.startsWith("$")) {
				nextLevel = SaveManager.getValue(nextLevel.substring(1),
						FIRST_LEVEL);
			}
			markLevelInField("completed_levels", currentLevelNumber);
			loadLevel(nextLevel);
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
	 * Puts the level at {@code levelNumbeR} into the saved value named
	 * {@code fieldName}. The value is interpreted as a bitfield of level
	 * numbers.
	 * 
	 * @param levelNumber numerical identifier of the level
	 */
	private void markLevelInField(String fieldName, int levelNumber) {
		if (levelNumber < 0) {
			return;
		}
		long levelFlags = Long.valueOf(SaveManager.getValue(fieldName, "0"));
		levelFlags |= 1L << levelNumber;
		SaveManager.putValue(fieldName, Long.toString(levelFlags));
	}

	/**
	 * Returns whether the level at {@code levelNumber} has been marked in the
	 * saved value named {@code fieldName}. The value is interpreted as a
	 * bitfield of level numbers.
	 * <p>
	 * If {@code levelNumber} is less than 0, always returns {@code false}.
	 * 
	 * @param levelNumber numerical identifier of the level
	 * 
	 * @return {@code true} if it has been completed
	 */
	private boolean isLevelInField(String fieldName, int levelNumber) {
		if (levelNumber < 0) {
			return false;
		}
		long levelFlags = Long.valueOf(SaveManager.getValue(fieldName, "0"));
		return (levelFlags & (1L << levelNumber)) != 0;
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
