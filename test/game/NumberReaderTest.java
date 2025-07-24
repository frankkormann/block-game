package game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

class NumberReaderTest {

	private InputStream fromWriter(byte[] bytes) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		NumberWriter writer = new NumberWriter(outputStream);

		for (byte b : bytes) {
			writer.writeByte(b);
		}
		writer.flush();

		InputStream inputStream = new ByteArrayInputStream(
				outputStream.toByteArray());

		return inputStream;
	}

	@Test
	void can_read_bytes() throws IOException {
		byte[] bytes = { 1, 2, 3, 4, 5, 127 };

		InputStream stream = fromWriter(bytes);
		NumberReader reader = new NumberReader(stream);

		for (byte b : bytes) {
			assertEquals(b, reader.readByte());
		}
	}

	private InputStream fromWriter(int[] ints) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		NumberWriter writer = new NumberWriter(outputStream);

		for (int i : ints) {
			writer.writeInt(i);
		}
		writer.flush();

		InputStream inputStream = new ByteArrayInputStream(
				outputStream.toByteArray());

		return inputStream;
	}

	@Test
	void can_read_ints() throws IOException {
		int[] ints = { 12, 76294, 3742, 482972, 34 };

		InputStream stream = fromWriter(ints);
		NumberReader reader = new NumberReader(stream);

		for (int i : ints) {
			assertEquals(i, reader.readInt());
		}
	}

	@Test
	void can_read_negative_ints() throws IOException {
		int[] ints = { -42742, -9743, -123, -874, -413741 };

		InputStream stream = fromWriter(ints);
		NumberReader reader = new NumberReader(stream);

		for (int i : ints) {
			assertEquals(i, reader.readInt());
		}
	}

	@Test
	void can_read_many_zeros_in_a_row() throws IOException {
		byte[] zeros = new byte[50];
		for (int i = 0; i < zeros.length; i++) {
			zeros[i] = 0;
		}

		InputStream stream = fromWriter(zeros);
		NumberReader reader = new NumberReader(stream);

		for (int i = 0; i < zeros.length; i++) {
			assertEquals(0, reader.readByte());
		}
	}
}
