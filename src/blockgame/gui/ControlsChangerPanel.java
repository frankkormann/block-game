package blockgame.gui;

import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import blockgame.gui.MenuBar.MetaInput;
import blockgame.input.GameInputHandler;
import blockgame.input.GetterSetter;
import blockgame.input.InputMapper;
import blockgame.input.ValueChangerPanel;
import blockgame.input.GameInputHandler.DirectionSelectorInput;
import blockgame.input.GameInputHandler.MovementInput;
import blockgame.input.GameInputHandler.ResizingInput;
import blockgame.util.Pair;

/**
 * {@code JPanel} which allows the user to rebind keyboard inputs in
 * {@code InputMapper}. Automatically calls {@code InputMapper.save} when the
 * parent window is closed.
 * 
 * @author Frank Kormann
 */
public class ControlsChangerPanel
		extends ValueChangerPanel<Pair<Integer, Integer>>
		implements KeyEventDispatcher, KeyListener {

	private static final String REBIND_INSTRUCTIONS = "Type a new key";

	private static final String MOVEMENT_CONTROLS_TITLE = "Movement";
	private static final String RESIZING_CONTROLS_TITLE = "Window Resizing";
	private static final String META_CONTROLS_TITLE = "Menu Shortcuts";

	private static final int VERTICAL_SPACE = 3;

	private InputMapper inputMapper;
	private Enum<?> currentlyRebindingInput;

	/**
	 * Creates a {@code ControlsChangerPanel} which will change the keybinds in
	 * {@code inputMapper}.
	 * 
	 * @param rootPane    {@code JRootPane} of {@code Window} this will be added
	 *                    to
	 * @param inputMapper {@code InputMapper} to alter
	 */
	public ControlsChangerPanel(JRootPane rootPane, InputMapper inputMapper) {
		super(rootPane, inputMapper);
		this.inputMapper = inputMapper;
		currentlyRebindingInput = null;

		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(this);
	}

	@Override
	protected GetterSetter<Pair<Integer, Integer>> createGetterSetter(
			Enum<?> enumValue) {
		return new InputButton(enumValue);
	}

	@Override
	protected JPanel createGetterSetterPanel(
			Map<Enum<?>, GetterSetter<Pair<Integer, Integer>>> getterSetters) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		Pair<JPanel, JComboBox<String>> cardsPanelComponents = createCardsPanel(
				new Pair<>(
						createInputsPanel(getterSetters,
								MovementInput.values()),
						MOVEMENT_CONTROLS_TITLE),
				new Pair<>(
						createInputsPanel(getterSetters,
								DirectionSelectorInput.values(),
								ResizingInput.values()),
						RESIZING_CONTROLS_TITLE),
				new Pair<>(createInputsPanel(getterSetters, MetaInput.values()),
						META_CONTROLS_TITLE));

		add(Box.createVerticalStrut(VERTICAL_SPACE));
		add(cardsPanelComponents.second);
		add(cardsPanelComponents.first);
		add(Box.createVerticalStrut(VERTICAL_SPACE));

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
	 * Creates a {@code JPanel} which displays all the inputs in
	 * {@code inputsList} with a {@code InputButton} to rebind them.
	 * 
	 * @param inputButtons {@code Map} of enum values to their
	 *                     {@code InputButton}
	 * @param inputsList   enum values to display
	 * 
	 * @return the {@code JPanel}
	 */
	private JPanel createInputsPanel(
			Map<Enum<?>, GetterSetter<Pair<Integer, Integer>>> inputButtons,
			Enum<?>[]... inputsList) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		for (Enum<?>[] inputs : inputsList) {
			for (Enum<?> input : inputs) {
				if (!inputButtons.containsKey(input)) {
					continue;
				}

				JPanel inputPanel = new JPanel();
				inputPanel.setLayout(new GridLayout());

				JLabel label = new JLabel(inputToName(input));
				label.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));

				inputPanel.add(label);
				inputPanel.add((InputButton) inputButtons.get(input));

				panel.add(Box.createVerticalStrut(VERTICAL_SPACE));
				panel.add(inputPanel);
			}
		}

		return panel;
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

	/**
	 * {@code JButton} that displays a keybind.
	 */
	private class InputButton extends JButton
			implements GetterSetter<Pair<Integer, Integer>> {

		private Enum<?> input;
		private Pair<Integer, Integer> keybind;

		public InputButton(Enum<?> input) {
			super();
			keybind = new Pair<>(0, 0);
			addActionListener(e -> {
				if (currentlyRebindingInput == null) {
					currentlyRebindingInput = input;
					setText(REBIND_INSTRUCTIONS);
				}
				else if (currentlyRebindingInput == input) {
					currentlyRebindingInput = null;
					set(get());  // Reset text
				}
			});
		}

		@Override
		public Pair<Integer, Integer> get() {
			return keybind;
		}

		@Override
		public void set(Pair<Integer, Integer> value) {
			if (value == null) {
				setText(" ");
			}
			else {
				setText("");
				if (value.second != 0) {
					setText(KeyEvent.getModifiersExText(value.second) + "+");
				}
				setText(getText() + KeyEvent.getKeyText(value.first));
			}
			keybind = value;
		}

		@Override
		public void addChangeListener(Runnable runnable) {}

	}

}
