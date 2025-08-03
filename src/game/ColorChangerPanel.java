package game;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import game.Rectangle.Colors;

/**
 * {@code JPanel} which allows the user to change the color values in
 * {@code ColorMapper}. Automatically calls {@code ColorMapper.save} when the
 * parent window is closed.
 * 
 * @author Frank Kormann
 */
public class ColorChangerPanel extends ValueChangerPanel<Integer> {

	private static final int VERTICAL_SPACE = 3;
	private static final Colors DISALLOW_CHANGING = Colors.TRANSPARENT;

	/**
	 * Creates a {@code ColorChangePanel} which will change the color mappings
	 * in {@code colorMapper}.
	 * 
	 * @param rootPane    {@code JRootPane} of {@code Window} this will be added
	 *                    to
	 * @param colorMapper {@code ColorMapper} to alter
	 */
	public ColorChangerPanel(JRootPane rootPane, ColorMapper colorMapper) {
		super(rootPane, colorMapper);
	}

	@Override
	protected GetterSetter<Integer> createGetterSetter(Enum<?> enumValue) {
		return new ColorButton();
	}

	@Override
	protected JPanel createGetterSetterPanel(
			Map<Enum<?>, GetterSetter<Integer>> getterSetters) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridy = 0;
		c.insets = new Insets(VERTICAL_SPACE, 0, 0, 0);
		c.weightx = 0.5;
		c.weighty = 0.5;

		for (Colors color : Colors.values()) {
			if (!getterSetters.containsKey(color)
					|| color == DISALLOW_CHANGING) {
				continue;
			}

			JLabel label = new JLabel(colorToName(color));
			label.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));

			c.gridx = 0;
			panel.add(label, c);
			c.gridx = 1;
			panel.add((ColorButton) getterSetters.get(color), c);

			c.gridy++;
		}

		return panel;
	}

	/**
	 * Converts {@code color} to a user-understandable {@code String}. If there
	 * is no mapping available, returns {@code input.toString()}.
	 * 
	 * @param color enum value to convert
	 * 
	 * @return {@code String} that describes {@code color}
	 */
	private String colorToName(Colors color) {
		String asString = "";
		String[] words = color.toString().split("_");
		for (String word : words) {
			word = word.toLowerCase();
			char firstAsUpperCase = (char) ((int) word.charAt(0) + 'A' - 'a');
			word = firstAsUpperCase + word.substring(1);
			asString += " " + word;
		}
		return asString.substring(1);  // Remove leading space
	}

	/**
	 * {@code JButton} that pops up a {@code JColorChooser} when it is activated
	 * and sets its background color to the result.
	 */
	private class ColorButton extends JButton implements GetterSetter<Integer> {

		private ColorButton() {
			super();
			addActionListener(e -> {
				Color newColor = JColorChooser.showDialog(this,
						"Choose New Color", getBackground());
				if (newColor != null) {
					setBackground(newColor);
				}
			});
		}

		@Override
		public Integer get() {
			return getBackground().getRGB();
		}

		@Override
		public void set(Integer value) {
			setBackground(new Color(value, true));
		}

		@Override
		public void addChangeListener(Runnable runnable) {
			addPropertyChangeListener("background", e -> runnable.run());
		}

	}

}
