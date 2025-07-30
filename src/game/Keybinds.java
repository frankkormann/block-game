package game;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import game.GameInputHandler.DirectionSelectorInput;
import game.GameInputHandler.MovementInput;
import game.GameInputHandler.ResizingInput;
import game.MenuBar.MetaInput;

/**
 * Container for keybind information while reading/writing JSON.
 * 
 * @author Frank Kormann
 */
public class Keybinds {

	private static final Class<? extends Enum<?>>[] ENUM_CLASSES = new Class[] {
			MovementInput.class, DirectionSelectorInput.class,
			ResizingInput.class, MetaInput.class };

	public Map<Enum<?>, Pair<Integer, Integer>> keybinds;

	public Keybinds() {
		keybinds = new HashMap<>();
	}

	public void setKeybinds(Map<String, Pair<Integer, Integer>> keybinds)
			throws IOException {
		for (Entry<String, Pair<Integer, Integer>> entry : keybinds
				.entrySet()) {
			try {
				this.keybinds.put(getEnum(entry.getKey()), entry.getValue());
			}
			catch (IllegalArgumentException e) {
				throw new IOException(e);
			}
		}
	}

	private Enum<?> getEnum(String name) throws IllegalArgumentException {
		Enum<?> result = null;
		for (Class<? extends Enum> enumClass : ENUM_CLASSES) {
			try {
				result = Enum.valueOf(enumClass, name);
			}
			catch (IllegalArgumentException ignored) {}
		}

		if (result == null) {
			throw new IllegalArgumentException("Unknown enum value");
		}

		return result;
	}

}
