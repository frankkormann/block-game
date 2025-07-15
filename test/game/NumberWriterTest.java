package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NumberWriterTest {

	NumberWriter writer;
	ByteArrayOutputStream stream;

	@BeforeEach()
	void setUp() {
		stream = new ByteArrayOutputStream();
		writer = new NumberWriter(stream);
	}

	@Test
	void is_open_initially() {
		assertTrue(writer.isOpen);
	}

	@Test
	void is_not_open_after_being_closed() throws IOException {
		writer.close();

		assertFalse(writer.isOpen);
	}

	@Test
	void the_correct_byte_is_written_for_positive_bytes() throws IOException {
		for (int i = 0; i < 100; i++) {
			byte b = (byte) (Math.random() * Byte.MAX_VALUE + 1);
			writer.writeByte(b);

			assertEquals(b, stream.toByteArray()[i]);
		}
	}

	@Test
	void the_correct_byte_is_written_for_negative_bytes() throws IOException {
		for (int i = 0; i < 100; i++) {
			byte b = (byte) -(Math.random() * Byte.MAX_VALUE + 1);
			writer.writeByte(b);

			assertEquals(b, stream.toByteArray()[i]);
		}
	}

	@Test
	void a_single_zero_then_the_number_of_zeros_is_written() throws IOException {
		int numZeros = 50;
		for (int i = 0; i < numZeros; i++) {
			writer.writeByte(0);
		}
		writer.flush();

		assertEquals(0, stream.toByteArray()[0]);
		assertEquals(numZeros - 1, stream.toByteArray()[1]);
	}

	@Test
	void only_the_nonzero_bytes_of_an_int_and_the_number_of_these_bytes_are_written()
			throws IOException {
		int i = 0b1011100110011011;
		writer.writeInt(i);

		byte[] bytes = stream.toByteArray();
		assertEquals(2, bytes[0]);
		assertEquals((byte) 0b10011011, bytes[1]);
		assertEquals((byte) 0b10111001, bytes[2]);
	}

	@Test
	void the_number_of_bytes_is_negative_to_indicate_a_negative_int()
			throws IOException {
		int i = -0b1011100110011011;
		writer.writeInt(i);

		byte[] bytes = stream.toByteArray();
		assertEquals(-2, bytes[0]);
		assertEquals((byte) 0b10011011, bytes[1]);
		assertEquals((byte) 0b10111001, bytes[2]);
	}
}
