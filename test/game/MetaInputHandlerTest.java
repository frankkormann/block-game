package game;

import static org.junit.Assert.assertEquals;

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

	private void pressKey(MetaInput input) {
		inputHandler.keyPressed(new KeyEvent(new JLabel(), KeyEvent.KEY_PRESSED, 1l,
				input.mask, input.keyCode, '\0'));
	}

	private void pressInputAndAssertIt(MetaInput input, boolean shouldHaveFile) {
		pressKey(input);
		assertEquals(input, inputProcessor.lastInputProcessed);
		assertEquals(shouldHaveFile, inputProcessor.hadFile);
	}

	@Test
	void each_non_file_input_is_reported_correctly() {
		MetaInput[] inputs = { MetaInput.FRAME_ADVANCE, MetaInput.PAUSE,
				MetaInput.RELOAD_LEVEL, MetaInput.STOP_RECORDING,
				MetaInput.TOGGLE_HINTS, MetaInput.PLAY_SOLUTION };

		for (MetaInput inp : inputs) {
			pressInputAndAssertIt(inp, false);
		}
	}

	// TODO Figure out how to test inputs that involve a file select GUI
}
