package blockgame.util;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds two values.
 * 
 * @param <F> type of first value
 * @param <S> type of second value
 * 
 * @author Frank Kormann
 */
public class Pair<F, S> {

	@JsonAlias({ "area", "path" })
	public F first;
	@JsonAlias({ "options", "number" })
	public S second;

	@JsonCreator
	public Pair(@JsonProperty("first") F first,
			@JsonProperty("second") S second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Pair)) {
			return false;
		}
		return first.equals(((Pair<?, ?>) other).first)
				&& second.equals(((Pair<?, ?>) other).second);
	}

}
