package game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import game.ParameterMapper.Parameter;
import game.Rectangle.Colors;
import mocks.IntChangeListener;
import mocks.IntMapper;

public class MapperTest {

	private static final String SAVE_PATH = "/save";
	private static final String DEFAULT_RESOURCE = "/colors_default.json";

	Mapper<Integer> mapper;

	@BeforeEach
	void setUp(@TempDir Path dir) {
		SaveManager.setDirectory(dir.toString());
		mapper = new IntMapper(SAVE_PATH, DEFAULT_RESOURCE);
	}

	@Test
	void creates_new_save_file_if_none_exists() {
		InputStream saveFile = SaveManager.readFile(SAVE_PATH);
		assertNotNull(saveFile);
		try {
			saveFile.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void reads_from_save_file_when_instantiated() throws IOException {
		// Create fake savedata
		BufferedWriter saveWriter = new BufferedWriter(
				new OutputStreamWriter(SaveManager.writeFile(SAVE_PATH)));
		saveWriter.write(
				"{\"values\":{\"TRANSPARENT\": 0,\"DARK_GRAY\": 1,\"GREEN\": 2,\"BLUE\": 3,\"BLACK\": 4,\"GRAY\": 5,\"TRANSLUCENT_BLUE\": 6,\"PLAYER\": 7,\"TRANSLUCENT_RED\": 8,\"GRAY\": 9,\"ORANGE\": 10,\"TRANSLUCENT_GREEN\": 11,\"TRANSLUCENT_YELLOW\": 12,\"TRANSLUCENT_PINK\": 13,\"RED\": 14}}");
		saveWriter.close();

		mapper = new IntMapper(SAVE_PATH, DEFAULT_RESOURCE);

		assertEquals(3, (int) mapper.get(Colors.BLUE));
	}

	@Test
	void returns_null_for_nonexistent_key() {
		assertNull(mapper.get(Parameter.GAME_SCALING));
	}

	@Test
	void can_set_value() {
		mapper.set(Colors.ORANGE, 76);

		assertEquals(76, (int) mapper.get(Colors.ORANGE));
	}

	@Test
	void returns_null_when_value_is_removed() {
		mapper.remove(Colors.BLACK);

		assertNull(mapper.get(Colors.BLACK));
	}

	@Test
	void reads_from_resource_for_setToDefaults() {
		mapper.setToDefaults();

		assertEquals(-11184811, (int) mapper.get(Colors.DARK_GRAY));
	}

	@Test
	void doesnt_save_with_reload() {
		int originalValue = mapper.get(Colors.GREEN);
		mapper.set(Colors.GREEN, 200);
		mapper.reload();

		assertEquals(originalValue, (int) mapper.get(Colors.GREEN));
	}

	@Test
	void can_save() {
		mapper.set(Colors.BLACK, 74);
		mapper.save();
		mapper = new IntMapper(SAVE_PATH, DEFAULT_RESOURCE);

		assertEquals(74, (int) mapper.get(Colors.BLACK));
	}

	@Test
	void reads_from_resource_even_after_saving() {
		mapper.set(Colors.BLACK, 74);
		mapper.save();

		mapper = new IntMapper(SAVE_PATH, DEFAULT_RESOURCE);
		mapper.setToDefaults();

		assertEquals(-16777216, (int) mapper.get(Colors.BLACK));
	}

	@Nested
	class WithChangeListener {
		IntChangeListener listener;

		@BeforeEach
		void setUp() {
			listener = new IntChangeListener();
			mapper.addListener(listener);
		}

		@Test
		void change_listener_receives_valueChanged_events() {
			mapper.set(Colors.TRANSLUCENT_PINK, 10);

			assertEquals("valueChanged", listener.lastEvent);
			assertEquals(Colors.TRANSLUCENT_PINK, listener.eventInfo.first);
			assertEquals(10, (int) listener.eventInfo.second);
		}

		@Test
		void change_listener_receives_valueRemoved_events() {
			mapper.remove(Colors.TRANSLUCENT_YELLOW);

			assertEquals("valueRemoved", listener.lastEvent);
		}

		@Test
		void listener_is_removed_properly() {
			mapper.removeListener(listener);
			mapper.set(Colors.TRANSLUCENT_RED, 15);

			assertEquals("", listener.lastEvent);
		}

	}

}
