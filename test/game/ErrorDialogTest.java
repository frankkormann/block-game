package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

// These tests will skip if in a headless environment (an environment that doesn't
// support keyboard, display, etc.)
class ErrorDialogTest {

	private static final String TITLE = "This is the title";
	private static final String MESSAGE = "This is the message";

	ErrorDialog errorDialog;
	Exception error;

	@BeforeEach
	void setUp() {
		assumeFalse(GraphicsEnvironment.isHeadless());

		try {
			int x = 5 / 0;
		}
		catch (ArithmeticException e) {
			error = e;
			errorDialog = new ErrorDialog(TITLE, MESSAGE, e);
		}
	}

	@Test
	void has_correct_title() {
		assertEquals(TITLE, errorDialog.getTitle());
	}

	@Nested
	class InnerComponents {
		// @formatter:off
		/*
		 * The expected layout is:
		 * - JTextArea containing the message and error.toString()
		 * - JPanel containing:
		 *     - JButton that reveals a JScrollPane ("Details")
		 *     - JButton that disposes of the dialog ("OK")
		 *     - JButton that copies stack trace to clipboard ("Copy")
		 * - JScrollPane, not visible initially, containing:
		 *     - JTextArea containing the error's stack trace
		 */
		// @formatter:on

		JTextArea messageArea;
		JPanel buttonPanel;
		JScrollPane stackTracePane;

		@BeforeEach
		void findTopComponents() {
			for (Component comp : errorDialog.getContentPane()
					.getComponents()) {
				if (comp instanceof JTextArea) {
					messageArea = (JTextArea) comp;
				}
				else if (comp instanceof JPanel) {
					buttonPanel = (JPanel) comp;
				}
				else if (comp instanceof JScrollPane) {
					stackTracePane = (JScrollPane) comp;
				}
			}
		}

		@Test
		void message_area_contains_correct_message() {
			assertEquals(MESSAGE + "\n\n" + error.toString(),
					messageArea.getText());
		}

		@Test
		void button_panel_contains_three_buttons() {
			assertEquals(3, buttonPanel.getComponents().length);

			for (Component comp : buttonPanel.getComponents()) {
				assertTrue(comp instanceof JButton);
			}
		}

		@Test
		void stack_trace_scroll_pane_is_not_visible() {
			assertFalse(stackTracePane.isVisible());
		}

		@Nested
		class Buttons {

			JButton detailsButton;
			JButton disposeButton;
			JButton copyButton;

			@BeforeEach
			void findButtons() {
				for (Component comp : buttonPanel.getComponents()) {
					JButton button = (JButton) comp;
					switch (button.getText()) {
						case "Details":
							detailsButton = button;
							break;
						case "OK":
							disposeButton = button;
							break;
						case "Copy":
							copyButton = button;
							break;
					}
				}
			}

			@Test
			void details_button_shows_stack_trace_scroll_pane() {
				detailsButton.doClick();

				assertTrue(stackTracePane.isVisible());
			}

			@Test
			void dispose_button_closes_dialog() {
				disposeButton.doClick();

				assertFalse(errorDialog.isDisplayable());
			}

			@Test
			void copy_button_copies_stack_trace_to_clipboard()
					throws UnsupportedFlavorException, IOException {
				copyButton.doClick();

				String actualText = (String) Toolkit.getDefaultToolkit()
						.getSystemClipboard()
						.getContents(null)
						.getTransferData(DataFlavor.stringFlavor);

				assertEquals(getExpectedStackTrace(), actualText);
			}
		}

		@Nested
		class StackTraceArea {
			JTextArea stackTraceArea;

			@BeforeEach
			void findStackTraceArea() {
				stackTraceArea = (JTextArea) ((JViewport) stackTracePane
						.getComponent(0)).getComponent(0);
			}

			@Test
			void contains_stack_trace() {
				assertEquals(getExpectedStackTrace(), stackTraceArea.getText());
			}
		}
	}

	private String getExpectedStackTrace() {
		String expectedStackTrace = error.toString() + "\n\n";
		for (StackTraceElement elem : error.getStackTrace()) {
			expectedStackTrace += elem + "\n";
		}
		return expectedStackTrace;
	}

}
