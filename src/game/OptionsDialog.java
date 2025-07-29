package game;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import game.GameInputHandler.DirectionSelectorInput;
import game.GameInputHandler.GameInput;
import game.GameInputHandler.ResizingInput;
import game.MenuBar.MetaInput;

/**
 * {@code JDialog} for letting the user change settings such as controls.
 */
public class OptionsDialog extends JDialog implements KeyListener {

	private static final String TITLE = "Options";

	private InputMapper inputMapper;
	private Enum<?> currentlyRebinding;

	/**
	 * Creates a {@code OptionsDialog} for altering the keybinds in
	 * {@code inputMapper}.
	 * 
	 * @param owner       {@code Window} to display this on
	 * @param inputMapper {@code InputMapper} to change controls of
	 */
	public OptionsDialog(Window owner, InputMapper inputMapper) {
		super(owner, TITLE, Dialog.DEFAULT_MODALITY_TYPE);
		this.inputMapper = inputMapper;
		currentlyRebinding = null;

		JPanel contentPanePanel = new JPanel(); // Ensure that content pane is a
		setContentPane(contentPanePanel);	   // JPanel so it can have a border
		contentPanePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(createControlsPanel());

		addKeyListener(this);

		pack();
		setLocationRelativeTo(null);
	}

	private JPanel createControlsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		for (JPanel pa : createButtonPanels()) {
			panel.add(pa);
		}

		return panel;
	}

	private List<JPanel> createButtonPanels() {
		List<JPanel> panelsList = new ArrayList<>();

		Stream.concat(
				Stream.concat(Arrays.stream(MetaInput.values()),
						Stream.concat(Arrays.stream(ResizingInput.values()),
								Arrays.stream(
										DirectionSelectorInput.values()))),
				Arrays.stream(GameInput.values())).forEach(e -> {
					JPanel panel = new JPanel();
					panel.add(new JLabel(e.name()));
					JButton button = new JButton(inputToString(e));
					button.setFocusable(false);
					button.addActionListener(ev -> {
						if (currentlyRebinding == null) {
							currentlyRebinding = e;
							button.setText("Press new key, click to finish");
						}
						else {
							currentlyRebinding = null;
							button.setText(inputToString(e));
						}
					});
					panel.add(button);

					panelsList.add(panel);
				});

		return panelsList;
	}

	private String inputToString(Enum<?> input) {
		String asString = "";
		Pair<Integer, Integer> keybind = inputMapper.getKeybind(input);
		if (keybind.second != 0) {
			asString += KeyEvent.getModifiersExText(keybind.second) + "+";
		}
		return asString + KeyEvent.getKeyText(keybind.first);
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		if (currentlyRebinding != null) {
			inputMapper.setKeybind(currentlyRebinding, e.getKeyCode(),
					e.getModifiersEx());
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}

}
