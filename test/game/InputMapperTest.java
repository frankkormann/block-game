package game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import game.GameInputHandler.DirectionSelectorInput;
import game.GameInputHandler.MovementInput;
import game.GameInputHandler.ResizingInput;
import game.MenuBar.MetaInput;

public class InputMapperTest {

	InputMapper mapper;

	@BeforeAll
	static void createSave(@TempDir Path dir) {
		SaveManager.setDirectory(dir.toString());
		new InputMapper();
	}

	@BeforeEach
	void setUp() {
		mapper = new InputMapper();
	}

	@Test
	void has_correct_enum_class() {
		assertEquals(4, mapper.getEnumClasses().length);
		assertEquals(MovementInput.class, mapper.getEnumClasses()[0]);
		assertEquals(DirectionSelectorInput.class, mapper.getEnumClasses()[1]);
		assertEquals(ResizingInput.class, mapper.getEnumClasses()[2]);
		assertEquals(MetaInput.class, mapper.getEnumClasses()[3]);
	}

	@Test
	void has_0_0_for_default_value() {
		assertEquals(new Pair<Integer, Integer>(0, 0),
				mapper.getDefaultValue());
	}

	@Test
	void can_set_by_keybind() {
		Pair<Integer, Integer> testKeybind = new Pair<>(5, 10);
		mapper.set(MovementInput.UP, testKeybind.first, testKeybind.second);

		assertEquals(testKeybind, mapper.get(MovementInput.UP));
	}
}
