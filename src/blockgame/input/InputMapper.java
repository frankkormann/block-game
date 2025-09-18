package blockgame.input;

import com.fasterxml.jackson.core.type.TypeReference;

import blockgame.gui.MenuBar.MetaInput;
import blockgame.input.GameInputHandler.AbsoluteResizingInput;
import blockgame.input.GameInputHandler.DirectionSelectorInput;
import blockgame.input.GameInputHandler.MovementInput;
import blockgame.input.GameInputHandler.SelectedSideResizingInput;
import blockgame.util.Pair;

/**
 * {@code Mapper} for keyboard inputs. A keyboard input is a
 * {@code KeyEvent.VK_?} key code and a {@code KeyEvent.???_MASK} modifier mask,
 * or {@code 0} if no modifier keys should be held down.
 * <p>
 * A core part of this is that keybinds can be changed. Any class which is
 * interested in receiving updates on changes to keybinds should implement
 * {@code ValueChangeListener} and register itself with {link
 * {@link Mapper#addListener(ValueChangeListener)}.
 * <p>
 * Mappings will be read from a save file if possible, or a resource if the save
 * file is unavailable.
 * 
 * @author Frank Kormann
 */
public class InputMapper extends Mapper<Pair<Integer, Integer>> {

	private static final String SAVE_PATH = "/controls.json";
	private static final String DEFAULT_RESOURCE = "/controls_default.json";

	/**
	 * Creates an {@code InputMapper} with default key binds for
	 * {@code MovementInput}, {@code DirectionSelectorInput},
	 * {@code ResizingInput}, and {@code MetaInput}.
	 */
	public InputMapper() {
		super(SAVE_PATH, DEFAULT_RESOURCE);
	}

	@Override
	public TypeReference<EnumValues<Pair<Integer, Integer>>> getJsonTypeReference() {
		return new TypeReference<EnumValues<Pair<Integer, Integer>>>() {};
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Enum<?>>[] getEnumClasses() {
		return new Class[] { MovementInput.class, DirectionSelectorInput.class,
				SelectedSideResizingInput.class, AbsoluteResizingInput.class,
				MetaInput.class };
	}

	@Override
	public Pair<Integer, Integer> getDefaultValue() {
		return new Pair<Integer, Integer>(0, 0);
	}

	@Override
	public boolean allowUnset() {
		return true;
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
	}

	/**
	 * Associates the specified keyboard input with {@code input}.
	 * 
	 * @param input   enum value to set
	 * @param keybind {@code Pair} where the first value is a key code and
	 *                second value is modifier mask
	 */
	// Overridden for more descriptive Javadoc
	@Override
	public void set(Enum<?> input, Pair<Integer, Integer> keybind) {
		set(input, keybind.first, keybind.second);
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
	@Override
	public Pair<Integer, Integer> get(Enum<?> input) {
		return super.get(input);
	}

}
