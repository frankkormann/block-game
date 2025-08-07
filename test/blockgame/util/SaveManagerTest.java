package blockgame.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SaveManagerTest {

	@BeforeEach
	void setUp(@TempDir Path dir) {
		SaveManager.setDirectory(dir.toString());
	}

	@Test
	void can_write_a_new_file_and_read_it() throws IOException {
		OutputStream out = SaveManager.writeFile("/test");
		byte[] content = new byte[] { 1, 2, 3, 4, 5 };
		out.write(content);
		out.close();

		InputStream in = SaveManager.readFile("/test");
		for (byte b : content) {
			assertEquals(b, in.read());
		}
		in.close();
	}

	@Test
	void can_put_a_value_and_get_it() {
		String name = "foo";
		String value = "bar";
		SaveManager.putValue(name, value);

		assertEquals(value, SaveManager.getValue(name, ""));
	}

	@Test
	void returns_default_value_if_value_doesnt_exist() {
		assertEquals("potato", SaveManager.getValue("I'm not here", "potato"));
	}
}
