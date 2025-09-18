package blockgame.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;

import blockgame.gui.ErrorDialog;
import blockgame.util.SaveManager;

/**
 * Maps enumerated values to objects of type {@code T}. The mapping can be
 * serialized/deserialized as JSON.
 * <p>
 * Mappings can be read from both a {@code File} or a resource, but can only be
 * written to the {@code File}. The resource acts as a source of default values
 * for the mapping. The default implementation is to read from the save file if
 * possible and use the resource as a backup.
 * 
 * @param <T> type to map enum values to
 */
public abstract class Mapper<T> {

	private Map<Enum<?>, T> valuesMap;
	private Map<Enum<?>, T> defaultValues;
	private List<ValueChangeListener> changeListeners;

	private String savePath;

	public Mapper(String savePath, String defaultValuesResource) {
		valuesMap = new HashMap<>();
		changeListeners = new ArrayList<>();
		this.savePath = savePath;

		try (InputStream stream = getClass()
				.getResourceAsStream(defaultValuesResource)) {
			defaultValues = readValues(stream).values;
		}
		catch (IOException | IllegalArgumentException e) {
			e.printStackTrace();
			ErrorDialog.showDialog("Default values file is unavailable", e);
		}

		loadFromFile();
	}

	/**
	 * Returns a {@code TypeReference} of type
	 * {@code TypeReference<EnumValues<T>>}.
	 * <p>
	 * This is necessary due to type erasure. The {@code Mapper} superclass does
	 * not know what type {@code T} is, so it cannot construct a suitable
	 * {@code TypeReference}.
	 * <p>
	 * An example implementation would be, if {@code T} is {@code Integer}:
	 * 
	 * <pre>
	 * public TypeReference&lt;EnumValues&lt;Integer&gt;&gt; getJsonTypeReference() {
	 * 	return new TypeReference&lt;EnumValues&lt;Integer&gt;&gt;() {};
	 * }
	 * </pre>
	 * 
	 * @return the {@code TypeReference<EnumValues<T>>}
	 */
	public abstract TypeReference<EnumValues<T>> getJsonTypeReference();

	/**
	 * Gets the {@code Enum} subclasses which will be used to retrieve actual
	 * enum values when reading JSON.
	 * 
	 * @return array of {@code Enum} subclasses
	 */
	public abstract Class<? extends Enum<?>>[] getEnumClasses();

	/**
	 * Value to use if the default values file cannot be read.
	 * 
	 * @return the ultra-default value
	 */
	public abstract T getDefaultValue();

	/**
	 * Whether values are allowed to not be set. Note that this does not
	 * guarantee that all values will be set.
	 * 
	 * @return {@code true} if values can be unset
	 */
	public abstract boolean allowUnset();

	/**
	 * Save all values to disk.
	 */
	public void save() {
		ObjectMapper mapper = new ObjectMapper();
		EnumValues<T> json = new EnumValues<>();
		json.values = valuesMap;
		try (OutputStream out = SaveManager.writeFile(savePath)) {
			mapper.writeValue(out, json);
		}
		catch (IOException e) {
			e.printStackTrace();
			ErrorDialog.showDialog("Failed to save to " + savePath, e);
		}
	}

	/**
	 * Loads values from {@code stream}.
	 * 
	 * @param stream {@code InputStream} to read from
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	private void load(InputStream stream) throws IOException {
		EnumValues<T> json = readValues(stream);

		for (Enum<?> key : json.values.keySet()) {
			T value = json.values.get(key);
			set(key, value);
		}
	}

	/**
	 * For each key, if it not already set, loads its default value.
	 */
	private void loadUnset() {
		for (Class<? extends Enum<?>> enumClass : getEnumClasses()) {
			for (Enum<?> key : enumClass.getEnumConstants()) {
				if (get(key) == null) {
					setToDefault(key);
				}
			}
		}
	}

	private EnumValues<T> readValues(InputStream stream) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setInjectableValues(new InjectableValues.Std()
				.addValue("enumClasses", getEnumClasses()));
		return mapper.readValue(stream, getJsonTypeReference());
	}

	/**
	 * Sets all values to their default values.
	 */
	public void setToDefaults() {
		for (Class<? extends Enum<?>> enumClass : getEnumClasses()) {
			for (Enum<?> key : enumClass.getEnumConstants()) {
				setToDefault(key);
			}
		}
	}

	/**
	 * Sets the value associated with {@code key} to its default value.
	 * 
	 * @param key enum value to reset
	 */
	public void setToDefault(Enum<?> key) {
		if (defaultValues == null) {
			System.err.println(
					"defaultValues is null in " + getClass().getSimpleName());
			set(key, getDefaultValue());
			return;
		}
		if (!defaultValues.containsKey(key)) {
			System.err.println("No default value set for " + key + " in "
					+ getClass().getSimpleName());
			set(key, getDefaultValue());
			return;
		}
		set(key, defaultValues.get(key));
	}

	/**
	 * Reload keybinds without saving changes.
	 */
	public void reload() {
		loadFromFile();
	}

	private void loadFromFile() {
		InputStream fileStream = SaveManager.readFile(savePath);
		if (fileStream == null) {
			setToDefaults();
			save();
		}
		else {
			try {
				load(fileStream);
				fileStream.close();
			}
			catch (IOException e) {
				e.printStackTrace();
				ErrorDialog.showDialog(
						"Can't read saved values, resetting to defaults", e);
				setToDefaults();
				save();
			}
		}

		if (!allowUnset()) {
			loadUnset();
		}
	}

	/**
	 * Associates the specified value with {@code key}.
	 * 
	 * @param key   enum value to set
	 * @param value value to set
	 */
	public void set(Enum<?> key, T value) {
		valuesMap.put(key, value);

		for (ValueChangeListener listener : changeListeners) {
			listener.valueChanged(key, value);
		}
	}

	/**
	 * Returns the value associated with {@code key}. Returns {@code null} if
	 * there is no set value for {@code key}.
	 * 
	 * @param key enum value to get
	 * 
	 * @return value of type {@code T}
	 */
	public T get(Enum<?> key) {
		return valuesMap.get(key);
	}

	/**
	 * Removes {@code key} and its associated value.
	 * <p>
	 * Throws {@code UnsupportedOperationException} if values must be set (that
	 * is, if {@link #allowUnset()} returns {@code false}).
	 * 
	 * @param key enum value to remove
	 * 
	 * @see #allowUnset()
	 */
	public void remove(Enum<?> key) {
		if (!allowUnset()) {
			throw new UnsupportedOperationException("Values must be set");
		}
		valuesMap.remove(key);

		for (ValueChangeListener listener : changeListeners) {
			listener.valueRemoved(key);
		}
	}

	/**
	 * Registers {@code listener} to receive value change events.
	 * 
	 * @param listener {@code ValueChangeListener} to add
	 */
	public void addListener(ValueChangeListener listener) {
		changeListeners.add(listener);
	}

	/**
	 * Unregisters {@code listener} from receiving value change events.
	 * 
	 * @param listener {@code ValueChangeListener} to remove
	 */
	public void removeListener(ValueChangeListener listener) {
		changeListeners.remove(listener);
	}

}
