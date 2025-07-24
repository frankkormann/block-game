package mocks;

import java.io.File;

import game.GameController;
import game.MetaInputHandler.MetaInput;

public class MetaInputProcessingMock extends GameController {

	public MetaInput lastInputProcessed;
	public boolean hadFile;

	public MetaInputProcessingMock() {
		lastInputProcessed = null;
		hadFile = false;
	}

	@Override
	public void processMetaInput(MetaInput input)
			throws IllegalArgumentException {
		lastInputProcessed = input;
		hadFile = false;
	}

	@Override
	public void processMetaInput(MetaInput input, File file)
			throws IllegalArgumentException {
		lastInputProcessed = input;
		hadFile = file != null;
	}
}
