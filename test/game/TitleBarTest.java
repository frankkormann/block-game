package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mocks.MovableMock;
import mocks.WindowEventReporterFrame;

//These tests will skip if in a headless environment (an environment that doesn't
//support keyboard, display, etc.)
class TitleBarTest {

	TitleBar titleBar;
	MovableMock movable;
	WindowEventReporterFrame dummyFrame;

	@BeforeEach
	void setUp() {
		assumeFalse(GraphicsEnvironment.isHeadless());

		movable = new MovableMock();
		dummyFrame = new WindowEventReporterFrame();
		titleBar = new TitleBar(movable, dummyFrame);
	}

	private void clickAndDrag(int x1, int y1, int x2, int y2) {
		// Use constructors with xAbs and yAbs because the dummy JLabel isn't actually
		// on screen
		titleBar.mousePressed(new MouseEvent(new JLabel(), MouseEvent.MOUSE_CLICKED, 1l,
				0, x1, y1, x1, y1, 1, false, MouseEvent.NOBUTTON));
		titleBar.mouseDragged(new MouseEvent(new JLabel(), MouseEvent.MOUSE_DRAGGED, 1l,
				0, x2, y2, x2, y2, 1, false, MouseEvent.NOBUTTON));
	}

	@Test
	void correct_movement_based_on_mouse_dragging() {
		int xChange = 600;
		int yChange = 50;
		clickAndDrag(0, 0, xChange, yChange);

		assertEquals(xChange, movable.totalXChange);
		assertEquals(yChange, movable.totalYChange);
	}

	@Test
	void negative_mouse_dragging() {
		int xChange = -60;
		int yChange = -500;
		clickAndDrag(0, 0, xChange, yChange);

		assertEquals(xChange, movable.totalXChange);
		assertEquals(yChange, movable.totalYChange);
	}

	@Test
	void can_close_frame() {
		titleBar.actionPerformed(
				new ActionEvent(titleBar, ActionEvent.ACTION_PERFORMED, "exit"));

		assertEquals(WindowEvent.WINDOW_CLOSING, dummyFrame.lastEventReceived);
	}

	@Test
	void can_minimize_frame() {
		titleBar.actionPerformed(
				new ActionEvent(titleBar, ActionEvent.ACTION_PERFORMED, "minimize"));

		assertEquals(JFrame.ICONIFIED, dummyFrame.getState());
	}
}
