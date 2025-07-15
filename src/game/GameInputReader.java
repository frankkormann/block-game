package game;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reads bytes and ints from an {@code InputStream}. The format of the stream
 * should be that which is written by {@link GameInputWriter}.
 * <p>
 * When end-of-stream is detected, the stream is automatically closed.
 * {@code isOpen} should be checked before any reads.
 * 
 * @see GameInputWriter
 * 
 * @author Frank Kormann
 */
public class GameInputReader {

	InputStream stream;

	boolean isOpen;
	int zerosInARow;
	int nextByte;

	/**
	 * Creates a {@code GameInputReader} which reads from {@code stream}.
	 * 
	 * @param stream {@code InputStream} to read from
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public GameInputReader(InputStream stream) throws IOException {
		this.stream = stream;
		isOpen = true;
		zerosInARow = 0;
		nextByte = stream.read();
	}

	public boolean isOpen() {
		return isOpen;
	}

	/**
	 * Reads the next {@code byte} in the input stream and returns it as an
	 * {@code int}.
	 * <p>
	 * Follows the format defined by {@link GameInputWriter#writeByte(int)}.
	 * <p>
	 * Closes the input stream if end-of-input is detected.
	 * 
	 * @return next {@code byte} as {@code int}
	 */
	public int readByte() throws IOException {
		int value;

		if (zerosInARow > 0) {
			zerosInARow--;
			value = 0;
		}
		else {

			if (nextByte == 0) {
				zerosInARow = stream.read();
			}

			value = nextByte;

			nextByte = stream.read();

		}

		if (nextByte == -1 && zerosInARow == 0) {
			close();
		}

		return value;
	}

	/**
	 * Interprets the next bytes as an {@code int} written by
	 * {@link GameInputWriter#writeInt(int)}.
	 * <p>
	 * Closes the input stream if end-of-input is detected.
	 * 
	 * @return next {@code int}
	 */
	public int readInt() throws IOException {
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
	 * Closes the {@code InputStream} this is based on. Subsequent calls to
	 * {@code isOpen} will return {@code false}.
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public void close() throws IOException {
		stream.close();
		isOpen = false;
	}

}
