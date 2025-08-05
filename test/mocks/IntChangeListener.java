package mocks;

import blockgame.Pair;
import blockgame.input.ValueChangeListener;

public class IntChangeListener implements ValueChangeListener {

	public String lastEvent;
	public Pair<Enum<?>, Integer> eventInfo;

	public IntChangeListener() {
		lastEvent = "";
		eventInfo = new Pair<>(null, null);
	}

	@Override
	public void valueChanged(Enum<?> key, Object newValue) {
		lastEvent = "valueChanged";
		eventInfo.first = key;
		eventInfo.second = (Integer) newValue;
	}

	@Override
	public void valueRemoved(Enum<?> key) {
		lastEvent = "valueRemoved";
		eventInfo.first = key;
		eventInfo.second = null;
	}

}
