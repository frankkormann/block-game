package game;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import game.GameInputHandler.DirectionSelectorInput;
import game.GameInputHandler.MovementInput;
import game.GameInputHandler.ResizingInput;
import game.MenuBar.MetaInput;

/**
 * {@code JDialog} for letting the user change settings such as controls.
 */
public class OptionsDialog extends JDialog
		implements KeyListener, KeybindChangeListener, WindowListener {

	private static final String TITLE = "Options";
	private static final String REBIND_INSTRUCTIONS = "Click to finish";

	private InputMapper inputMapper;
	private Enum<?> currentlyRebindingInput;
	private Map<Enum<?>, JButton> inputToButton;

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
		currentlyRebindingInput = null;
		inputToButton = new HashMap<>();

		JPanel contentPanePanel = new JPanel(); // Ensure that content pane is a
		setContentPane(contentPanePanel);	   // JPanel so it can have a border
		contentPanePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(createControlsPanel());

		addKeyListener(this);
		addWindowListener(this);
		inputMapper.addKeybindListener(this);

		pack();
		setLocationRelativeTo(null);
	}

	private JPanel createControlsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.add(createMovementControlsPanel());
		panel.add(createResizingControlsPanel());
		panel.add(createMetaControlsPanel());

		JButton resetButton = new JButton("Reset to defaults");
		resetButton.addActionListener(e -> {
			inputMapper.setToDefaults();
		});
		resetButton.setFocusable(false);
		resetButton.setAlignmentX(CENTER_ALIGNMENT);

		panel.add(resetButton);

		return panel;
	}

	private JPanel createMovementControlsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		for (MovementInput input : MovementInput.values()) {
			panel.add(createButtonPanel(input));
			panel.setAlignmentX(CENTER_ALIGNMENT);
		}

		return panel;
	}

	private JPanel createResizingControlsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		for (DirectionSelectorInput input : DirectionSelectorInput.values()) {
			panel.add(createButtonPanel(input));
			panel.setAlignmentX(CENTER_ALIGNMENT);
		}
		for (ResizingInput input : ResizingInput.values()) {
			panel.add(createButtonPanel(input));
			panel.setAlignmentX(CENTER_ALIGNMENT);
		}

		return panel;
	}

	private JPanel createMetaControlsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		for (MetaInput input : MetaInput.values()) {
			panel.add(createButtonPanel(input));
			panel.setAlignmentX(CENTER_ALIGNMENT);
		}

		return panel;
	}

	private JPanel createButtonPanel(Enum<?> input) {
		JPanel panel = new JPanel();

		panel.add(new JLabel(input.toString()));
		panel.add(createButtonForInput(input));

		return panel;
	}

	private JButton createButtonForInput(Enum<?> input) {
		JButton button = new JButton(inputToString(input));
		button.addActionListener(e -> {
			if (currentlyRebindingInput == null) {
				currentlyRebindingInput = input;
			}
			else if (currentlyRebindingInput == input) {
				currentlyRebindingInput = null;
			}
			updateButtonText(input);
		});
		button.setFocusable(false);

		inputToButton.put(input, button);

		return button;
	}

	/**
	 * Sets the text of the button associated with {@code input}.
	 * 
	 * @param input enum value for the button
	 */
	private void updateButtonText(Enum<?> input) {
		String newText;
		if (currentlyRebindingInput != input) {
			newText = inputToString(input);
		}
		else {
			newText = REBIND_INSTRUCTIONS;
		}
		inputToButton.get(input).setText(newText);
	}

	private String inputToString(Enum<?> input) {
		String asString = "";
		Pair<Integer, Integer> keybind = inputMapper.getKeybind(input);
		if (keybind.second != 0) {
			asString += KeyEvent.getModifiersExText(keybind.second) + "+";
		}
		return asString + KeyEvent.getKeyText(keybind.first);
	}

	/* KeyListener */

	@Override
	public void keyPressed(KeyEvent e) {
		if (currentlyRebindingInput != null) {
			inputMapper.setKeybind(currentlyRebindingInput, e.getKeyCode(),
					e.getModifiersEx());
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}

	/* KeybindChangeListener */

	@Override
	public void keybindChanged(Enum<?> input, int newKeyCode,
			int newModifiers) {
		if (inputToButton.containsKey(input)) {
			updateButtonText(input);
		}
	}

	/* WindowListener */

	@Override
	public void windowClosing(WindowEvent e) {
		inputMapper.removeKeybindListener(this);
	}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

}
