package game;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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

	public ErrorDialog(String title, String message, Exception err) {
		super((Frame) null, title, true);

		try {
			setIconImage(ImageIO.read(getClass().getResource(MainFrame.TASKBAR_ICON)));
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JTextArea messageArea = new JTextArea(message + "\n\n" + err, 4, 50);
		messageArea.setLineWrap(true);
		messageArea.setEditable(false);

		JScrollPane scrollPane = createTextScrollPane(buildStackTrace(err), 20, 50);
		scrollPane.setVisible(false);

		JPanel buttonPanel = createButtonsPanel("Details", scrollPane, "OK");
		buttonPanel.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width,
				buttonPanel.getPreferredSize().height));

		messageArea.setAlignmentX(LEFT_ALIGNMENT);
		buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
		scrollPane.setAlignmentX(LEFT_ALIGNMENT);

		add(messageArea);
		add(buttonPanel);
		add(scrollPane);

		pack();
		setLocationRelativeTo(null);
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
	private JPanel createButtonsPanel(String toggleButtonLabel, Component toToggle,
			String disposeButtonLabel) {
		JPanel panel = new JPanel();

		JButton toggleButton = createButton(toggleButtonLabel, e -> {
			toToggle.setVisible(!toToggle.isVisible());
			pack();
		});
		JButton disposeButton = createButton(disposeButtonLabel, e -> dispose());

		panel.add(toggleButton);
		panel.add(disposeButton);

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
	private JScrollPane createTextScrollPane(String content, int rows, int columns) {
		JTextArea area = new JTextArea(rows, columns);

		area.setText(content);
		area.setEditable(false);
		area.setBackground(Color.WHITE);

		JScrollPane scrollPane = new JScrollPane(area);

		return scrollPane;
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
	 * Assembles a {@code String} containing {@code err}'s stack trace. Each element
	 * of the trace is on a new line.
	 * 
	 * @param err {@code Exception} to take stack trace from
	 * @return multi-line {@code String} representation of {@code err}'s stack trace
	 */
	private String buildStackTrace(Exception err) {
		String trace = "";
		for (StackTraceElement elem : err.getStackTrace()) {
			trace += elem + "\n";
		}
		return trace;
	}

}
