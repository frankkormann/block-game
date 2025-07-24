package game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.event.KeyEvent;

import javax.swing.JLabel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import game.MetaInputHandler.MetaInput;
import mocks.MetaInputProcessingMock;

class MetaInputHandlerTest {

	MetaInputHandler inputHandler;
	MetaInputProcessingMock inputProcessor;

	@BeforeEach
	void setUp() {
		inputProcessor = new MetaInputProcessingMock();
		inputHandler = new MetaInputHandler(inputProcessor);
	}

	private void pressKey(MetaInput input, int extraMasks) {
		inputHandler.keyPressed(new KeyEvent(new JLabel(), KeyEvent.KEY_PRESSED,
				1l, input.mask + extraMasks, input.keyCode, '\0'));
	}

	@Test
	void each_non_file_input_is_reported_correctly() {
		MetaInput[] inputs = { MetaInput.FRAME_ADVANCE, MetaInput.PAUSE,
				MetaInput.RELOAD_LEVEL, MetaInput.STOP_RECORDING,
				MetaInput.TOGGLE_HINTS, MetaInput.PLAY_SOLUTION };

		for (MetaInput inp : inputs) {
			pressKey(inp, 0);
			assertEquals(inp, inputProcessor.lastInputProcessed);
			assertEquals(false, inputProcessor.hadFile);
		}
	}

	@Test
	void mouse_buttons_dont_affect_input_masks() {
		MetaInput[] inputs = { MetaInput.FRAME_ADVANCE, MetaInput.PAUSE,
				MetaInput.RELOAD_LEVEL, MetaInput.STOP_RECORDING,
				MetaInput.TOGGLE_HINTS, MetaInput.PLAY_SOLUTION };

		for (MetaInput inp : inputs) {
			pressKey(inp, KeyEvent.BUTTON1_DOWN_MASK);
			assertEquals(inp, inputProcessor.lastInputProcessed);
			assertEquals(false, inputProcessor.hadFile);
		}
	}

	// TODO Figure out how to test inputs that involve a file select GUI
}
