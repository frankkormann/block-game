package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.Component;
import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.formdev.flatlaf.FlatLightLaf;

import game.MainFrame.Direction;

// These tests will skip if in a headless environment (an environment that doesn't
// support keyboard, display, etc.)
class MainFrameTest {

	MainFrame mainFrame;
	Level level;

	int initialX, initialY, initialWidth, initialHeight;

	@BeforeAll
	static void setUpLaF() {
		FlatLightLaf.setup();
	}

	@BeforeEach
	void setUp() {
		assumeFalse(GraphicsEnvironment.isHeadless());
		SaveManager.setDirectory(System.getProperty("java.io.tmpdir"));

		GameInputHandler inputHandler = new GameInputHandler(new InputMapper());
		mainFrame = new MainFrame(inputHandler);

		level = new Level();
		level.width = 800;
		level.height = 500;
		mainFrame.setUpLevel(level);
	}

	@Test
	void has_correct_width_and_height_from_level() {
		assumeFalse(GraphicsEnvironment.isHeadless());

		assertEquals(level.width, mainFrame.getWidth(), 100);   // +/- 100 to
																   // allow for
		assertEquals(level.height, mainFrame.getHeight(), 100); // insets,
																 // non-level
																 // components,
																 // etc.
	}

	private void setInitialValues() {
		initialWidth = mainFrame.getWidth();
		initialHeight = mainFrame.getHeight();
		initialX = mainFrame.getX();
		initialY = mainFrame.getY();
	}

	@Test
	void width_and_height_are_as_expected_after_resize() {
		assumeFalse(GraphicsEnvironment.isHeadless());

		setInitialValues();
		int eastChange = 100;
		int southChange = 50;

		mainFrame.resize(eastChange, Direction.EAST);
		mainFrame.resize(southChange, Direction.SOUTH);
		mainFrame.incorporateChanges();

		assertEquals(initialWidth + eastChange, mainFrame.getWidth());
		assertEquals(initialHeight + southChange, mainFrame.getHeight());
	}

	@Test
	void changes_to_north_or_east_also_change_position() {
		assumeFalse(GraphicsEnvironment.isHeadless());

		setInitialValues();
		int northChange = 100;
		int westChange = 50;

		mainFrame.resize(-northChange, Direction.NORTH);
		mainFrame.resize(-westChange, Direction.WEST);
		mainFrame.incorporateChanges();

		assertEquals(initialWidth + westChange, mainFrame.getWidth());
		assertEquals(initialHeight + northChange, mainFrame.getHeight());
		assertEquals(initialX - westChange, mainFrame.getX());
		assertEquals(initialY - northChange, mainFrame.getY());
	}

	@Test
	void resizingSides_are_on_correct_sides() {
		assumeFalse(GraphicsEnvironment.isHeadless());

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
}
