package blockgame.input;

/**
 * Something which can get and set a value.
 * 
 * @param <T> type of value to set/get
 * 
 * @author Frank Kormann
 */
public interface GetterSetter<T> {

	/**
	 * Gets the value.
	 * 
	 * @return {@code T} which this stores
	 */
	public T get();

	/**
	 * Sets the value.
	 * 
	 * @param value {@code T} to seet
	 */
	public void set(T value);

	/**
	 * Adds a listener for when the value changes.
	 * 
	 * @param runnable what to do
	 */
	public void addChangeListener(Runnable runnable);

}
