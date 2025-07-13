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

import javax.swing.JOptionPane;

import game.MainFrame.Direction;

/**
 * Get inputs from the user or a stream. Optionally write inputs to a stream.
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
public class GameInputHandler extends KeyAdapter implements Resizable {

	public enum GameInput {
		UP(KeyEvent.VK_W, KeyEvent.VK_UP, KeyEvent.VK_SPACE),
		LEFT(KeyEvent.VK_A, KeyEvent.VK_LEFT), RIGHT(KeyEvent.VK_D, KeyEvent.VK_RIGHT);

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
	private InputStream reader;
	private OutputStream writer;

	private int nextByte;
	private int writingZerosInARow;
	private int readingZerosInARow;

	/**
	 * Creates a new {@code GameInputHandler} with no input stream or output stream.
	 */
	public GameInputHandler() {
		keysPressed = new HashSet<>();
		resizesSinceLastFrame = new HashMap<>();
		zeroAllDirectionsInResizes();

		reader = null;
		writer = null;
	}

	/**
	 * Returns the inputs and resizes for this frame. If in reading mode, these will
	 * be taken from the input stream; otherwise, they will be taken from the user.
	 * If in writing mode, these will be written to the output stream.
	 * 
	 * @return {@code Pair} of {@code Map<Direction, Integer>} for resizes in each
	 *         direction and {@code Set<GameInput>} for inputs
	 */
	public Pair<Map<Direction, Integer>, Set<GameInput>> poll() {
		try {
			return new Pair<>(getResizes(), getInputs());
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Malformed recording data",
					"Error reading file", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			endReading();
			return new Pair<>(new HashMap<>(), new HashSet<>());
		}
	}

	/**
	 * Get the resizes on this frame. If in reading mode, these will be read from
	 * the input stream . Otherwise, they will be taken from the
	 * {@code ResizingSides}.
	 * <p>
	 * If in writing mode, these will also be written to the output stream.
	 * 
	 * @return {@code Map} of {@code Direction} to {@code Integer} amount resized
	 */
	private Map<Direction, Integer> getResizes() throws IOException {
		Map<Direction, Integer> resizes = new HashMap<>();
		if (reader == null) {
			resizes.putAll(resizesSinceLastFrame);
		}
		else {
			// Make sure values are read in the correct order
			resizes.put(Direction.NORTH, readInt());
			resizes.put(Direction.SOUTH, readInt());
			resizes.put(Direction.WEST, readInt());
			resizes.put(Direction.EAST, readInt());
		}
		if (writer != null) {
			// Make sure values are written in the correct order
			writeInt(resizes.get(Direction.NORTH));
			writeInt(resizes.get(Direction.SOUTH));
			writeInt(resizes.get(Direction.WEST));
			writeInt(resizes.get(Direction.EAST));
		}
		zeroAllDirectionsInResizes();
		return resizes;
	}

	/**
	 * Gets the inputs pressed on this frame.
	 * <p>
	 * If in reading mode, these will be read from the input stream. Otherwise, they
	 * will be taken from the keyboard.
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
	 * Takes input from an {@code InputStream}. If this is already reading from a
	 * different {@code InputStream}, that stream is replaced.
	 * 
	 * @param input {@code InputStream} to read from
	 */
	public void beginReading(InputStream input) {
		reader = input;
		readingZerosInARow = 0;
		try {
			nextByte = reader.read();
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
		writingZerosInARow = 0;
	}

	/**
	 * Stops reading and closes the stream. If this is not reading, this method has
	 * no effect.
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
	 * Stops writing and closes the stream. If this is not writing, this method has
	 * no effect.
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
		}
	}

	/**
	 * Returns the {@code Input}s pressed on this frame in the input stream.
	 */
	private Set<GameInput> readInputs() throws IOException {
		Set<GameInput> gameInputs = EnumSet.noneOf(GameInput.class);

		int numberOfInputs = readByte();
		for (int i = 0; i < numberOfInputs; i++) {
			int inputOrdinal = readByte();
			gameInputs.add(GameInput.values()[inputOrdinal]);
		}

		return gameInputs;
	}

	/**
	 * Reads the next {@code byte} in the input stream and returns it as an
	 * {@code int}.
	 * <p>
	 * Follows the format defined by {@link#writeByte(int)}.
	 * <p>
	 * Closes the input stream if end-of-input is detected.
	 * 
	 * @return next {@code byte} as {@code int}
	 */
	private int readByte() throws IOException {

		int value;

		if (readingZerosInARow > 0) {
			readingZerosInARow--;
			value = 0;
		}
		else {

			if (nextByte == 0) {
				readingZerosInARow = reader.read();
			}

			value = nextByte;

			nextByte = reader.read();

		}

		if (nextByte == -1 && readingZerosInARow == 0) {
			endReading();
		}

		return value;
	}

	/**
	 * Interprets the next bytes as an {@code int} written by {@link#writeInt(int)}.
	 * <p>
	 * Closes the input stream if end-of-input is detected.
	 * 
	 * @return next {@code int}
	 */
	private int readInt() throws IOException {
		int i = 0;

		int numBytes = (byte) readByte();  // Cast to byte for negative values
		int sign = (int) Math.signum(numBytes);
		numBytes = Math.abs(numBytes);

		for (int j = 0; j < numBytes; j++) {
			i += (readByte() & 0xFF) << (Integer.SIZE - Byte.SIZE) * j;
		}

		i *= sign;

		return i;
	}

	/**
	 * First writes the number of {@code Input}s pressed this frame, then each
	 * {@code Input}'s ordinal in turn.
	 * 
	 * @param gameInputs {@code Set} of {@code Input}s to write
	 */
	private void writeInputs(Set<GameInput> gameInputs) {
		writeByte(gameInputs.size());
		for (GameInput inp : gameInputs) {
			writeByte(inp.ordinal());
		}
	}

	/**
	 * Writes a single {@code byte} to the output stream.
	 * <p>
	 * For {@code 0} specifically, instead of writing many {@code 0}s in a row, only
	 * the first {@code 0} is written. Then the number of following {@code 0}s is
	 * written.
	 * 
	 * @param b {@code byte} to write
	 */
	private void writeByte(int b) {
		try {

			if (b == 0) {
				writingZerosInARow++;
			}
			if ((b != 0 && writingZerosInARow > 0) || writingZerosInARow > 0xFF) {
				writer.write(writingZerosInARow - 1);
				writingZerosInARow = 0;
			}
			if ((b == 0 && writingZerosInARow == 1) || b != 0) {
				writer.write(b);
			}

		}
		catch (IOException e) {
			e.printStackTrace();
			endWriting();
		}
	}

	/**
	 * Writes a compressed {@code int} to the output stream.
	 * <p>
	 * First, {@code i} is split into four bytes. Any bytes which are entirely
	 * {@code 0} and do not have useful bytes above them are thrown out. Then, the
	 * number of remaining bytes is written and each byte is written in turn. If
	 * {@i == 0}, only one byte (being 0) is written.
	 * <p>
	 * If {@code i} is negative, the byte indicating the number of bytes in
	 * {@code i} will be negative. Each byte of {@code i} represents {@code i}'s
	 * absolute value.
	 * 
	 * @param i {@code int} to write
	 */
	private void writeInt(int i) {

		if (i == 0) {
			writeByte(0);
			return;
		}

		byte[] bytes = new byte[Integer.BYTES];
		int usefulBytes;
		int originalSign = (int) Math.signum(i);

		i = Math.abs(i);

		for (int j = 0; j < Integer.BYTES; j++) {
			bytes[j] = (byte) i;
			i >>= Byte.SIZE;
		}

		for (usefulBytes = bytes.length; usefulBytes > 0
				&& bytes[usefulBytes - 1] == 0; usefulBytes--)
			;

		writeByte(usefulBytes * originalSign);
		for (int j = 0; j < usefulBytes; j++) {
			writeByte(bytes[j]);
		}

	}

	/**
	 * Ensures that all inputs are written to the output stream.
	 */
	public void flushWriter() {
		try {
			if (writingZerosInARow > 0) {
				writer.write(writingZerosInARow - 1);
				writingZerosInARow = 0;
			}
			writer.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
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

}
