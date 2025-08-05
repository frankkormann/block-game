package blockgame.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import blockgame.Pair;
import blockgame.SaveManager;
import blockgame.gui.MainFrame.Direction;
import blockgame.input.GameInputHandler;
import blockgame.input.InputMapper;
import blockgame.input.ParameterMapper;
import blockgame.input.GameInputHandler.DirectionSelectorInput;
import blockgame.input.GameInputHandler.MovementInput;
import blockgame.input.GameInputHandler.ResizingInput;

class GameInputHandlerTest {

	GameInputHandler inputHandler;
	InputMapper inputMapper;
	Pair<Map<Direction, Integer>, Set<MovementInput>> inputs;

	@BeforeEach
	void setUp(@TempDir Path dir) {
		SaveManager.setDirectory(dir.toString());
		inputMapper = new InputMapper();
		inputHandler = new GameInputHandler(inputMapper, new ParameterMapper());
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

	private void pressKey(Enum<?> input) {
		Pair<Integer, Integer> keybind = inputMapper.get(input);
		inputHandler.keyPressed(new KeyEvent(new JLabel(), KeyEvent.KEY_PRESSED,
				1l, keybind.second, keybind.first, '\0'));
	}

	@Test
	void inputs_are_returned_and_nothing_else() {
		pressKey(MovementInput.UP);
		pressKey(MovementInput.LEFT);

		inputs = inputHandler.poll();

		assertResizes(inputs.first, 0, 0, 0, 0);
		assertTrue(inputs.second.contains(MovementInput.UP));
		assertTrue(inputs.second.contains(MovementInput.LEFT));
	}

	@Test
	void can_read_back_what_it_wrote() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		inputHandler.beginWriting(output);
		List<Pair<Map<Direction, Integer>, Set<MovementInput>>> inputList = new ArrayList<>();

		pressKey(MovementInput.RIGHT);
		inputList.add(inputHandler.poll());

		inputHandler.resize(100, Direction.EAST);
		pressKey(MovementInput.LEFT);
		inputList.add(inputHandler.poll());

		inputList.add(inputHandler.poll());

		inputHandler.resize(10, Direction.SOUTH);
		inputList.add(inputHandler.poll());

		inputHandler.endWriting();

		ByteArrayInputStream input = new ByteArrayInputStream(
				output.toByteArray());
		inputHandler.beginReading(input);

		for (Pair<Map<Direction, Integer>, Set<MovementInput>> expectedInputs : inputList) {
			inputs = inputHandler.poll();

			Map<Direction, Integer> resizes = expectedInputs.first;
			Set<MovementInput> movementInputs = expectedInputs.second;
			assertResizes(inputs.first, resizes.get(Direction.NORTH),
					resizes.get(Direction.SOUTH), resizes.get(Direction.WEST),
					resizes.get(Direction.EAST));
			for (MovementInput inp : MovementInput.values()) {
				if (movementInputs.contains(inp)) {
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
		ResizingInput resizingInput = ResizingInput.MOVE_DOWN;

		pressKey(selection);
		pressKey(resizingInput);

		inputs = inputHandler.poll();

		assertNotEquals(0, inputs.first.get(Direction.NORTH));
	}

	@Test
	void selected_side_is_resized__south() {
		DirectionSelectorInput selection = DirectionSelectorInput.SELECT_SOUTH;
		ResizingInput resizingInput = ResizingInput.MOVE_DOWN;

		pressKey(selection);
		pressKey(resizingInput);

		inputs = inputHandler.poll();

		assertNotEquals(0, inputs.first.get(Direction.SOUTH));
	}

	@Test
	void selected_side_is_resized_west() {
		DirectionSelectorInput selection = DirectionSelectorInput.SELECT_WEST;
		ResizingInput resizingInput = ResizingInput.MOVE_RIGHT;

		pressKey(selection);
		pressKey(resizingInput);

		inputs = inputHandler.poll();

		assertNotEquals(0, inputs.first.get(Direction.WEST));
	}

	@Test
	void selected_side_is_resized_east() {
		DirectionSelectorInput selection = DirectionSelectorInput.SELECT_EAST;
		ResizingInput resizingInput = ResizingInput.MOVE_RIGHT;

		pressKey(selection);
		pressKey(resizingInput);

		inputs = inputHandler.poll();

		assertNotEquals(0, inputs.first.get(Direction.EAST));
	}

	@Test
	void no_vertical_resizing_when_a_horizontal_increase_is_used() {
		DirectionSelectorInput selection = DirectionSelectorInput.SELECT_NORTH;
		ResizingInput resizingInput = ResizingInput.MOVE_RIGHT;

		pressKey(selection);
		pressKey(resizingInput);

		inputs = inputHandler.poll();

		assertEquals(0, inputs.first.get(Direction.NORTH));
	}

	@Test
	void no_horizontal_resizing_when_a_vertical_increase_is_used() {
		DirectionSelectorInput selection = DirectionSelectorInput.SELECT_WEST;
		ResizingInput resizingInput = ResizingInput.MOVE_DOWN;

		pressKey(selection);
		pressKey(resizingInput);

		inputs = inputHandler.poll();

		assertEquals(0, inputs.first.get(Direction.WEST));
	}
}
