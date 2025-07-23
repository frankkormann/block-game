package game;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Dialog to display an error message and stack trace information.
 * 
 * @author Frank Kormann
 */
public class ErrorDialog extends JDialog {

	/**
	 * Creates an {@code ErrorDialog} for {@code err} and displays it.
	 * <p>
	 * {@code message} and {@code err}'s short form are shown in the dialog's
	 * body, and {@code err}'s full stack trace is hidden behind a "Details"
	 * button.
	 * 
	 * @param title   {@code String} to display in the title bar
	 * @param message short error message to display in the dialog body
	 * @param err     {@code Exception} to extract stack trace information from
	 */
	public ErrorDialog(String title, String message, Exception err) {
		super((Frame) null, title, true);

		JPanel panelContentPane = new JPanel(); // Ensure that content pane is a
		setContentPane(panelContentPane);	// JPanel so it can have a border
		panelContentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		JTextArea messageArea = createTextArea(message + "\n\n" + err, 1, 50);
		messageArea.setLineWrap(true);
		setMaximumSizeHeight(messageArea,
				messageArea.getPreferredSize().height);

		JTextArea stackTraceArea = createTextArea(
				err.toString() + "\n\n" + buildStackTrace(err), 20, 50);
		JScrollPane scrollPane = new JScrollPane(stackTraceArea);
		scrollPane.setMaximumSize(
				new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		scrollPane.setVisible(false);

		JPanel buttonPanel = createButtonsPanel("Details", "OK", "Copy",
				scrollPane, stackTraceArea);
		buttonPanel.setPreferredSize(
				new Dimension(scrollPane.getPreferredSize().width,
						buttonPanel.getPreferredSize().height));
		setMaximumSizeHeight(buttonPanel,
				buttonPanel.getPreferredSize().height);

		setAlignmentForAll(LEFT_ALIGNMENT, messageArea, buttonPanel,
				scrollPane);

		add(messageArea);
		add(buttonPanel);
		add(scrollPane);

		messageArea.setSize(messageArea.getPreferredSize());  // Fix issue where
		pack();												  // wrapped line
		setLocationRelativeTo(null);						  // height is not
															  // taken into
															  // account by
															  // pack()
	}

	/**
	 * Creates a {@code JPanel} with a {@code JButton} to toggle visibility of a
	 * component and a {@code JButton} to dispose of this.
	 * 
	 * @param toggleButtonLabel
	 * @param toToggle
	 * @param disposeButtonLabel
	 * 
	 * @return the built {@code JPanel}
	 */
	private JPanel createButtonsPanel(String toggleButtonLabel,
			String disposeButtonLabel, String copyButtonLabel,
			Component toToggle, JTextArea toCopy) {
		JPanel panel = new JPanel();

		JButton toggleButton = createButton(toggleButtonLabel, e -> {
			toToggle.setVisible(!toToggle.isVisible());
			pack();
		});
		JButton disposeButton = createButton(disposeButtonLabel,
				e -> dispose());

		JButton copyButton = createButton(copyButtonLabel, e -> {
			toCopy.selectAll();
			toCopy.copy();
		});

		panel.add(toggleButton);
		panel.add(disposeButton);
		panel.add(copyButton);

		return panel;
	}

	/**
	 * Creates a {@code JScrollPane} which contains a {@code JTextArea} with the
	 * specified {@code content}, {@code rows}, and {@code columns}.
	 * <p>
	 * The {@code JTextArea} is set non-editable.
	 * 
	 * @param content {@code String} of text to display
	 * @param rows    number of rows
	 * @param columns number of columns
	 * 
	 * @return
	 */
	private JTextArea createTextArea(String content, int rows, int columns) {
		JTextArea area = new JTextArea(rows, columns);

		area.setText(content);
		area.setEditable(false);
		area.setBackground(Color.WHITE);

		return area;
	}

	/**
	 * Creates a {@code JButton} with the specified {@code label} and
	 * {@code ActionListener}.
	 * 
	 * @param label    {@code String} to put on the button
	 * @param listener {@code ActionListener} to register
	 * 
	 * @return the built {@code JButton}
	 */
	private JButton createButton(String label, ActionListener listener) {
		JButton button = new JButton(label);
		button.addActionListener(listener);
		return button;
	}

	/**
	 * Assembles a {@code String} containing {@code err}'s stack trace. Each
	 * element of the trace is on a new line.
	 * 
	 * @param err {@code Exception} to take stack trace from
	 * @return multi-line {@code String} representation of {@code err}'s stack
	 *         trace
	 */
	private String buildStackTrace(Exception err) {
		String trace = "";
		for (StackTraceElement elem : err.getStackTrace()) {
			trace += elem + "\n";
		}
		return trace;
	}

	/**
	 * Sets only the height of {@code comp}'s maximum size.
	 * 
	 * @param comp   {@code Component} to alter
	 * @param height new maximum height
	 */
	private void setMaximumSizeHeight(Component comp, int height) {
		comp.setMaximumSize(new Dimension(comp.getMaximumSize().width, height));
	}

	/**
	 * Sets {@link JComponent#setAlignmentX(float)} to {@code alignment} for
	 * {@code components}.
	 * 
	 * @param alignment  value to set
	 * @param components {@code JComponent}s to affect
	 */
	private void setAlignmentForAll(float alignment, JComponent... components) {
		for (JComponent comp : components) {
			comp.setAlignmentX(alignment);
		}
	}

}
