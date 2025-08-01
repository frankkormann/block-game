package game;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;

import game.ParameterMapper.Parameter;

/**
 * {@code JPanel} which allows the user to remap values in
 * {@code ParameterMapper}, such as GUI scaling. Automatically calls
 * {@code ParameterMapper.save} the parent window is closed.
 */
public class ParameterChangerPanel extends JPanel
		implements ValueChangeListener {

	private static final String UNDO_TEXT = "Undo changes";
	private static final String RESET_TEXT = "Reset to defaults";

	private static final int VERTICAL_SPACE = 3;

	private ParameterMapper paramMapper;
	private Map<Parameter, SliderSpinner> paramToSliderSpinner;

	/**
	 * Creates a {@code ParameterChangerPanel} which will alter
	 * {@code paramMapper}.
	 * 
	 * @param rootPane    {@code JRootPane} of parent window
	 * @param paramMapper {@code ParameterMapper} to alter
	 */
	public ParameterChangerPanel(JRootPane rootPane,
			ParameterMapper paramMapper) {
		super();
		this.paramMapper = paramMapper;
		paramToSliderSpinner = new HashMap<>();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		SliderSpinner gameSpeed = new SliderSpinner(1, 100, 1, 1000, 5);
		bindSliderSpinner(gameSpeed, Parameter.GAME_SPEED);

		SliderSpinner gameScaling = new SliderSpinner(0.5f, 2f, 0.5f, 2f, 0.1f);
		bindSliderSpinner(gameScaling, Parameter.GAME_SCALING);

		SliderSpinner guiScaling = new SliderSpinner(0.5f, 2f, 0.5f, 2f, 0.1f);
		bindSliderSpinner(guiScaling, Parameter.GUI_SCALING);

		SliderSpinner hintOpacity = new SliderSpinner(0f, 1f, 0f, 1f, 0.1f);
		bindSliderSpinner(hintOpacity, Parameter.HINT_OPACITY);

		SliderSpinner keyboardingResizingAmount = new SliderSpinner(1, 10, 1,
				200, 1);
		bindSliderSpinner(keyboardingResizingAmount,
				Parameter.KEYBOARD_RESIZING_AMOUNT);

		SliderSpinner resizingAreaWidth = new SliderSpinner(10, 100, 0, 1000,
				5);
		bindSliderSpinner(resizingAreaWidth, Parameter.RESIZING_AREA_WIDTH);

		add(createSlidersPanel());
		add(createUndoResetButtonPanel());

		Window window = SwingUtilities.getWindowAncestor(rootPane);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cleanUp();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				cleanUp();
			}
		});
		paramMapper.addListener(this);
	}

	/**
	 * Creates a {@code JPanel} with a {@code JButton} to undo unsaved changes
	 * and a {@code JButton} to reset values back to defaults.
	 * 
	 * @return the {@code JPanel}
	 */
	private JPanel createUndoResetButtonPanel() {
		JPanel panel = new JPanel();

		JButton undoButton = new JButton(UNDO_TEXT);
		undoButton.addActionListener(e -> {
			paramMapper.reload();
		});

		JButton resetButton = new JButton(RESET_TEXT);
		resetButton.addActionListener(e -> {
			paramMapper.setToDefaults();
		});

		panel.add(undoButton);
		panel.add(resetButton);

		return panel;
	}

	/**
	 * Sets {@code sliderSpinner} to stick to and change the value of
	 * {@code param}.
	 * 
	 * @param sliderSpinner {@code SliderSpinner} to bind
	 * @param param         value to bind to in {@code ParameterMapper}
	 */
	private void bindSliderSpinner(SliderSpinner sliderSpinner,
			Parameter param) {
		sliderSpinner.setValue(paramMapper.get(param));
		sliderSpinner.addChangeListener(e -> {
			paramMapper.set(param, sliderSpinner.getValue());
		});
		paramToSliderSpinner.put(param, sliderSpinner);
	}

	/**
	 * Creates a {@code JPanel} that contains a {@code JLabel} and the
	 * {@code SliderSpinner} components for each {@code Parameter} that has been
	 * bound to a {@code SliderSpinner}.
	 * 
	 * @return the {@code JPanel}
	 */
	private JPanel createSlidersPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridy = 0;
		c.weighty = 0.5;

		for (Parameter param : paramToSliderSpinner.keySet()) {
			JLabel label = new JLabel(paramToName(param));
			label.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));

			SliderSpinner sliderSpinner = paramToSliderSpinner.get(param);

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
				return "Game Speed (FPS)";
			case GAME_SCALING:
				return "Game Scaling";
			case GUI_SCALING:
				return "GUI Scaling";
			case HINT_OPACITY:
				return "Hint Opacity";
			case KEYBOARD_RESIZING_AMOUNT:
				return "Keyboard resizing rate";
			case RESIZING_AREA_WIDTH:
				return "Window-edge resizing area size";
		}

		return param.toString();
	}

	private void cleanUp() {
		paramMapper.removeListener(this);
		paramMapper.save();
	}

	@Override
	public void valueChanged(Enum<?> key, Object newValue) {
		if (paramToSliderSpinner.containsKey(key)) {
			paramToSliderSpinner.get(key).setValue(paramMapper.get(key));
		}
	}

	@Override
	public void valueRemoved(Enum<?> key) {}

	/**
	 * Unifies a {@code JSlider} and {@code JSpinner} to coordinate their
	 * values.
	 */
	private class SliderSpinner {

		private JSlider slider;
		private JSpinner spinner;
		private int sliderScaling;

		public SliderSpinner(int sliderMin, int sliderMax, int spinnerMin,
				int spinnerMax, int spinnerStep) {
			sliderScaling = 1;
			slider = new JSlider(sliderMin, sliderMax);
			spinner = new JSpinner(new SpinnerNumberModel(spinnerMin,
					spinnerMin, spinnerMax, spinnerStep));

			connectComponents();
		}

		public SliderSpinner(float sliderMin, float sliderMax, float spinnerMin,
				float spinnerMax, float spinnerStep) {
			sliderScaling = 100;
			slider = new JSlider((int) (sliderMin * sliderScaling),
					(int) (sliderMax * sliderScaling));
			spinner = new JSpinner(new SpinnerNumberModel(spinnerMin,
					spinnerMin, spinnerMax, spinnerStep));

			connectComponents();
		}

		private void connectComponents() {
			slider.addChangeListener(e -> {
				if (sliderScaling == 1) {
					spinner.setValue(slider.getValue());
				}
				else {
					spinner.setValue(slider.getValue() / (float) sliderScaling);
				}
			});
			spinner.addChangeListener(e -> {
				slider.setValue(
						(int) (((Number) spinner.getValue()).doubleValue()
								* sliderScaling));
			});
		}

		public JSlider getSlider() {
			return slider;
		}

		public JSpinner getSpinner() {
			return spinner;
		}

		public void setValue(Number value) {
			spinner.setValue(value);
		}

		public Number getValue() {
			return (Number) spinner.getValue();
		}

		public void addChangeListener(ChangeListener listener) {
			spinner.addChangeListener(listener);
		}

	}
}