package game;

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

/**
 * Get inputs from the user or a file. Optionally write inputs to a file.
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
 * Input is accessible through {@link#getKeysPressed()} and
 * {@link#getResizes()}. These methods should both be called every frame to
 * ensure file reading/writing does not get desynchronized. These methods should
 * always be called in the same order.
 * 
 * @author Frank Kormann
 */
public class GameInputHandler extends KeyAdapter {

	public enum GameInput {
		UP(KeyEvent.VK_W, KeyEvent.VK_UP, KeyEvent.VK_SPACE),
		LEFT(KeyEvent.VK_A, KeyEvent.VK_LEFT), RIGHT(KeyEvent.VK_D, KeyEvent.VK_RIGHT);

		public final int[] keyCodes;

		private GameInput(int... keyCodes) {
			this.keyCodes = keyCodes;
		}
	}

	private Set<Integer> keysPressed;
	private Map<MainFrame.Direction, Integer> resizesSinceLastFrame;
	private InputStream reader;
	private OutputStream writer;

	private int nextByte;

	public GameInputHandler() {
		keysPressed = new HashSet<>();
		resizesSinceLastFrame = new HashMap<>();
		zeroAllDirectionsInResizes();

		reader = null;
		writer = null;

		nextByte = 0;
	}

	private void zeroAllDirectionsInResizes() {
		for (MainFrame.Direction direction : MainFrame.Direction.values()) {
			resizesSinceLastFrame.put(direction, 0);
		}
	}

	/**
	 * Takes input from an {@code InputStream}. If this is already reading from a
	 * different {@code InputStream}, that stream is replaced.
	 * 
	 * @param input {@code InputStream} to read from
	 */
	public void beginReading(InputStream input) {
		reader = input;
		readByte();  // prime nextByte
	}

	/**
	 * Stops reading and closes the file. If this is not reading, this method has no
	 * effect.
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
		}
	}

	/**
	 * Starts writing input to an {@code OutputStream}. If a different
	 * {@code OutputStream} is already being written to, that stream is replaced.
	 * 
	 * @param output {@code OutputStream} to write to
	 */
	public void beginWriting(OutputStream output) {
		writer = output;
	}

	public void flushWriter() {
		try {
			writer.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stops writing and closes the file. If this is not writing, this method has no
	 * effect.
	 */
	public void endWriting() {
		if (writer == null) {
			return;
		}
		try {
			writer.close();
			writer = null;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads the next {@code byte} in the input stream and it as an {@code int}.
	 * <p>
	 * Closes the input stream if end-of-input is detected.
	 * 
	 * @return next {@code byte} as {@code int}
	 */
	private int readByte() {
		int value = nextByte;

		try {
			nextByte = reader.read();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		if (nextByte == -1) {
			endReading();
		}

		return value;
	}

	/**
	 * Interprets the next 4 bytes in the input stream as an {@code int}.
	 * <p>
	 * Closes the input stream if end-of-input is detected.
	 * 
	 * @return next {@code int}
	 */
	private int readInt() {
		int i = 0;

		i += (readByte() & 0xFF) << (Integer.SIZE - Byte.SIZE);
		for (int j = 1; j < Integer.BYTES; j++) {
			i >>>= Byte.SIZE;
			i += (readByte() & 0xFF) << (Integer.SIZE - Byte.SIZE);
		}

		return i;
	}

	/**
	 * Writes a single {@code byte} to the output stream.
	 * 
	 * @param b {@code byte} to write
	 */
	private void writeByte(int b) {
		try {
			writer.write(b);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes an {@code int}, encoded as four {@code byte}s, to the output stream.
	 * 
	 * @param i {@code int} to write
	 */
	private void writeInt(int i) {
		byte[] bytes = new byte[Integer.BYTES];
		for (int j = 0; j < Integer.BYTES; j++) {
			bytes[j] = (byte) i;
			i >>= Byte.SIZE;
		}

		try {
			writer.write(bytes);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the inputs pressed on this frame.
	 * <p>
	 * If in reading mode, these will be read from the reading file. Otherwise, they
	 * will be taken from the keyboard.
	 * <p>
	 * If in writing mode, these will also be written to the writing file.
	 * 
	 * @return {@code Set} of {@code Input}s
	 */
	public Set<GameInput> getInputs() {
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
			gameInputs = readInputsFromFile();
		}

		if (writer != null) {
			writeInputsToFile(gameInputs);
		}

		return gameInputs;
	}

	/**
	 * Returns the {@code Input}s pressed on this frame in the reading file.
	 */
	private Set<GameInput> readInputsFromFile() {
		Set<GameInput> gameInputs = EnumSet.noneOf(GameInput.class);

		int numberOfInputs = readByte();
		for (int i = 0; i < numberOfInputs; i++) {
			int inputOrdinal = readByte();
			gameInputs.add(GameInput.values()[inputOrdinal]);
		}

		return gameInputs;
	}

	/**
	 * First writes the number of {@code Input}s pressed this frame, then each
	 * {@code Input}'s ordinal in turn.
	 * 
	 * @param gameInputs {@code Set} of {@code Input}s to write
	 */
	private void writeInputsToFile(Set<GameInput> gameInputs) {
		writeByte(gameInputs.size());
		for (GameInput inp : gameInputs) {
			writeByte(inp.ordinal());
		}
	}

	/**
	 * Get the resizes on this frame. If in reading mode, these will be read from
	 * the reading file. Otherwise, they will be taken from the
	 * {@code ResizingSides}. If in writing mode, these will also be written to the
	 * writing file.
	 * 
	 * @return {@code Map} of {@code Direction} to {@code Integer} amount resized
	 */
	public Map<MainFrame.Direction, Integer> getResizes() {
		Map<MainFrame.Direction, Integer> resizes = new HashMap<>();
		if (reader == null) {
			resizes.putAll(resizesSinceLastFrame);
		}
		else {
			// Make sure values are read in the correct order
			resizes.put(MainFrame.Direction.NORTH, readInt());
			resizes.put(MainFrame.Direction.SOUTH, readInt());
			resizes.put(MainFrame.Direction.WEST, readInt());
			resizes.put(MainFrame.Direction.EAST, readInt());
		}
		if (writer != null) {
			// Make sure values are written in the correct order
			writeInt(resizes.get(MainFrame.Direction.NORTH));
			writeInt(resizes.get(MainFrame.Direction.SOUTH));
			writeInt(resizes.get(MainFrame.Direction.WEST));
			writeInt(resizes.get(MainFrame.Direction.EAST));
		}
		zeroAllDirectionsInResizes();
		return resizes;
	}

	public void resize(MainFrame.Direction direction, int amount) {
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

}
