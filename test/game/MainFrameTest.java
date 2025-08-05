package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.formdev.flatlaf.FlatLightLaf;

import game.MainFrame.Direction;
import game.ParameterMapper.Parameter;

// These tests will skip if in a headless environment (an environment that doesn't
// support keyboard, display, etc.)
class MainFrameTest {

	MainFrame mainFrame;
	Level level;
	ParameterMapper paramMapper;

	int initialX, initialY, initialWidth, initialHeight;

	@BeforeAll
	static void setUpLaF() {
		FlatLightLaf.setup();
	}

	@BeforeEach
	void setUp(@TempDir Path dir) {
		assumeFalse(GraphicsEnvironment.isHeadless());
		SaveManager.setDirectory(dir.toString());
		paramMapper = new ParameterMapper();

		GameInputHandler inputHandler = new GameInputHandler(new InputMapper(),
				paramMapper);
		mainFrame = new MainFrame(inputHandler, paramMapper);

		level = new Level();
		level.width = 800;
		level.height = 500;
		mainFrame.setUpLevel(level);
	}

	@Test
	void has_correct_width_and_height_from_level() {
		assertEquals(level.width, mainFrame.getNextWidth());
		assertEquals(level.height, mainFrame.getNextHeight());
	}

	private void setInitialValues() {
		initialWidth = mainFrame.getNextWidth();
		initialHeight = mainFrame.getNextHeight();
		initialX = mainFrame.getNextXOffset();
		initialY = mainFrame.getNextYOffset();
	}

	@Test
	void width_and_height_are_as_expected_after_resize() {
		setInitialValues();
		int eastChange = 100;
		int southChange = 50;

		mainFrame.resize(eastChange, Direction.EAST);
		mainFrame.resize(southChange, Direction.SOUTH);
		mainFrame.incorporateChanges();

		assertEquals(initialWidth + eastChange, mainFrame.getNextWidth());
		assertEquals(initialHeight + southChange, mainFrame.getNextHeight());
	}

	@Test
	void changes_to_north_or_east_also_change_position() {
		setInitialValues();
		int northChange = 100;
		int westChange = 50;

		mainFrame.resize(-northChange, Direction.NORTH);
		mainFrame.resize(-westChange, Direction.WEST);
		mainFrame.incorporateChanges();

		assertEquals(initialWidth + westChange, mainFrame.getNextWidth());
		assertEquals(initialHeight + northChange, mainFrame.getNextHeight());
		assertEquals(initialX - westChange, mainFrame.getNextXOffset());
		assertEquals(initialY - northChange, mainFrame.getNextYOffset());
	}

	@Test
	void resizingSides_are_on_correct_sides() {
		int middleX = mainFrame.getX() + mainFrame.getWidth() / 2;
		int middleY = mainFrame.getY() + mainFrame.getHeight() / 2;

		for (Component comp : mainFrame.getLayeredPane().getComponents()) {
			if (comp instanceof ResizingSide) {
				switch (((ResizingSide) comp).getDirection()) {
					case NORTH:
						assertTrue(comp.getY() < middleY);
						break;
					case SOUTH:
						assertTrue(comp.getY() > middleY);
						break;
					case WEST:
						assertTrue(comp.getX() < middleX);
						break;
					case EAST:
						assertTrue(comp.getX() > middleX);
						break;
				}
			}
		}
	}

	@Test
	void size_changes_correctly_when_scale_changes() {
		initialWidth = mainFrame.getWidth();
		paramMapper.set(Parameter.GAME_SCALING, 0.5);

		assertEquals(initialWidth / 2, mainFrame.getWidth(), 50);
	}

	@Test
	void next_values_dont_change_with_scale() {
		setInitialValues();
		paramMapper.set(Parameter.GAME_SCALING, 0.5);

		assertEquals(initialX, mainFrame.getNextXOffset());
		assertEquals(initialY, mainFrame.getNextYOffset());
		assertEquals(initialWidth, mainFrame.getNextWidth());
		assertEquals(initialHeight, mainFrame.getNextHeight());
	}

	@Test
	void offset_values_dont_change_when_moved() {
		setInitialValues();
		mainFrame.setLocation(mainFrame.getX() + 50, mainFrame.getY() + 100);

		assertEquals(initialX, mainFrame.getNextXOffset());
		assertEquals(initialY, mainFrame.getNextYOffset());
	}
}
