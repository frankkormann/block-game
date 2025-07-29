package game;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import game.MainFrame.Direction;

/**
 * Get inputs from the user or a stream. Optionally write inputs to a stream.
 * <p>
 * This should be registered as a {@code KeyListener} and {@code FocusListener}
 * to the main window. If the window loses focus, all keys will be considered
 * un-pressed to prevent a key from becoming "stuck" down since the
 * {@code KeyEvent.KEY_RELEASED} event cannot be dispatched.
 * <p>
 * By default, inputs will be taken from the user. Key presses will be taken
 * from the keyboard and window resizes will be taken from calls to
 * {@code resize}. These calls are provided by the {@code ResizingSide} class.
 * <p>
 * If an {@code InputStream} is given to {@code beginReading(InputStream)},
 * input will be taken from that stream instead.
 * <p>
 * If an {@code OutputStream} is given to {@code beginWriting(OutputStream)},
 * input will be written to that stream.
 * <p>
 * Input is accessible through {@link#poll()}.
 * 
 * @author Frank Kormann
 */
public class GameInputHandler extends KeyAdapter
		implements FocusListener, Resizable {

	public enum GameInput {
		UP, LEFT, RIGHT
	}

	public enum DirectionSelectorInput {
		SELECT_NORTH, SELECT_SOUTH, SELECT_WEST, SELECT_EAST
	}

	public enum ResizingInput {
		MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT
	}

	private static final int KEYBOARD_RESIZE_AMOUNT = 5;

	private InputMapper inputMapper;

	private Set<Integer> keysPressed;
	private Map<Direction, Integer> resizesSinceLastFrame;
	private boolean isNorthSelected;
	private boolean isWestSelected;

	private NumberReader reader;
	private NumberWriter writer;

	/**
	 * Creates a new {@code GameInputHandler} with no input stream or output
	 * stream.
	 */
	public GameInputHandler(InputMapper inputMapper) {
		this.inputMapper = inputMapper;

		keysPressed = new HashSet<>();
		resizesSinceLastFrame = new HashMap<>();
		isNorthSelected = true;
		isWestSelected = true;

		zeroAllDirectionsInResizes();

		reader = null;
		writer = null;
	}

	/**
	 * Returns the inputs and resizes for this frame. If in reading mode, these
	 * will be taken from the input stream; otherwise, they will be taken from
	 * the user. If in writing mode, these will be written to the output stream.
	 * 
	 * @return {@code Pair} of {@code Map<Direction, Integer>} for resizes in
	 *         each direction and {@code Set<GameInput>} for inputs
	 */
	public Pair<Map<Direction, Integer>, Set<GameInput>> poll() {
		try {
			return new Pair<>(getResizes(), getInputs());
		}
		catch (Exception e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Malformed recording data", e)
					.setVisible(true);
			endReading();
			return new Pair<>(new HashMap<>(), new HashSet<>());
		}
	}

	/**
	 * Get the resizes on this frame. If in reading mode, these will be read
	 * from the input stream . Otherwise, they will be taken from the
	 * {@code ResizingSides}.
	 * <p>
	 * If in writing mode, these will also be written to the output stream.
	 * 
	 * @return {@code Map} of {@code Direction} to {@code Integer} amount
	 *         resized
	 */
	private Map<Direction, Integer> getResizes() throws IOException {
		Map<Direction, Integer> resizes = new HashMap<>();
		if (reader == null) {
			addResizesFromKeyboard(resizes);
			resizes.putAll(resizesSinceLastFrame);
		}
		else {
			// Make sure values are read in the correct order
			resizes.put(Direction.NORTH, reader.readInt());
			resizes.put(Direction.SOUTH, reader.readInt());
			resizes.put(Direction.WEST, reader.readInt());
			resizes.put(Direction.EAST, reader.readInt());
		}
		if (writer != null) {
			// Make sure values are written in the correct order
			writer.writeInt(resizes.get(Direction.NORTH));
			writer.writeInt(resizes.get(Direction.SOUTH));
			writer.writeInt(resizes.get(Direction.WEST));
			writer.writeInt(resizes.get(Direction.EAST));
		}

		zeroAllDirectionsInResizes();
		return resizes;

	}

	/**
	 * Adds any resizes from the keyboard.
	 * 
	 * @param resizes {@code Map} to add resizes to
	 */
	private void addResizesFromKeyboard(Map<Direction, Integer> resizes) {
		for (ResizingInput inp : ResizingInput.values()) {
			Pair<Integer, Integer> keybind = inputMapper.getKeybind(inp);
			if (keysPressed.contains(keybind.first)
					&& containsMask(keysPressed, keybind.second)) {
				int amount = KEYBOARD_RESIZE_AMOUNT;

				switch (inp) {
					case MOVE_UP:
						amount *= -1;
					case MOVE_DOWN:
						if (isNorthSelected) {
							resize(amount, Direction.NORTH);
						}
						else {
							resize(amount, Direction.SOUTH);
						}
						break;
					case MOVE_LEFT:
						amount *= -1;
					case MOVE_RIGHT:
						if (isWestSelected) {
							resize(amount, Direction.WEST);
						}
						else {
							resize(amount, Direction.EAST);
						}
						break;
				}
			}
		}
	}

	/**
	 * Gets the inputs pressed on this frame.
	 * <p>
	 * If in reading mode, these will be read from the input stream. Otherwise,
	 * they will be taken from the keyboard.
	 * <p>
	 * If in writing mode, these will also be written to the output stream.
	 * 
	 * @return {@code Set} of {@code Input}s
	 */
	private Set<GameInput> getInputs() throws IOException {
		Set<GameInput> gameInputs = EnumSet.noneOf(GameInput.class);

		if (reader == null) {
			for (GameInput inp : GameInput.values()) {
				Pair<Integer, Integer> keybind = inputMapper.getKeybind(inp);
				if (keysPressed.contains(keybind.first)
						&& containsMask(keysPressed, keybind.second)) {
					gameInputs.add(inp);
				}
			}
		}
		else {
			gameInputs = readInputs();
		}

		if (writer != null) {
			writeInputs(gameInputs);
		}

		return gameInputs;
	}

	/**
	 * Takes input from an {@code InputStream}. If this is already reading from
	 * a different {@code InputStream}, that stream is replaced. Note that this
	 * does not close the previous stream.
	 * 
	 * @param input {@code InputStream} to read from
	 */
	public void beginReading(InputStream input) {
		try {
			reader = new NumberReader(input);
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Couldn't open stream for reading", e)
					.setVisible(true);
		}
	}

	/**
	 * Starts writing input to an {@code OutputStream}. If a different
	 * {@code OutputStream} is already being written to, that stream is
	 * replaced. Note that this does not close the previous stream.
	 * 
	 * @param output {@code OutputStream} to write to
	 */
	public void beginWriting(OutputStream output) {
		writer = new NumberWriter(output);
	}

	/**
	 * Stops reading and closes the stream. If this is not reading, this method
	 * has no effect.
	 */
	public void endReading() {
		if (reader == null) {
			return;
		}
		try {
			reader.close();
			reader = null;
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Couldn't close reading stream", e)
					.setVisible(true);
		}
	}

	/**
	 * Stops writing and closes the stream. If this is not writing, this method
	 * has no effect.
	 */
	public void endWriting() {
		if (writer == null) {
			return;
		}
		try {
			flushWriter();
			writer.close();
			writer = null;
		}
		catch (IOException e) {
			e.printStackTrace();
			// Don't pop up an ErrorDialog because the user probably doesn't
			// care
		}
	}

	/**
	 * Returns the {@code Input}s pressed on this frame in the input stream.
	 */
	private Set<GameInput> readInputs() throws IOException {
		Set<GameInput> gameInputs = EnumSet.noneOf(GameInput.class);

		int numberOfInputs = reader.readByte();
		for (int i = 0; i < numberOfInputs; i++) {
			int inputOrdinal = reader.readByte();
			gameInputs.add(GameInput.values()[inputOrdinal]);
		}

		if (!reader.isOpen) {
			reader = null;
		}

		return gameInputs;
	}

	/**
	 * First writes the number of {@code Input}s pressed this frame, then each
	 * {@code Input}'s ordinal in turn.
	 * 
	 * @param gameInputs {@code Set} of {@code Input}s to write
	 */
	private void writeInputs(Set<GameInput> gameInputs) throws IOException {
		if (!writer.isOpen) {
			writer = null;
			return;
		}

		writer.writeByte(gameInputs.size());
		for (GameInput inp : gameInputs) {
			writer.writeByte(inp.ordinal());
		}
	}

	/**
	 * Ensures that all inputs are written to the output stream.
	 */
	public void flushWriter() {
		try {
			writer.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Potential error",
					"Couldn't flush output stream, output file may be corrupted",
					e).setVisible(true);
		}
	}

	/**
	 * Selects a new direction for {@code selectedDirection} if {@code keyCode}
	 * and {@code modifiers} match a {@code DirectionSelectorInput}.
	 * 
	 * @param keyCode   key code which was pressed
	 * @param modifiers modifier mask which was held
	 * 
	 * @return {@code true} if a new direction was selected
	 */
	private boolean selectDirection(int keyCode, int modifiers) {
		int mouseMasks = KeyEvent.BUTTON1_DOWN_MASK + KeyEvent.BUTTON2_DOWN_MASK
				+ KeyEvent.BUTTON3_DOWN_MASK;
		int modifiersWithoutMouse = modifiers & ~mouseMasks;

		for (DirectionSelectorInput inp : DirectionSelectorInput.values()) {
			Pair<Integer, Integer> keybind = inputMapper.getKeybind(inp);
			if (keyCode == keybind.first
					&& (modifiersWithoutMouse ^ keybind.second) == 0) {
				switch (inp) {
					case SELECT_NORTH:
						isNorthSelected = true;
						return true;
					case SELECT_SOUTH:
						isNorthSelected = false;
						return true;
					case SELECT_WEST:
						isWestSelected = true;
						return true;
					case SELECT_EAST:
						isWestSelected = false;
						return true;
				}
			}
		}

		return false;
	}

	/**
	 * Tests whether {@code keys} contains all of the modifier key codes that
	 * would produce {@code modifierMask} and none of the ones that would not.
	 * <p>
	 * For example, if
	 * {@code modifierMask == KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK},
	 * this returns {@code true} if {@code keys} contains
	 * {@code KeyEvent.VK_SHIFT} and {@code KeyEvent.VK_CONTROL} and no other
	 * modifier keys.
	 * 
	 * @param keys         {@code Set} of key codes to test
	 * @param modifierMask modifier mask to compare against
	 * 
	 * @return {@code true} if {@code keys} contains exactly the modifier key
	 *         codes necessary to produce {@code modifierMask}
	 */
	private boolean containsMask(Set<Integer> keys, int modifierMask) {
		if ((modifierMask & KeyEvent.ALT_DOWN_MASK) != 0
				^ keys.contains(KeyEvent.VK_ALT))
			return false;
		if ((modifierMask & KeyEvent.ALT_GRAPH_DOWN_MASK) != 0
				^ keys.contains(KeyEvent.VK_ALT_GRAPH))
			return false;
		if ((modifierMask & KeyEvent.CTRL_DOWN_MASK) != 0
				^ keys.contains(KeyEvent.VK_CONTROL))
			return false;
		if ((modifierMask & KeyEvent.META_DOWN_MASK) != 0
				^ keys.contains(KeyEvent.VK_META))
			return false;
		if ((modifierMask & KeyEvent.SHIFT_DOWN_MASK) != 0
				^ keys.contains(KeyEvent.VK_SHIFT))
			return false;

		return true;
	}

	private void zeroAllDirectionsInResizes() {
		for (Direction direction : Direction.values()) {
			resizesSinceLastFrame.put(direction, 0);
		}
	}

	/**
	 * Records a resize for the next frame's inputs.
	 * <p>
	 * If multiple resizes are given for the same direction, only their sum is
	 * remembered.
	 * 
	 * @param amount    difference between the new length and the old length
	 * @param direction which way to resize
	 */
	@Override
	public void resize(int amount, Direction direction) {
		int newAmount = resizesSinceLastFrame.get(direction) + amount;
		resizesSinceLastFrame.put(direction, newAmount);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (selectDirection(e.getKeyCode(), e.getModifiersEx())) {
			return;
		}

		keysPressed.add(e.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keysPressed.remove(e.getKeyCode());
	}

	@Override
	public void focusGained(FocusEvent e) {}

	@Override
	public void focusLost(FocusEvent e) {
		keysPressed.clear();
	}

}
