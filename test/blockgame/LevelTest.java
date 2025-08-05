package blockgame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LevelTest {

	@Test
	void all_fields_empty_and_not_null_by_default() {
		Level level = new Level();

		assertEquals("", level.name);
		assertEquals(0, level.width);
		assertEquals(0, level.height);
		assertEquals("", level.solution);
		assertTrue(level.movingRectangles.isEmpty());
		assertTrue(level.walls.isEmpty());
		assertTrue(level.areas.isEmpty());
		assertTrue(level.hints.isEmpty());
	}
}
