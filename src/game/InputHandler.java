package game;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Get inputs from the user or a file. Optionally write inputs to a file.
 * <p>
 * By default, inputs will be taken from the user. Key presses will be taken
 * from the keyboard and window resizes will be taken from calls to resize.
 * These calls are provided by the {@code ResizingSide} class.
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
 * <p>
 * 
 * @author Frank Kormann
 */

public class InputHandler extends KeyAdapter {

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

	public void beginWriting(URL location) {
		if (writer != null) {
			return;
		}
		try {
			File file = new File(location.getFile());
			file.delete();
			file.createNewFile();
			writer = Files.newBufferedWriter(file.toPath());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

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

	// Wrap read() to deal with end-of-file and cast to signed int
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
	 * Get the keys pressed on this frame. If in reading mode, these will be read
	 * from the reading file. Otherwise, they will be taken from the keyboard. If in
	 * writing mode, these will also be written to the writing file.
	 * 
	 * @return {@code Set} of KeyCodes
	 */
	public Set<Integer> getKeysPressed() {
		Set<Integer> keys = new HashSet<>();
		if (reader == null) {
			keys = keysPressed;
		}
		else {
			keys = readKeysFromFile();
		}
		if (writer != null) {
			writeKeysToFile(keys);
		}
		return keys;
	}

	private Set<Integer> readKeysFromFile() {
		Set<Integer> keys = new HashSet<>();
		try {
			while (true) {
				int next = read();
				if (next == 0) {
					break;
				}
				keys.add(next);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return keys;
	}

	private void writeKeysToFile(Set<Integer> keyCodes) {
		try {
			for (int key : keyCodes) {
				writer.write(key);
			}
			writer.write(0);
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
				read();
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
				writer.write(0);
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
