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
		UP(KeyEvent.VK_W, KeyEvent.VK_UP, KeyEvent.VK_SPACE),
		LEFT(KeyEvent.VK_A, KeyEvent.VK_LEFT),
		RIGHT(KeyEvent.VK_D, KeyEvent.VK_RIGHT);

		/**
		 * Key codes which will trigger this
		 */
		public final int[] keyCodes;

		private GameInput(int... keyCodes) {
			this.keyCodes = keyCodes;
		}
	}

	private Set<Integer> keysPressed;
	private Map<Direction, Integer> resizesSinceLastFrame;
	private NumberReader reader;
	private NumberWriter writer;

	/**
	 * Creates a new {@code GameInputHandler} with no input stream or output
	 * stream.
	 */
	public GameInputHandler() {
		keysPressed = new HashSet<>();
		resizesSinceLastFrame = new HashMap<>();
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
				for (int key : inp.keyCodes) {
					if (keysPressed.contains(key)) {
						gameInputs.add(inp);
						break;
					}
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
	 * a different {@code InputStream}, that stream is replaced.
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
	 * replaced.
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
