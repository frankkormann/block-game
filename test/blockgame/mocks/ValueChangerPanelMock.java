package blockgame.mocks;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JRootPane;

import blockgame.input.GetterSetter;
import blockgame.input.Mapper;
import blockgame.input.ValueChangerPanel;

public class ValueChangerPanelMock extends ValueChangerPanel<Integer> {

	public Map<Enum<?>, GetterSetter<Integer>> getterSetters;

	public ValueChangerPanelMock(JRootPane rootPane, Mapper<Integer> mapper) {
		super(rootPane, mapper);
	}

	@Override
	protected GetterSetter<Integer> createGetterSetter(Enum<?> enumValue) {
		if (getterSetters == null) {
			getterSetters = new HashMap<>();
		}
		GetterSetter<Integer> getterSetter = new IntGetterSetter();
		getterSetters.put(enumValue, getterSetter);
		return getterSetter;
	}

	@Override
	protected JPanel createGetterSetterPanel(
			Map<Enum<?>, GetterSetter<Integer>> getterSetters) {
		return new JPanel();  // TODO Figure out a way to test GUI elements
	}

	private class IntGetterSetter implements GetterSetter<Integer> {

		int value;
		Runnable changeListener;

		public IntGetterSetter() {
			value = 0;
			changeListener = () -> {};
		}

		@Override
		public Integer get() {
			return value;
		}

		@Override
		public void set(Integer value) {
			boolean valueChanged = value != this.value;
			this.value = value;
			if (valueChanged) {
				changeListener.run();
			}
		}

		@Override
		public void addChangeListener(Runnable runnable) {
			changeListener = runnable;
		}

	}

}
