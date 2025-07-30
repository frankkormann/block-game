package game;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import game.GameInputHandler.DirectionSelectorInput;
import game.GameInputHandler.MovementInput;
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
 * <p>
 * Mappings will be read from a save file if possible, or a resource if the save
 * file is unavailable.
 */
public class InputMapper extends Mapper<Pair<Integer, Integer>> {

	private static final String KEYBIND_PATH = "./save/controls.json";
	private static final String DEFAULT_KEYBIND_RESOURCE = "/controls_default.json";

	private List<KeybindChangeListener> changeListeners;

	/**
	 * Creates an {@code InputMapper} with default key binds for
	 * {@code MovementInput}, {@code DirectionSelectorInput},
	 * {@code ResizingInput}, and {@code MetaInput}.
	 */
	public InputMapper() {
		super(new File(KEYBIND_PATH), DEFAULT_KEYBIND_RESOURCE);
		changeListeners = new ArrayList<>();
	}

	public TypeReference<EnumValues<Pair<Integer, Integer>>> getJsonTypeReference() {
		return new TypeReference<EnumValues<Pair<Integer, Integer>>>() {};
	}

	public Class<? extends Enum<?>>[] getEnumClasses() {
		return new Class[] { MovementInput.class, DirectionSelectorInput.class,
				ResizingInput.class, MetaInput.class };
	}

	/**
	 * Associates the specified keyboard input with {@code input}.
	 * 
	 * @param input     enum value to set
	 * @param keyCode   key code of keyboard input
	 * @param modifiers modifier mask of keyboard input
	 */
	public void set(Enum<?> input, int keyCode, int modifiers) {
		super.set(input, new Pair<Integer, Integer>(keyCode, modifiers));

		if (changeListeners != null) {  // Will be null during super() in
										  // constructor
			for (KeybindChangeListener listener : changeListeners) {
				listener.keybindChanged(input, keyCode, modifiers);
			}
		}
	}

	@Override
	public void set(Enum<?> input, Pair<Integer, Integer> keybind) {
		set(input, keybind.first, keybind.second);
	}

	@Override
	public void remove(Enum<?> input) {
		super.remove(input);

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
	// Overridden for more descriptive Javadoc
	public Pair<Integer, Integer> getKeybind(Enum<?> input) {
		return get(input);
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
