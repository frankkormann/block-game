package blockgame.input;

/**
 * Listener interface for receiving valye change events. An implementing class
 * should register itself with {@code Mapper}'s {@code addChangeListener} method
 * to receive events.
 * 
 * @author Frank Kormann
 */
public interface ValueChangeListener {

	/**
	 * Fired when a value is changed or added.
	 * 
	 * @param key      enum key which has changed
	 * @param newValue new value for the enum
	 */
	public void valueChanged(Enum<?> key, Object newValue);

	/**
	 * Fired when a key is removed.
	 * 
	 * @param key enum key which no longer has a value
	 */
	public void valueRemoved(Enum<?> key);
}
