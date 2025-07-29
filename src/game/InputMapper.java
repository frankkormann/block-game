package game;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import game.GameInputHandler.DirectionSelectorInput;
import game.GameInputHandler.GameInput;
import game.GameInputHandler.ResizingInput;
import game.MenuBar.MetaInput;

/**
 * Maps keyboard inputs to enumerated values. A keyboard input is a
 * {@code KeyEvent.VK_?} key code and a {@code KeyEvent.???_MASK} modifier mask,
 * or {@code 0} if no modifier keys should be held down.
 */
public class InputMapper {

	public static final int SHIFT_CONTROL_MASK = KeyEvent.CTRL_DOWN_MASK
			| KeyEvent.SHIFT_DOWN_MASK;

	private Map<Enum<?>, Pair<Integer, Integer>> inputToKeyBind;

	/**
	 * Creates an {@code InputMapper} with default key binds for
	 * {@code GameInput}, {@code DirectionSelectorInput}, {@code ResizingInput},
	 * and {@code MetaInput}.
	 */
	public InputMapper() {
		inputToKeyBind = new HashMap<>();

		// TODO Read these from a file instead
		setKeyBind(GameInput.UP, KeyEvent.VK_W, 0);
		setKeyBind(GameInput.LEFT, KeyEvent.VK_A, 0);
		setKeyBind(GameInput.RIGHT, KeyEvent.VK_D, 0);

		setKeyBind(DirectionSelectorInput.SELECT_NORTH, KeyEvent.VK_I,
				KeyEvent.SHIFT_DOWN_MASK);
		setKeyBind(DirectionSelectorInput.SELECT_SOUTH, KeyEvent.VK_K,
				KeyEvent.SHIFT_DOWN_MASK);
		setKeyBind(DirectionSelectorInput.SELECT_WEST, KeyEvent.VK_J,
				KeyEvent.SHIFT_DOWN_MASK);
		setKeyBind(DirectionSelectorInput.SELECT_EAST, KeyEvent.VK_L,
				KeyEvent.SHIFT_DOWN_MASK);
		setKeyBind(ResizingInput.INCREASE_VERTICAL, KeyEvent.VK_K, 0);
		setKeyBind(ResizingInput.INCREASE_HORIZONTAL, KeyEvent.VK_L, 0);
		setKeyBind(ResizingInput.DECREASE_VERTICAL, KeyEvent.VK_I, 0);
		setKeyBind(ResizingInput.DECREASE_HORIZONTAL, KeyEvent.VK_J, 0);

		setKeyBind(MetaInput.PAUSE, KeyEvent.VK_P, 0);
		setKeyBind(MetaInput.FRAME_ADVANCE, KeyEvent.VK_N, 0);
		setKeyBind(MetaInput.RELOAD_LEVEL, KeyEvent.VK_R, 0);
		setKeyBind(MetaInput.TOGGLE_HINTS, KeyEvent.VK_H, 0);
		setKeyBind(MetaInput.PLAY_SOLUTION, KeyEvent.VK_H, SHIFT_CONTROL_MASK);
		setKeyBind(MetaInput.SAVE_RECORDING, KeyEvent.VK_S, SHIFT_CONTROL_MASK);
		setKeyBind(MetaInput.PLAY_RECORDING, KeyEvent.VK_P, SHIFT_CONTROL_MASK);
		setKeyBind(MetaInput.STOP_RECORDING, KeyEvent.VK_S, 0);
	}

	/**
	 * Associates the specified keyboard input with {@code input}.
	 * 
	 * @param input     enum value to set
	 * @param keyCode   key code of keyboard input
	 * @param modifiers modifier mask of keyboard input
	 */
	public void setKeyBind(Enum<?> input, int keyCode, int modifiers) {
		inputToKeyBind.put(input, new Pair<>(keyCode, modifiers));
	}

	/**
	 * Returns the keyboard input associated with {@code input}.
	 * 
	 * @param input enum value to get
	 * 
	 * @return {@code Pair} where first value is the key code and second value
	 *         is the modifier mask
	 */
	public Pair<Integer, Integer> getKeyBind(Enum<?> input) {
		return inputToKeyBind.get(input);
	}

}
