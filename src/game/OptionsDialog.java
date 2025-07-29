package game;

import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
	private static final String REBIND_INSTRUCTIONS = "Type a new key";

	private static final String MOVEMENT_CONTROLS_TITLE = "Movement";
	private static final String RESIZING_CONTROLS_TITLE = "Window Resizing";
	private static final String META_CONTROLS_TITLE = "Menu Shortcuts";

	private static final int VERTICAL_SPACE = 3;

	private InputMapper inputMapper;
	private Enum<?> currentlyRebindingInput;
	private Map<Enum<?>, JButton> inputToButton;

	JPanel controlsPanel;

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

		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		controlsPanel = createControlsPanel();
		JButton closeButton = new JButton("OK");
		closeButton.addActionListener(e -> dispose());
		closeButton.setFocusable(false);
		closeButton.setAlignmentX(CENTER_ALIGNMENT);

		add(controlsPanel);
		add(Box.createVerticalStrut(VERTICAL_SPACE));
		add(closeButton);

		addKeyListener(this);
		addWindowListener(this);
		inputMapper.addKeybindListener(this);

		pack();
		setLocationRelativeTo(null);
	}

	private JPanel createControlsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel resizingControlsPanel = new JPanel();
		resizingControlsPanel.setLayout(
				new BoxLayout(resizingControlsPanel, BoxLayout.Y_AXIS));
		resizingControlsPanel
				.add(createInputsPanel(DirectionSelectorInput.values()));
		resizingControlsPanel.add(createInputsPanel(ResizingInput.values()));

		Pair<JPanel, JComboBox<String>> cardsPanelComponents = createCardsPanel(
				new Pair<>(createInputsPanel(MovementInput.values()),
						MOVEMENT_CONTROLS_TITLE),
				new Pair<>(resizingControlsPanel, RESIZING_CONTROLS_TITLE),
				new Pair<>(createInputsPanel(MetaInput.values()),
						META_CONTROLS_TITLE));

		JButton resetButton = new JButton("Reset all to defaults");
		resetButton.addActionListener(e -> {
			inputMapper.setToDefaults();
		});
		resetButton.setFocusable(false);
		resetButton.setAlignmentX(CENTER_ALIGNMENT);

		panel.add(cardsPanelComponents.second);
		panel.add(cardsPanelComponents.first);
		panel.add(Box.createVerticalStrut(VERTICAL_SPACE));
		panel.add(resetButton);

		return panel;
	}

	/**
	 * Creates a {@code JPanel} using {@code CardLayout} and a {@code JComboBox}
	 * to control it. For each {@code Pair} of a {@code JPanel} and
	 * {@code String}, the {@code JPanel} will be added as a card to
	 * {@code CardLayout} and the {@code String} will act as its key in the
	 * {@code JComboBox}.
	 * 
	 * @param panels list of {@code Pair}s of {@code JPanel}s to display and
	 *               {@code String} to act as keys
	 * 
	 * @return {@code Pair} of the {@code JPanel} which holds the cards and the
	 *         {@code JComboBox} which controls it
	 */
	private Pair<JPanel, JComboBox<String>> createCardsPanel(
			Pair<JPanel, String>... panels) {
		JPanel panel = new JPanel();
		panel.setLayout(new CardLayout());

		JComboBox<String> controller = new JComboBox<>();
		controller.setEditable(false);
		controller.setFocusable(false);

		for (Pair<JPanel, String> panelPair : panels) {
			controller.addItem(panelPair.second);
			panel.add(panelPair.first, panelPair.second);
		}

		controller.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				CardLayout cardLayout = (CardLayout) panel.getLayout();
				cardLayout.show(panel, (String) e.getItem());
			}
		});

		return new Pair<>(panel, controller);
	}

	/**
	 * Creates a {@code JPanel} which displays all the inputs in {@code inputs}
	 * with a {@code JButton} to rebind them.
	 * 
	 * @param inputs enum values to display
	 * 
	 * @return the {@code JPanel}
	 */
	private JPanel createInputsPanel(Enum<?>[] inputs) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		for (Enum<?> input : inputs) {
			panel.add(Box.createVerticalStrut(VERTICAL_SPACE));
			panel.add(createButtonPanel(input));
			panel.setAlignmentX(CENTER_ALIGNMENT);
		}

		return panel;
	}

	/**
	 * Creates a {@code JPanel} for {@code input}. It contains a {@code JLabel}
	 * with {@code input}'s name and a {@code JButton} to rebind it.
	 * 
	 * @param input enum value to create a {@code JPanel} for
	 * 
	 * @return the {@code JPanel}
	 */
	private JPanel createButtonPanel(Enum<?> input) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2, 10, 10));

		panel.add(new JLabel(inputToName(input)));
		panel.add(createButtonForInput(input));

		return panel;
	}

	/**
	 * Creates a {@code JButton} which will rebind {@code input}.
	 * 
	 * @param input enum value to rebind
	 * 
	 * @return the {@code JButton}
	 */
	private JButton createButtonForInput(Enum<?> input) {
		JButton button = new JButton(inputToKeybindString(input));
		button.addActionListener(e -> {
			if (currentlyRebindingInput == null) {
				currentlyRebindingInput = input;
				button.setText(REBIND_INSTRUCTIONS);
			}
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
		inputToButton.get(input).setText(inputToKeybindString(input));
	}

	/**
	 * Converts {@code input} into a {@code String} that represents its keybind.
	 * 
	 * @param input enum value to convert
	 * 
	 * @return {@code String} representation of the key presses to trigger it
	 */
	private String inputToKeybindString(Enum<?> input) {
		String asString = "";
		Pair<Integer, Integer> keybind = inputMapper.getKeybind(input);

		if (keybind == null) {
			return " ";  // Return a space to prevent the button's height from
		}				 // decreasing

		if (keybind.second != 0) {
			asString += KeyEvent.getModifiersExText(keybind.second) + "+";
		}
		return asString + KeyEvent.getKeyText(keybind.first);
	}

	/**
	 * Converts {@code input} to a user-understandable {@code String}. If there
	 * is no mapping available, returns {@code input.toString()}.
	 * 
	 * @param input enum value to convert
	 * 
	 * @return {@code String} that describes {@code input}
	 */
	private String inputToName(Enum<?> input) {
		if (input == MovementInput.UP) {
			return "Jump";
		}
		if (input == MovementInput.LEFT) {
			return "Move left";
		}
		if (input == MovementInput.RIGHT) {
			return "Move right";
		}
		if (input == DirectionSelectorInput.SELECT_NORTH) {
			return "Select top edge";
		}
		if (input == DirectionSelectorInput.SELECT_SOUTH) {
			return "Select bottom edge";
		}
		if (input == DirectionSelectorInput.SELECT_WEST) {
			return "Select left edge";
		}
		if (input == DirectionSelectorInput.SELECT_EAST) {
			return "Select right edge";
		}
		if (input == ResizingInput.MOVE_UP) {
			return "Move selected edge up";
		}
		if (input == ResizingInput.MOVE_DOWN) {
			return "Move selected edge down";
		}
		if (input == ResizingInput.MOVE_LEFT) {
			return "Move selected edge left";
		}
		if (input == ResizingInput.MOVE_RIGHT) {
			return "Move selected edge right";
		}
		if (input == MetaInput.FRAME_ADVANCE) {
			return "Advance frame";
		}
		if (input == MetaInput.PAUSE) {
			return "Pause";
		}
		if (input == MetaInput.PLAY_RECORDING) {
			return "Open recording file";
		}
		if (input == MetaInput.PLAY_SOLUTION) {
			return "Show level solution";
		}
		if (input == MetaInput.RELOAD_LEVEL) {
			return "Retry";
		}
		if (input == MetaInput.SAVE_RECORDING) {
			return "Save recording file";
		}
		if (input == MetaInput.STOP_RECORDING) {
			return "Stop recording playback";
		}
		if (input == MetaInput.TOGGLE_HINTS) {
			return "Show hint";
		}

		return input.toString();
	}

	/* KeyListener */

	@Override
	public void keyPressed(KeyEvent e) {
		if (currentlyRebindingInput != null && !isModifierKey(e.getKeyCode())) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				inputMapper.removeKeybind(currentlyRebindingInput);
			}
			else {
				inputMapper.setKeybind(currentlyRebindingInput, e.getKeyCode(),
						e.getModifiersEx());
			}

			currentlyRebindingInput = null;
		}
	}

	private boolean isModifierKey(int keyCode) {
		return keyCode == KeyEvent.VK_ALT || keyCode == KeyEvent.VK_ALT_GRAPH
				|| keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_META
				|| keyCode == KeyEvent.VK_SHIFT;
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

	@Override
	public void keybindRemoved(Enum<?> input) {
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
