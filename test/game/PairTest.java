package game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class PairTest {

	@Test
	void can_retrieve_values() {
		Object first = (Integer) 18;
		Object second = new ArrayList<Rectangle>();

		Pair<Object, Object> pair = new Pair<>(first, second);

		assertEquals(first, pair.first);
		assertEquals(second, pair.second);
	}
}
