package game;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes bytes and ints to an {@code OutputStream}. The format of the stream is
 * such that {@link NumberReader} can read it.
 * <p>
 * The method {@code flush} should be called before closing this to clear the
 * stream's buffer if it is buffered.
 * 
 * @see NumberReader
 * 
 * @author Frank Kormann
 */
public class NumberWriter {

	OutputStream stream;

	boolean isOpen;
	int zerosInARow;

	public NumberWriter(OutputStream stream) {
		this.stream = stream;
		isOpen = true;
		zerosInARow = 0;
	}

	public boolean isOpen() {
		return isOpen;
	}

	/**
	 * Writes a single {@code byte} to the output stream.
	 * <p>
	 * For {@code 0} specifically, instead of writing many {@code 0}s in a row,
	 * only the first {@code 0} is written. Then the number of following
	 * {@code 0}s is written.
	 * 
	 * @param b {@code byte} to write
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public void writeByte(int b) throws IOException {
		if (b == 0) {
			zerosInARow++;
		}
		if ((b != 0 && zerosInARow > 0) || zerosInARow > 0xFF) {
			stream.write(zerosInARow - 1);
			zerosInARow = 0;
		}
		if ((b == 0 && zerosInARow == 1) || b != 0) {
			stream.write(b);
		}
	}

	/**
	 * Writes a compressed {@code int} to the output stream.
	 * <p>
	 * First, {@code i} is split into four bytes. Any bytes which are entirely
	 * {@code 0} and do not have useful bytes above them are thrown out. Then,
	 * the number of remaining bytes is written and each byte is written in
	 * turn. If {@code i == 0}, only one byte (being 0) is written.
	 * <p>
	 * If {@code i} is negative, the byte indicating the number of bytes in
	 * {@code i} will be negative. Each byte of {@code i} represents {@code i}'s
	 * absolute value.
	 * 
	 * @param i {@code int} to write
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public void writeInt(int i) throws IOException {
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
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public void flush() throws IOException {
		if (zerosInARow > 0) {
			stream.write(zerosInARow - 1);
			zerosInARow = 0;
		}
		stream.flush();
	}

	/**
	 * Closes the {@code OutStream} this is based on. Subsequent calls to
	 * {@code isOpen} will return {@code false}.
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public void close() throws IOException {
		stream.close();
		isOpen = false;
	}

}
