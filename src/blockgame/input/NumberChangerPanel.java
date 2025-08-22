package blockgame.input;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * {@code ValueChangerPanel} which allows the user to remap numerical values
 * with {@code JSlider}s and {@code JSpinner}s.
 * 
 * @author Frank Kormann
 */
public abstract class NumberChangerPanel extends ValueChangerPanel<Number> {

	private static final int EDGE_SPACE = 3;

	/**
	 * Creates a {@code ParameterChangerPanel} which will alter
	 * {@code paramMapper}.
	 * 
	 * @param rootPane    {@code JRootPane} of parent window
	 * @param paramMapper {@code ParameterMapper} to alter
	 */
	public NumberChangerPanel(JRootPane rootPane, Mapper<Number> mapper) {
		super(rootPane, mapper);
	}

	/**
	 * Returns a {@code SliderSpinner} with suitable bounds and settings for
	 * {@code enumValue}.
	 * 
	 * @param enumValue value to create a {@code SliderSpinner} for
	 * 
	 * @return the {@code SliderSpinner}
	 */
	protected abstract SliderSpinner createSliderSpinner(Enum<?> enumValue);

	/**
	 * Returns a user-understandable for {@code enumValue}.
	 * 
	 * @param enumValue value to create a name for
	 * 
	 * @return {@code enumValue}'s name
	 */
	protected abstract String paramToName(Enum<?> enumValue);

	/**
	 * Returns tooltip-text which further explains how {@code enumValue} affects
	 * the game.
	 * 
	 * @param enumValue value to explain
	 * 
	 * @return tooltip-text for {@code enumValue}
	 */
	protected abstract String paramToTooltip(Enum<?> enumValue);

	/**
	 * Returns the enum values which this is adjusting.
	 * 
	 * @return the {@code Enum[]}
	 */
	protected abstract Enum[] getEnumValues();

	@Override
	protected GetterSetter<Number> createGetterSetter(Enum<?> enumValue) {
		return createSliderSpinner(enumValue);
	}

	@Override
	protected JPanel createGetterSetterPanel(
			Map<Enum<?>, GetterSetter<Number>> getterSetters) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridy = 0;
		c.insets = new Insets(EDGE_SPACE, EDGE_SPACE, EDGE_SPACE, EDGE_SPACE);
		c.weightx = 0.5;
		c.weighty = 0.5;

		for (Enum<?> enumValue : getEnumValues()) {
			if (!getterSetters.containsKey(enumValue)) {
				continue;
			}

			JLabel label = new JLabel(paramToName(enumValue));
			label.setToolTipText(paramToTooltip(enumValue));

			SliderSpinner sliderSpinner = (SliderSpinner) getterSetters
					.get(enumValue);

			c.gridx = 0;
			panel.add(label, c);
			c.gridx = 1;
			panel.add(sliderSpinner.getSlider(), c);
			c.gridx = 2;
			panel.add(sliderSpinner.getSpinner(), c);

			c.gridy++;
		}

		return panel;
	}

	/**
	 * Unifies a {@code JSlider} and {@code JSpinner} to coordinate their
	 * values.
	 */
	protected class SliderSpinner implements GetterSetter<Number> {

		private JSlider slider;
		private JSpinner spinner;
		private boolean isPercent;

		private Runnable valueIsAdjustingListener;

		public SliderSpinner(int sliderMin, int sliderMax, int spinnerMin,
				int spinnerMax, int spinnerStep, boolean isPercent,
				String suffix) {
			slider = new JSlider(sliderMin, sliderMax);
			spinner = new JSpinner(new SpinnerNumberModel(spinnerMin,
					spinnerMin, spinnerMax, spinnerStep));
			this.isPercent = isPercent;
			valueIsAdjustingListener = null;

			slider.setValue((int) spinner.getValue());
			spinner.setEditor(new LabeledNumberEditor(spinner, suffix));

			slider.addChangeListener(e -> {
				spinner.setValue(slider.getValue());
				if (!slider.getValueIsAdjusting()
						&& valueIsAdjustingListener != null) {
					valueIsAdjustingListener.run();
				}
			});
			spinner.addChangeListener(e -> {
				slider.setValue((int) spinner.getValue());
			});
		}

		private JSlider getSlider() {
			return slider;
		}

		private JSpinner getSpinner() {
			return spinner;
		}

		@Override
		public void set(Number value) {
			int intValue;
			if (isPercent) {
				if (Math.abs(
						value.doubleValue() - get().doubleValue()) < 0.01) {
					return;
				}
				intValue = (int) (value.doubleValue() * 100);
			}
			else {
				intValue = value.intValue();
			}

			slider.setValue(intValue);
			spinner.setValue(intValue);
		}

		@Override
		public Number get() {
			if (isPercent) {
				return (int) spinner.getValue() / 100.0;
			}
			return (int) spinner.getValue();
		}

		@Override
		public void addChangeListener(Runnable listener) {
			spinner.addChangeListener(e -> listener.run());
		}

		public void setValueIsAdjustingListener(Runnable runnable) {
			valueIsAdjustingListener = runnable;
		}

		/**
		 * {@code NumberEditor} with a {@code JLabel} to display a unit which is
		 * not part of the {@code JFormattedTextField} that the user types into.
		 */
		private class LabeledNumberEditor extends JSpinner.NumberEditor {

			private LabeledNumberEditor(JSpinner spinner, String unit) {
				super(spinner);
				setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

				MouseAdapter focusTextField = new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						getTextField().requestFocus();
					}
				};
				Cursor textCursor = Cursor
						.getPredefinedCursor(Cursor.TEXT_CURSOR);

				setCursor(textCursor);
				addMouseListener(focusTextField);

				JLabel unitLabel = new JLabel(unit);
				unitLabel.setCursor(textCursor);
				unitLabel.addMouseListener(focusTextField);

				add(unitLabel);
				add(Box.createHorizontalStrut(10));
			}

		}

	}
}
