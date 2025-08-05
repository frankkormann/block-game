package game;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import game.ParameterMapper.Parameter;

/**
 * {@code JPanel} which allows the user to remap values in
 * {@code ParameterMapper}, such as GUI scaling. Automatically calls
 * {@code ParameterMapper.save} the parent window is closed.
 * 
 * @author Frank Kormann
 */
public class ParameterChangerPanel extends ValueChangerPanel<Number> {

	private static final int VERTICAL_SPACE = 3;

	/**
	 * Creates a {@code ParameterChangerPanel} which will alter
	 * {@code paramMapper}.
	 * 
	 * @param rootPane    {@code JRootPane} of parent window
	 * @param paramMapper {@code ParameterMapper} to alter
	 */
	public ParameterChangerPanel(JRootPane rootPane,
			ParameterMapper paramMapper) {
		super(rootPane, paramMapper);
	}

	@Override
	protected GetterSetter<Number> createGetterSetter(Enum<?> enumValue) {
		if (enumValue == Parameter.GAME_SPEED) {
			return new SliderSpinner(1, 100, 1, 60000, 5, false, "ms");
		}
		if (enumValue == Parameter.GAME_SCALING) {
			return new SliderSpinner(50, 200, 20, 500, 10, true, "%");
		}
		if (enumValue == Parameter.GUI_SCALING) {
			SliderSpinner guiScaling = new SliderSpinner(50, 200, 20, 500, 10,
					true, "%");
			guiScaling.setValueIsAdjustingListener(() -> {
				SwingUtilities.invokeLater(() -> {
					Window window = SwingUtilities.getWindowAncestor(this);
					SwingUtilities.updateComponentTreeUI(window);
					window.pack();
				});
			});
			return guiScaling;
		}
		if (enumValue == Parameter.OPACITY_MULTIPLIER) {
			return new SliderSpinner(0, 100, 0, 100, 10, true, "%");
		}
		if (enumValue == Parameter.KEYBOARD_RESIZING_AMOUNT) {
			return new SliderSpinner(1, 10, 1, 50, 1, false, "px");
		}
		if (enumValue == Parameter.RESIZING_AREA_WIDTH) {
			return new SliderSpinner(10, 100, 0, 200, 5, false, "px");
		}

		return null;
	}

	@Override
	protected JPanel createGetterSetterPanel(
			Map<Enum<?>, GetterSetter<Number>> getterSetters) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridy = 0;
		c.insets = new Insets(VERTICAL_SPACE, 0, 0, 0);
		c.weighty = 0.5;

		for (Parameter param : Parameter.values()) {
			if (!getterSetters.containsKey(param)) {
				continue;
			}

			JLabel label = new JLabel(paramToName(param));
			label.setToolTipText(paramToTooltip(param));
			label.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));

			SliderSpinner sliderSpinner = (SliderSpinner) getterSetters
					.get(param);

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
	 * Returns a user-understandable for {@code param}.
	 * 
	 * @param param value to create a name for
	 * 
	 * @return {@code param}'s name
	 */
	private String paramToName(Parameter param) {
		switch (param) {
			case GAME_SPEED:
				return "Game Speed";
			case GAME_SCALING:
				return "Game Scaling";
			case GUI_SCALING:
				return "GUI Scaling";
			case OPACITY_MULTIPLIER:
				return "Opacity of Transparent Blocks";
			case KEYBOARD_RESIZING_AMOUNT:
				return "Keyboard Resizing Rate";
			case RESIZING_AREA_WIDTH:
				return "Window-edge Resizing Area Size";
		}

		return param.toString();
	}

	/**
	 * Returns tooltip-text which further explains how {@code param} affects the
	 * game.
	 * 
	 * @param param value to explain
	 * 
	 * @return tooltip-text for {@code param}
	 */
	private String paramToTooltip(Parameter param) {
		switch (param) {
			case GAME_SPEED:
				return "Number of milliseconds between each frame\nLower is faster";
			case GAME_SCALING:
				return "Multiplier for size of game objects";
			case GUI_SCALING:
				return "Multiplier for size of GUI elements";
			case OPACITY_MULTIPLIER:
				return "Alpha multiplier for blocks with transparent parts, such as hints\nLower is more transparent";
			case KEYBOARD_RESIZING_AMOUNT:
				return "Amount the window will be resized with each keyboard input";
			case RESIZING_AREA_WIDTH:
				return "Width of the window regions that can be click-dragged to resize it";
		}

		return param.toString();
	}

	/**
	 * Unifies a {@code JSlider} and {@code JSpinner} to coordinate their
	 * values.
	 */
	private class SliderSpinner implements GetterSetter<Number> {

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
			spinner.setEditor(
					new JSpinner.NumberEditor(spinner, "# '" + suffix + "'"));

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

		public JSlider getSlider() {
			return slider;
		}

		public JSpinner getSpinner() {
			return spinner;
		}

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

		public Number get() {
			if (isPercent) {
				return (int) spinner.getValue() / 100.0;
			}
			return (int) spinner.getValue();
		}

		public void addChangeListener(Runnable listener) {
			spinner.addChangeListener(e -> listener.run());
		}

		public void setValueIsAdjustingListener(Runnable runnable) {
			valueIsAdjustingListener = runnable;
		}

	}

}
