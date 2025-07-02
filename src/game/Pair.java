package game;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds two values.
 * 
 * @param <K>
 * @param <V>
 * 
 * @author Frank Kormann
 */
public class Pair<K, V> {

	@JsonAlias({ "area" })
	public K key;
	@JsonAlias({ "options" })
	public V value;

	@JsonCreator
	public Pair(@JsonProperty("key") K key, @JsonProperty("value") V value) {
		this.key = key;
		this.value = value;
	}

}
