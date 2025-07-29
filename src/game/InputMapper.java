package game;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.GameInputHandler.DirectionSelectorInput;
import game.GameInputHandler.GameInput;
import game.GameInputHandler.ResizingInput;
import game.MenuBar.MetaInput;

/**
 * Maps keyboard inputs to enumerated values. A keyboard input is a
 * {@code KeyEvent.VK_?} key code and a {@code KeyEvent.???_MASK} modifier mask,
 * or {@code 0} if no modifier keys should be held down.
 * <p>
 * A core part of this is that keybinds can be changed. Any class which is
 * interested in receiving updates on changes to keybinds should implement
 * {@code KeybindChangeListener} and register itself with {link
 * {@link #addKeybindListener(KeybindChangeListener)}.
 */
public class InputMapper {

	public static final int SHIFT_CONTROL_MASK = KeyEvent.CTRL_DOWN_MASK
			| KeyEvent.SHIFT_DOWN_MASK;

	private Map<Enum<?>, Pair<Integer, Integer>> inputToKeybind;
	private List<KeybindChangeListener> changeListeners;

	/**
	 * Creates an {@code InputMapper} with default key binds for
	 * {@code GameInput}, {@code DirectionSelectorInput}, {@code ResizingInput},
	 * and {@code MetaInput}.
	 */
	public InputMapper() {
		inputToKeybind = new HashMap<>();
		changeListeners = new ArrayList<>();

		setToDefaults();
	}

	/**
	 * Sets all keybinds to their default values.
	 */
	public void setToDefaults() {
		// TODO Read these from a file instead
		setKeybind(GameInput.UP, KeyEvent.VK_W, 0);
		setKeybind(GameInput.LEFT, KeyEvent.VK_A, 0);
		setKeybind(GameInput.RIGHT, KeyEvent.VK_D, 0);

		setKeybind(DirectionSelectorInput.SELECT_NORTH, KeyEvent.VK_I,
				KeyEvent.SHIFT_DOWN_MASK);
		setKeybind(DirectionSelectorInput.SELECT_SOUTH, KeyEvent.VK_K,
				KeyEvent.SHIFT_DOWN_MASK);
		setKeybind(DirectionSelectorInput.SELECT_WEST, KeyEvent.VK_J,
				KeyEvent.SHIFT_DOWN_MASK);
		setKeybind(DirectionSelectorInput.SELECT_EAST, KeyEvent.VK_L,
				KeyEvent.SHIFT_DOWN_MASK);
		setKeybind(ResizingInput.MOVE_DOWN, KeyEvent.VK_K, 0);
		setKeybind(ResizingInput.MOVE_RIGHT, KeyEvent.VK_L, 0);
		setKeybind(ResizingInput.MOVE_UP, KeyEvent.VK_I, 0);
		setKeybind(ResizingInput.MOVE_LEFT, KeyEvent.VK_J, 0);

		setKeybind(MetaInput.PAUSE, KeyEvent.VK_P, 0);
		setKeybind(MetaInput.FRAME_ADVANCE, KeyEvent.VK_N, 0);
		setKeybind(MetaInput.RELOAD_LEVEL, KeyEvent.VK_R, 0);
		setKeybind(MetaInput.TOGGLE_HINTS, KeyEvent.VK_H, 0);
		setKeybind(MetaInput.PLAY_SOLUTION, KeyEvent.VK_H, SHIFT_CONTROL_MASK);
		setKeybind(MetaInput.SAVE_RECORDING, KeyEvent.VK_S, SHIFT_CONTROL_MASK);
		setKeybind(MetaInput.PLAY_RECORDING, KeyEvent.VK_P, SHIFT_CONTROL_MASK);
		setKeybind(MetaInput.STOP_RECORDING, KeyEvent.VK_S, 0);
	}

	/**
	 * Associates the specified keyboard input with {@code input}.
	 * 
	 * @param input     enum value to set
	 * @param keyCode   key code of keyboard input
	 * @param modifiers modifier mask of keyboard input
	 */
	public void setKeybind(Enum<?> input, int keyCode, int modifiers) {
		inputToKeybind.put(input, new Pair<>(keyCode, modifiers));

		for (KeybindChangeListener listener : changeListeners) {
			listener.keybindChanged(input, keyCode, modifiers);
		}
	}

	/**
	 * Returns the keyboard input associated with {@code input}.
	 * 
	 * @param input enum value to get
	 * 
	 * @return {@code Pair} where first value is the key code and second value
	 *         is the modifier mask
	 */
	public Pair<Integer, Integer> getKeybind(Enum<?> input) {
		return inputToKeybind.get(input);
	}

	/**
	 * Registers {@code listener} to receive keybind change events.
	 * 
	 * @param listener {@code KeybindChangeListener} to add
	 */
	public void addKeybindListener(KeybindChangeListener listener) {
		changeListeners.add(listener);
	}

	public void removeKeybindListener(KeybindChangeListener listener) {
		changeListeners.remove(listener);
	}

}
