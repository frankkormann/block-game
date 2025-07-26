package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import game.GameInputHandler.DirectionSelectorInput;
import game.GameInputHandler.GameInput;
import game.GameInputHandler.ResizingInput;
import game.MainFrame.Direction;

class GameInputHandlerTest {

	GameInputHandler inputHandler;
	Pair<Map<Direction, Integer>, Set<GameInput>> inputs;

	@BeforeEach
	void setUp() {
		inputHandler = new GameInputHandler();
	}

	@Test
	void no_inputs_when_nothing_happens() {
		inputs = inputHandler.poll();

		assertResizes(inputs.first, 0, 0, 0, 0);
		assertTrue(inputs.second.isEmpty());
	}

	private void assertResizes(Map<Direction, Integer> resizes, int north,
			int south, int west, int east) {
		assertEquals(north, resizes.get(Direction.NORTH));
		assertEquals(south, resizes.get(Direction.SOUTH));
		assertEquals(west, resizes.get(Direction.WEST));
		assertEquals(east, resizes.get(Direction.EAST));
	}

	@Test
	void resizes_are_returned_and_nothing_else() {
		inputHandler.resize(100, Direction.WEST);
		inputHandler.resize(-50, Direction.SOUTH);

		inputs = inputHandler.poll();

		assertResizes(inputs.first, 0, -50, 100, 0);
		assertTrue(inputs.second.isEmpty());
	}

	private void pressKey(int keyCode, int modifiers) {
		inputHandler.keyPressed(new KeyEvent(new JLabel(), KeyEvent.KEY_PRESSED,
				1l, modifiers, keyCode, '\0'));
	}

	@Test
	void inputs_are_returned_and_nothing_else() {
		pressKey(GameInput.UP.keyCodes[0], 0);
		pressKey(GameInput.LEFT.keyCodes[0], 0);

		inputs = inputHandler.poll();

		assertResizes(inputs.first, 0, 0, 0, 0);
		assertTrue(inputs.second.contains(GameInput.UP));
		assertTrue(inputs.second.contains(GameInput.LEFT));
	}

	@Test
	void can_read_back_what_it_wrote() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		inputHandler.beginWriting(output);
		List<Pair<Map<Direction, Integer>, Set<GameInput>>> inputList = new ArrayList<>();

		pressKey(GameInput.RIGHT.keyCodes[0], 0);
		inputList.add(inputHandler.poll());

		inputHandler.resize(100, Direction.EAST);
		pressKey(GameInput.LEFT.keyCodes[0], 0);
		inputList.add(inputHandler.poll());

		inputList.add(inputHandler.poll());

		inputHandler.resize(10, Direction.SOUTH);
		inputList.add(inputHandler.poll());

		inputHandler.endWriting();

		ByteArrayInputStream input = new ByteArrayInputStream(
				output.toByteArray());
		inputHandler.beginReading(input);

		for (Pair<Map<Direction, Integer>, Set<GameInput>> expectedInputs : inputList) {
			inputs = inputHandler.poll();

			Map<Direction, Integer> resizes = expectedInputs.first;
			Set<GameInput> gameInputs = expectedInputs.second;
			assertResizes(inputs.first, resizes.get(Direction.NORTH),
					resizes.get(Direction.SOUTH), resizes.get(Direction.WEST),
					resizes.get(Direction.EAST));
			for (GameInput inp : GameInput.values()) {
				if (gameInputs.contains(inp)) {
					assertTrue(inputs.second.contains(inp));
				}
				else {
					assertFalse(inputs.second.contains(inp));
				}
			}
		}

		inputHandler.endReading();
	}

	@Test
	void selected_side_is_resized_north() {
		DirectionSelectorInput selection = DirectionSelectorInput.SELECT_NORTH;
		ResizingInput resizingInput = ResizingInput.INCREASE_VERTICAL;

		pressKey(selection.keyCode, selection.mask);
		pressKey(resizingInput.keyCode, 0);

		inputs = inputHandler.poll();

		assertEquals(resizingInput.amount, inputs.first.get(Direction.NORTH));
	}

	@Test
	void selected_side_is_resized__south() {
		DirectionSelectorInput selection = DirectionSelectorInput.SELECT_SOUTH;
		ResizingInput resizingInput = ResizingInput.INCREASE_VERTICAL;

		pressKey(selection.keyCode, selection.mask);
		pressKey(resizingInput.keyCode, 0);

		inputs = inputHandler.poll();

		assertEquals(resizingInput.amount, inputs.first.get(Direction.SOUTH));
	}

	@Test
	void selected_side_is_resized_west() {
		DirectionSelectorInput selection = DirectionSelectorInput.SELECT_WEST;
		ResizingInput resizingInput = ResizingInput.INCREASE_HORIZONTAL;

		pressKey(selection.keyCode, selection.mask);
		pressKey(resizingInput.keyCode, 0);

		inputs = inputHandler.poll();

		assertEquals(resizingInput.amount, inputs.first.get(Direction.WEST));
	}

	@Test
	void selected_side_is_resized_east() {
		DirectionSelectorInput selection = DirectionSelectorInput.SELECT_EAST;
		ResizingInput resizingInput = ResizingInput.INCREASE_HORIZONTAL;

		pressKey(selection.keyCode, selection.mask);
		pressKey(resizingInput.keyCode, 0);

		inputs = inputHandler.poll();

		assertEquals(resizingInput.amount, inputs.first.get(Direction.EAST));
	}

	@Test
	void no_vertical_resizing_when_a_horizontal_increase_is_used() {
		DirectionSelectorInput selection = DirectionSelectorInput.SELECT_NORTH;
		ResizingInput resizingInput = ResizingInput.INCREASE_HORIZONTAL;

		pressKey(selection.keyCode, selection.mask);
		pressKey(resizingInput.keyCode, 0);

		inputs = inputHandler.poll();

		assertEquals(0, inputs.first.get(Direction.NORTH));
	}

	@Test
	void no_horizontal_resizing_when_a_vertical_increase_is_used() {
		DirectionSelectorInput selection = DirectionSelectorInput.SELECT_WEST;
		ResizingInput resizingInput = ResizingInput.INCREASE_VERTICAL;

		pressKey(selection.keyCode, selection.mask);
		pressKey(resizingInput.keyCode, 0);

		inputs = inputHandler.poll();

		assertEquals(0, inputs.first.get(Direction.WEST));
	}
}
