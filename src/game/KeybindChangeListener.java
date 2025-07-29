package game;

/**
 * Listener interface for receiving keybind change events. An implementing class
 * should register itself with {@code InputMapper}'s {@code addKeybindListener}
 * method to receive events.
 */
public interface KeybindChangeListener {

	/**
	 * Fired when a keybind is changed.
	 * 
	 * @param input        enum value which has changed
	 * @param newKeyCode   new key code for the keyboard input
	 * @param newModifiers new modifier mask for the keyboard input
	 */
	public void keybindChanged(Enum<?> input, int newKeyCode, int newModifiers);
}
