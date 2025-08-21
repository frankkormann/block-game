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

	private Map<Enum<?>, T> enumMap;
	private List<ValueChangeListener> changeListeners;

	private String savePath;
	private String defaultValuesResource;

	public Mapper(String savePath, String defaultValuesResource) {
		enumMap = new HashMap<>();
		changeListeners = new ArrayList<>();
		this.savePath = savePath;
		this.defaultValuesResource = defaultValuesResource;

		loadFromFile();
	}

	/**
	 * Returns a {@code TypeReference} of type
	 * {@code TypeReference<EnumValues<T>>}.
	 * <p>
	 * This is necessary due to Java type erasure. The {@code Mapper} superclass
	 * does not know what type {@code T} is, so it cannot construct a suitable
	 * {@code TypeReference}.
	 * <p>
	 * An example implementation would be, if {@code T} is {@code Integer}:
	 * 
	 * <pre>
	 * public TypeReference&lt;EnumValues&lt;T&gt;&gt; getJsonTypeReference() {
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
	 * Save all values to disk.
	 */
	public void save() {
		ObjectMapper mapper = new ObjectMapper();
		EnumValues<T> json = new EnumValues<>();
		json.values = enumMap;
		try {
			OutputStream out = SaveManager.writeFile(savePath);
			mapper.writeValue(out, json);
			try {
				out.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Failed to save to " + savePath, e)
					.setVisible(true);
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
	 * Loads values from {@code stream} if the value is not already set.
	 * 
	 * @param stream {@code InputStream} to read from
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	private void loadUnset(InputStream stream) throws IOException {
		EnumValues<T> json = readValues(stream);

		for (Enum<?> key : json.values.keySet()) {
			if (get(key) == null) {
				T value = json.values.get(key);
				set(key, value);
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
		try {
			load(getClass().getResourceAsStream(defaultValuesResource));
		}
		catch (IOException | IllegalArgumentException e) {
			e.printStackTrace();
			new ErrorDialog("Error",
					"Default values file is unavailable, go to Options to set values manually",
					e).setVisible(true);

			for (Class<? extends Enum> enumClass : getEnumClasses()) {
				for (Enum<?> key : enumClass.getEnumConstants()) {
					set(key, getDefaultValue());
				}
			}
		}
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
			fileStream = SaveManager.readFile(savePath);
		}

		try {
			load(fileStream);
			fileStream.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error",
					"Can't read saved values, resetting to defaults", e)
					.setVisible(true);
			setToDefaults();
			save();
		}

		try {
			loadUnset(getClass().getResourceAsStream(defaultValuesResource));
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error",
					"Can't read default values file. Some values may not be set.",
					e).setVisible(true);
		}
	}

	/**
	 * Associates the specified value with {@code key}.
	 * 
	 * @param key   enum value to set
	 * @param value value to set
	 */
	public void set(Enum<?> key, T value) {
		enumMap.put(key, value);

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
		return enumMap.get(key);
	}

	/**
	 * Removes {@code key} and its associated value.
	 * 
	 * @param key enum value to remove
	 */
	public void remove(Enum<?> key) {
		enumMap.remove(key);

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
