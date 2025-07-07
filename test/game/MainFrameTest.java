package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import game.MainFrame.Direction;

class MainFrameTest {

	MainFrame mainFrame;
	Level level;

	int initialX, initialY, initialWidth, initialHeight;

	@BeforeEach
	void setUp() {
		GameInputHandler inputHandler = new GameInputHandler();
		mainFrame = new MainFrame(inputHandler);

		level = new Level();
		level.width = 800;
		level.height = 500;
		mainFrame.setUpLevel(level);
		mainFrame.arrangeComponents();
	}

	@Test
	void has_correct_width_and_height_from_level() {
		assertEquals(level.width, mainFrame.getWidth(), 100);   // +/- 100 to allow for
		assertEquals(level.height, mainFrame.getHeight(), 100); // insets, non-level
																 // components, etc.
	}

	private void setInitialValues() {
		initialWidth = mainFrame.getWidth();
		initialHeight = mainFrame.getHeight();
		initialX = mainFrame.getX();
		initialY = mainFrame.getY();
	}

	@Test
	void width_and_height_are_as_expected_after_resize() {
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
	void position_is_as_expected_after_move2() {
		setInitialValues();
		int xChange = 80;
		int yChange = -23;

		mainFrame.move2(xChange, yChange);

		assertEquals(initialX + xChange, mainFrame.getX());
		assertEquals(initialY + yChange, mainFrame.getY());
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
}
