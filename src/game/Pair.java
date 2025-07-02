package game;

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

	public K key;
	public V value;

	@JsonCreator
	public Pair(@JsonProperty("key") K key, @JsonProperty("value") V value) {
		this.key = key;
		this.value = value;
	}

}
