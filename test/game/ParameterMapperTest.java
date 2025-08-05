package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import game.ParameterMapper.Parameter;

public class ParameterMapperTest {

	ParameterMapper mapper;

	@BeforeAll
	static void createSave(@TempDir Path dir) {
		SaveManager.setDirectory(dir.toString());
		new ParameterMapper();
	}

	@BeforeEach
	void setUp() {
		mapper = new ParameterMapper();
	}

	@Test
	void has_correct_enum_class() {
		assertEquals(1, mapper.getEnumClasses().length);
		assertEquals(Parameter.class, mapper.getEnumClasses()[0]);
	}

	@Test
	void has_0_for_default_value() {
		assertEquals(0, mapper.getDefaultValue());
	}

	@Test
	void can_get_as_int() {
		assertInstanceOf(Integer.class,
				mapper.get(Parameter.KEYBOARD_RESIZING_AMOUNT));
	}

	@Test
	void can_get_as_float() {
		assertInstanceOf(Double.class,  // It actually returns a double
				mapper.get(Parameter.OPACITY_MULTIPLIER)); // (close enough)
	}
}
