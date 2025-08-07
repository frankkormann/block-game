package blockgame.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import blockgame.physics.Rectangle;

class PairTest {

	@Test
	void can_retrieve_values() {
		Object first = (Integer) 18;
		Object second = new ArrayList<Rectangle>();

		Pair<Object, Object> pair = new Pair<>(first, second);

		assertEquals(first, pair.first);
		assertEquals(second, pair.second);
	}

	@Test
	void are_equal_if_both_values_are_equal() {
		Pair<Integer, Integer> first = new Pair<>(1, 2);
		Pair<Integer, Integer> second = new Pair<>(1, 2);

		assertEquals(first, second);
	}

	@Test
	void are_not_equal_if_either_value_is_different() {
		Pair<Integer, Integer> oneTwo = new Pair<>(1, 2);
		Pair<Integer, Integer> oneThree = new Pair<>(1, 3);
		Pair<Integer, Integer> twoTwo = new Pair<>(2, 2);

		assertNotEquals(oneTwo, oneThree);
		assertNotEquals(oneTwo, twoTwo);
	}
}
