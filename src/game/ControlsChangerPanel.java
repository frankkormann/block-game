package game;

import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import game.GameInputHandler.DirectionSelectorInput;
import game.GameInputHandler.MovementInput;
import game.GameInputHandler.ResizingInput;
import game.MenuBar.MetaInput;

/**
 * {@code JPanel} which allows the user to rebind keyboard inputs in
 * {@code InputMapper}. Automatically calls {@code InputMapper.save} when the
 * parent window is closed.
 * 
 * @author Frank Kormann
 */
public class ControlsChangerPanel extends JPanel
		implements KeyEventDispatcher, KeyListener, ValueChangeListener {

	private static final String REBIND_INSTRUCTIONS = "Type a new key";
	private static final String UNDO_TEXT = "Undo changes";
	private static final String RESET_TEXT = "Reset to defaults";

	private static final String MOVEMENT_CONTROLS_TITLE = "Movement";
	private static final String RESIZING_CONTROLS_TITLE = "Window Resizing";
	private static final String META_CONTROLS_TITLE = "Menu Shortcuts";

	private static final int VERTICAL_SPACE = 3;

	private InputMapper inputMapper;
	private Enum<?> currentlyRebindingInput;
	private Map<Enum<?>, JButton> inputToButton;

	/**
	 * Creates a {@code ControlsChangerPanel} which will change the keybinds in
	 * {@code inputMapper}.
	 * 
	 * @param rootPane    {@code JRootPane} of {@code Window} this will be added
	 *                    to
	 * @param inputMapper {@code InputMapper} to alter
	 */
	public ControlsChangerPanel(JRootPane rootPane, InputMapper inputMapper) {
		super();
		this.inputMapper = inputMapper;
		currentlyRebindingInput = null;
		inputToButton = new HashMap<>();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

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

		add(Box.createVerticalStrut(VERTICAL_SPACE));
		add(cardsPanelComponents.second);
		add(cardsPanelComponents.first);
		add(Box.createVerticalStrut(VERTICAL_SPACE));
		add(createUndoResetButtonsPanel());

		Window window = SwingUtilities.getWindowAncestor(rootPane);
		window.addKeyListener(this);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cleanUp();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				cleanUp();
			}
		});
		inputMapper.addListener(this);

		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(this);
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
	 * Creates a {@code JPanel} with a {@code JButton} to undo temporary keybind
	 * changes and a {@code JButton} to reset keybinds to defaults.
	 * 
	 * @return the {@code JPanel}
	 */
	private JPanel createUndoResetButtonsPanel() {
		JPanel panel = new JPanel();

		JButton undoButton = new JButton(UNDO_TEXT);
		undoButton.addActionListener(e -> {
			inputMapper.reload();
		});

		JButton resetButton = new JButton(RESET_TEXT);
		resetButton.addActionListener(e -> {
			inputMapper.setToDefaults();
		});

		panel.add(undoButton);
		panel.add(resetButton);

		return panel;
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

		JLabel label = new JLabel(inputToName(input));
		label.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));

		panel.add(label);
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
			else if (currentlyRebindingInput == input) {
				currentlyRebindingInput = null;
				updateButtonText(input);
			}
		});

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
		Pair<Integer, Integer> keybind = inputMapper.get(input);

		if (keybind == null) {
			return " ";  // Return a space to prevent the button's height from
		}				 // decreasing

		if (keybind.second != 0) {
			asString += KeyEvent.getModifiersExText(keybind.second) + "+";
		}
		return asString + KeyEvent.getKeyText(keybind.first);
	}

	private void cleanUp() {
		inputMapper.removeListener(this);
		inputMapper.save();
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

	/* KeyEventDispatcher */

	public boolean dispatchKeyEvent(KeyEvent e) {
		if (currentlyRebindingInput != null
				&& e.getID() == KeyEvent.KEY_PRESSED) {
			keyPressed(e);
			return true;
		}
		return false;
	}

	/* KeyListener */

	@Override
	public void keyPressed(KeyEvent e) {
		if (currentlyRebindingInput != null && !isModifierKey(e.getKeyCode())) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				inputMapper.remove(currentlyRebindingInput);
			}
			else {
				inputMapper.set(currentlyRebindingInput, e.getKeyCode(),
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
	public void valueChanged(Enum<?> input, Object newKeybind) {
		if (inputToButton.containsKey(input)) {
			updateButtonText(input);
		}
	}

	@Override
	public void valueRemoved(Enum<?> input) {
		if (inputToButton.containsKey(input)) {
			updateButtonText(input);
		}
	}

}
