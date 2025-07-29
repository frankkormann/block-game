package game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.event.KeyEvent;

import org.junit.jupiter.api.Test;

import game.MainFrame.Direction;

public class InputMapperTest {

	@Test
	void can_get_what_is_set() {
		InputMapper inputMapper = new InputMapper();

		int keyCode = KeyEvent.VK_A;
		int modifiers = KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK;
		inputMapper.setKeyBind(Direction.NORTH, keyCode, modifiers);

		Pair<Integer, Integer> actual = inputMapper.getKeyBind(Direction.NORTH);
		assertEquals(keyCode, actual.first);
		assertEquals(modifiers, actual.second);
	}
}
