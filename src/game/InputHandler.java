package game;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
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
 * If a {@code URL} is given to {@code beginReading(URL)}, input will be taken
 * from that file instead.
 * <p>
 * If a {@code URL} is given to {@code beginWriting(URL)} and a file at that
 * {@code URL} exists, input will be written to that file.
 * <p>
 * Input is accessible through {@link#getKeysPressed()} and
 * {@link#getResizes()}. These methods should both be called every frame to
 * ensure file reading/writing does not get desynchronized. These methods should
 * always be called in the same order.
 * 
 * @author Frank Kormann
 */
public class InputHandler extends KeyAdapter {

	public enum Input {
		UP(KeyEvent.VK_W, KeyEvent.VK_UP, KeyEvent.VK_SPACE),
		LEFT(KeyEvent.VK_A, KeyEvent.VK_LEFT), RIGHT(KeyEvent.VK_D, KeyEvent.VK_RIGHT);

		public final int[] keyCodes;

		private Input(int... keyCodes) {
			this.keyCodes = keyCodes;
		}
	}

	private Set<Integer> keysPressed;
	private Map<MainFrame.Direction, Integer> resizesSinceLastFrame;
	// Used during reading mode, null otherwise
	private BufferedReader reader;
	// Used during writing mode, null otherwise
	private BufferedWriter writer;

	public InputHandler() {
		keysPressed = new HashSet<>();
		resizesSinceLastFrame = new HashMap<>();
		zeroAllDirectionsInResizes();

		reader = null;
		writer = null;
	}

	private void zeroAllDirectionsInResizes() {
		for (MainFrame.Direction direction : MainFrame.Direction.values()) {
			resizesSinceLastFrame.put(direction, 0);
		}
	}

	/**
	 * Takes input from the file at {@code URL}. If this is already reading from a
	 * file, this method has no effect.
	 * 
	 * @param location {@code URL} of file to read from
	 */
	public void beginReading(URL location) {
		if (reader != null) {
			return;
		}
		try {
			reader = Files.newBufferedReader(new File(location.getFile()).toPath());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
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
	 * Starts writing input to the file at {@code URL}. The file must exist; it is
	 * not created if it does not exist. If this is already writing to a file, this
	 * method has no effect.
	 * <p>
	 * Any data previously in the file is overwritten.
	 * 
	 * @param location {@code URL} of file to write to
	 */
	public void beginWriting(URL location) {
		if (writer != null) {
			return;
		}
		try {
			writer = Files.newBufferedWriter(new File(location.getFile()).toPath());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
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
	 * Wraps {@code reader.read()} to deal with end-of-file and return a nicer
	 * value.
	 * 
	 * @return next value from global {@code reader} as signed {@code int}
	 * 
	 * @throws IOException
	 */
	private int read() throws IOException {
		int value = reader.read();
		reader.mark(1);
		if (reader.read() == -1) {
			endReading();
		}
		else {
			reader.reset();
		}
		return (int) (short) value;
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
	public Set<Input> getInputs() {
		Set<Input> inputs = EnumSet.noneOf(Input.class);

		if (reader == null) {
			for (Input inp : Input.values()) {
				for (int key : inp.keyCodes) {
					if (keysPressed.contains(key)) {
						inputs.add(inp);
						break;
					}
				}
			}
		}
		else {
			inputs = readInputsFromFile();
		}

		if (writer != null) {
			writeInputsToFile(inputs);
		}

		return inputs;
	}

	/**
	 * Returns the {@code Input}s pressed on this frame in the reading file.
	 */
	private Set<Input> readInputsFromFile() {
		Set<Input> inputs = EnumSet.noneOf(Input.class);

		try {
			int numberOfInputs = read();
			for (int i = 0; i < numberOfInputs; i++) {
				int inputOrdinal = read();
				inputs.add(Input.values()[inputOrdinal]);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return inputs;
	}

	/**
	 * First writes the number of {@code Input}s pressed this frame, then each
	 * {@code Input}'s ordinal in turn.
	 * 
	 * @param inputs {@code Set} of {@code Input}s to write
	 */
	private void writeInputsToFile(Set<Input> inputs) {
		try {
			writer.write(inputs.size());
			for (Input inp : inputs) {
				writer.write(inp.ordinal());
			}
		}
		catch (IOException e) {
			e.printStackTrace();
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
			try {
				// Make sure values are read in the correct order
				resizes.put(MainFrame.Direction.NORTH, read());
				resizes.put(MainFrame.Direction.SOUTH, read());
				resizes.put(MainFrame.Direction.WEST, read());
				resizes.put(MainFrame.Direction.EAST, read());
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (writer != null) {
			try {
				// Make sure values are written in the correct order
				writer.write(resizes.get(MainFrame.Direction.NORTH));
				writer.write(resizes.get(MainFrame.Direction.SOUTH));
				writer.write(resizes.get(MainFrame.Direction.WEST));
				writer.write(resizes.get(MainFrame.Direction.EAST));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
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
