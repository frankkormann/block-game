package blockgame.gui;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import blockgame.MusicPlayer;
import blockgame.input.ColorMapper;
import blockgame.input.InputMapper;
import blockgame.input.ParameterMapper;

/**
 * {@code JDialog} for letting the user change keyboard bindings and color
 * values.
 * 
 * @author Frank Kormann
 */
public class OptionsDialog extends JDialog {

	private static final String TITLE = "Options";
	private static final int VERTICAL_SPACE = 3;

	/**
	 * Creates a {@code OptionsDialog} for altering the keybinds in
	 * {@code inputMapper}, the colors of {@code colorMapper}, and the values of
	 * {@code paramMapper}.
	 * 
	 * @param owner       {@code Window} owner
	 * @param inputMapper {@code InputMapper} to change controls of
	 * @param colorMapper {@code ColorMapper} to change colors of
	 * @param paramMapper {@code ParameterMapper} to change values of
	 */
	public OptionsDialog(Window owner, InputMapper inputMapper,
			ColorMapper colorMapper, ParameterMapper paramMapper,
			MusicPlayer musicPlayer) {
		super(owner, TITLE, Dialog.DEFAULT_MODALITY_TYPE);

		JPanel contentPanePanel = new JPanel(); // Ensure that content pane is a
		setContentPane(contentPanePanel);	   // JPanel so it can have a border
		contentPanePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("General",
				new ParameterChangerPanel(getRootPane(), paramMapper));
		tabbedPane.addTab("Controls",
				new ControlsChangerPanel(getRootPane(), inputMapper));
		tabbedPane.addTab("Colors",
				new ColorChangerPanel(getRootPane(), colorMapper));
		tabbedPane.addTab("Music", new MusicPanel(musicPlayer));

		JButton closeButton = new JButton("OK");
		closeButton.addActionListener(e -> dispose());
		closeButton.setAlignmentX(CENTER_ALIGNMENT);

		JScrollPane scrollPane = new JScrollPane(tabbedPane);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());

		add(scrollPane);
		add(new JSeparator());
		add(Box.createVerticalStrut(VERTICAL_SPACE));
		add(closeButton);

		registerDisposeOnKeypress(KeyEvent.VK_ESCAPE);

		pack();
		setLocationRelativeTo(null);
	}

	/**
	 * Sets this to close when the key corresponding to {@code keyCode} is
	 * pressed.
	 * 
	 * @param keyCode which key should be pressed
	 */
	private void registerDisposeOnKeypress(int keyCode) {
		AbstractAction dispose = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};

		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(keyCode, 0), "close");
		getRootPane().getActionMap().put("close", dispose);
	}

}
