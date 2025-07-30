package game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

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

	private Map<Enum<?>, Pair<Integer, Integer>> inputToKeybind;
	private List<KeybindChangeListener> changeListeners;

	/**
	 * Creates an {@code InputMapper} with default key binds for
	 * {@code MovementInput}, {@code DirectionSelectorInput},
	 * {@code ResizingInput}, and {@code MetaInput}.
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
		try {
			ObjectMapper mapper = new ObjectMapper();
			Keybinds json = mapper.readValue(
					GameController.class.getResourceAsStream("/controls.json"),
					Keybinds.class);
			for (Enum<?> input : json.keybinds.keySet()) {
				Pair<Integer, Integer> keybind = json.keybinds.get(input);
				setKeybind(input, keybind.first, keybind.second);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Can't read keybind defaults", e)
					.setVisible(true);
		}
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

	public void removeKeybind(Enum<?> input) {
		inputToKeybind.remove(input);

		for (KeybindChangeListener listener : changeListeners) {
			listener.keybindRemoved(input);
		}
	}

	/**
	 * Returns the keyboard input associated with {@code input}. Returns
	 * {@code null} if there is no set keybind for {@code input}.
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
