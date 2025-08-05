package blockgame.input;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import blockgame.SaveManager;
import blockgame.input.ColorMapper;
import blockgame.physics.Rectangle.Colors;

public class ColorMapperTest {

	ColorMapper mapper;

	@BeforeAll
	static void createSave(@TempDir Path dir) {
		SaveManager.setDirectory(dir.toString());
		new ColorMapper();
	}

	@BeforeEach
	void setUp() {
		mapper = new ColorMapper();
	}

	@Test
	void has_correct_enum_class() {
		assertEquals(1, mapper.getEnumClasses().length);
		assertEquals(Colors.class, mapper.getEnumClasses()[0]);
	}

	@Test
	void has_0_0_0_for_default_value() {
		assertEquals(new Color(0, 0, 0).getRGB(), mapper.getDefaultValue());
	}

	@Test
	void can_set_color_by_Color() {
		Color testColor = new Color(1, 2, 3);
		mapper.setColor(Colors.BLACK, testColor);

		assertEquals(testColor.getRGB(), mapper.get(Colors.BLACK));
	}

	@Test
	void can_get_color_as_Color() {
		Color expected = new Color(mapper.get(Colors.BLUE));
		Color actual = mapper.getColor(Colors.BLUE);

		assertEquals(expected, actual);
	}
}
