package game;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Container for enum-value map information while reading/writing JSON.
 * 
 * @param <T> type of value to store
 * 
 * @author Frank Kormann
 */
public class EnumValues<T> {

	public Map<Enum<?>, T> values;
	@JsonIgnore
	@JacksonInject("enumClasses")
	public Class<? extends Enum<?>>[] enumClasses;

	/**
	 * Note: {@code enumClasses} must be set if this is going to be used to read
	 * JSON.
	 */
	public EnumValues() {
		values = new HashMap<>();
	}

	public void setValues(Map<String, T> values) throws IOException {
		for (Entry<String, T> entry : values.entrySet()) {
			try {
				this.values.put(getEnum(entry.getKey()), entry.getValue());
			}
			catch (IllegalArgumentException e) {
				throw new IOException(e);
			}
		}
	}

	private Enum<?> getEnum(String name) throws IllegalArgumentException {
		Enum<?> result = null;
		for (Class<? extends Enum> enumClass : enumClasses) {
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
