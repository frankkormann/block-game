package blockgame.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import blockgame.gui.MainFrame.Direction;
import mocks.ResizableMock;

class ResizingSideTest {

	ResizableMock resizable;

	@BeforeEach
	void setUp() {
		resizable = new ResizableMock();
	}

	private void clickAndDrag(ResizingSide side, int x1, int y1, int x2,
			int y2) {
		// Use constructors with xAbs and yAbs because the dummy JLabel isn't
		// actually on screen
		side.mousePressed(new MouseEvent(new JLabel(), MouseEvent.MOUSE_CLICKED,
				1l, 0, x1, y1, x1, y1, 1, false, MouseEvent.NOBUTTON));
		side.mouseDragged(new MouseEvent(new JLabel(), MouseEvent.MOUSE_DRAGGED,
				1l, 0, x2, y2, x2, y2, 1, false, MouseEvent.NOBUTTON));
	}

	@Test
	void correct_change_for_north_and_south() {
		int change = 100;
		ResizingSide side1 = new ResizingSide(Direction.NORTH, resizable);
		ResizingSide side2 = new ResizingSide(Direction.SOUTH, resizable);
		clickAndDrag(side1, 0, 0, 50, change);
		clickAndDrag(side2, 0, 0, 50, change);

		assertEquals(change, resizable.resizes.get(Direction.NORTH));
		assertEquals(change, resizable.resizes.get(Direction.SOUTH));
	}

	@Test
	void correct_change_for_west_and_east() {
		int change = 82;
		ResizingSide side1 = new ResizingSide(Direction.WEST, resizable);
		ResizingSide side2 = new ResizingSide(Direction.EAST, resizable);
		clickAndDrag(side1, 0, 0, change, 40);
		clickAndDrag(side2, 0, 0, change, 40);

		assertEquals(change, resizable.resizes.get(Direction.WEST));
		assertEquals(change, resizable.resizes.get(Direction.EAST));
	}
}
