package game;

import java.awt.Dialog;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

/**
 * {@code JDialog} for letting the user change keyboard bindings and color
 * values.
 */
public class OptionsDialog extends JDialog {

	private static final String TITLE = "Options";
	private static final int VERTICAL_SPACE = 3;

	/**
	 * Creates a {@code OptionsDialog} for altering the keybinds in
	 * {@code inputMapper}.
	 * 
	 * @param owner       {@code Window} to display this on
	 * @param inputMapper {@code InputMapper} to change controls of
	 * @param colorMapper {@code ColorMapper} to change colors of
	 */
	public OptionsDialog(Window owner, InputMapper inputMapper,
			ColorMapper colorMapper) {
		super(owner, TITLE, Dialog.DEFAULT_MODALITY_TYPE);

		JPanel contentPanePanel = new JPanel(); // Ensure that content pane is a
		setContentPane(contentPanePanel);	   // JPanel so it can have a border
		contentPanePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setFocusable(false);
		tabbedPane.addTab("Controls",
				new ControlsChangerPanel(getRootPane(), inputMapper));
		tabbedPane.addTab("Colors",
				new ColorChangerPanel(getRootPane(), colorMapper));

		JButton closeButton = new JButton("OK");
		closeButton.addActionListener(e -> dispose());
		closeButton.setFocusable(false);
		closeButton.setAlignmentX(CENTER_ALIGNMENT);

		add(tabbedPane);
		add(new JSeparator());
		add(Box.createVerticalStrut(VERTICAL_SPACE));
		add(closeButton);

		pack();
		setLocationRelativeTo(null);
	}

}
