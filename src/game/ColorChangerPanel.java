package game;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import game.Area.TranslucentColors;
import game.MovingRectangle.Colors;
import game.WallRectangle.WallColors;

/**
 * {@code JPanel} which allows the user to change the color values in
 * {@code ColorMapper}. Automatically calls {@code colorMapper.save()} when the
 * parent window is closed.
 */
public class ColorChangerPanel extends JPanel implements ValueChangeListener {

	private static final String UNDO_TEXT = "Undo changes";
	private static final String RESET_TEXT = "Reset to defaults";

	private static final int VERTICAL_SPACE = 3;
	private static final Enum<?> DISALLOW_CHANGING = TranslucentColors.TRANSPARENT;

	private ColorMapper colorMapper;
	private Map<Enum<?>, JButton> colorToButton;

	/**
	 * Creates a {@code ColorChangePanel} which will change the color mappings
	 * in {@code colorMapper}.
	 * 
	 * @param rootPane    {@code JRootPane} of {@code Window} this will be added
	 *                    to
	 * @param colorMapper {@code ColorMapper} to alter
	 */
	public ColorChangerPanel(JRootPane rootPane, ColorMapper colorMapper) {
		super();
		this.colorMapper = colorMapper;
		colorToButton = new HashMap<>();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		add(createColorsPanel(Colors.values()));
		add(createColorsPanel(WallColors.values()));
		add(createColorsPanel(TranslucentColors.values()));
		add(createUndoResetButtonsPanel());

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
		colorMapper.addListener(this);
	}

	/**
	 * Creates a {@code JPanel} with a {@code JButton} to undo temporary color
	 * changes and a {@code JButton} to reset colors to defaults.
	 * 
	 * @return the {@code JPanel}
	 */
	private JPanel createUndoResetButtonsPanel() {
		JPanel panel = new JPanel();

		JButton undoButton = new JButton(UNDO_TEXT);
		undoButton.addActionListener(e -> {
			colorMapper.reload();
		});
		undoButton.setFocusable(false);

		JButton resetButton = new JButton(RESET_TEXT);
		resetButton.addActionListener(e -> {
			colorMapper.setToDefaults();
		});
		resetButton.setFocusable(false);

		panel.add(undoButton);
		panel.add(resetButton);

		return panel;
	}

	/**
	 * Creates a {@code JPanel} which displays all the colors in {@code colors}
	 * with a {@code JButton} to rebind them.
	 * 
	 * @param colors enum values to display
	 * 
	 * @return the {@code JPanel}
	 */
	private JPanel createColorsPanel(Enum<?>[] colors) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		for (Enum<?> color : colors) {
			if (color == DISALLOW_CHANGING) {
				continue;
			}
			panel.add(Box.createVerticalStrut(VERTICAL_SPACE));
			panel.add(createButtonPanel(color));
			panel.setAlignmentX(CENTER_ALIGNMENT);
		}

		return panel;
	}

	/**
	 * Creates a {@code JPanel} for {@code color}. It contains a {@code JLabel}
	 * with {@code color}'s name and a {@code JButton} to rebind it.
	 * 
	 * @param color enum value to create a {@code JPanel} for
	 * 
	 * @return the {@code JPanel}
	 */
	private JPanel createButtonPanel(Enum<?> color) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2, 10, 10));

		panel.add(new JLabel(colorToName(color)));
		panel.add(createButtonForColor(color));

		return panel;
	}

	/**
	 * Creates a {@code JButton} which will rebind {@code color}.
	 * 
	 * @param color enum value to rebind
	 * 
	 * @return the {@code JButton}
	 */
	private JButton createButtonForColor(Enum<?> color) {
		JButton button = new JButton();
		button.setBackground(colorMapper.getColor(color));
		button.addActionListener(e -> {
			Color newColor = JColorChooser.showDialog(this, "Choose New Color",
					colorMapper.getColor(color));
			if (newColor != null) {
				colorMapper.setColor(color, newColor);
				button.setBackground(newColor);
			}
		});

		colorToButton.put(color, button);

		return button;
	}

	/**
	 * Converts {@code color} to a user-understandable {@code String}. If there
	 * is no mapping available, returns {@code input.toString()}.
	 * 
	 * @param color enum value to convert
	 * 
	 * @return {@code String} that describes {@code color}
	 */
	private String colorToName(Enum<?> color) {
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

	private void cleanUp() {
		colorMapper.removeListener(this);
		colorMapper.save();
	}

	@Override
	public void valueChanged(Enum<?> key, Object newValue) {
		colorToButton.get(key).setBackground(colorMapper.getColor(key));
	}

	@Override
	public void valueRemoved(Enum<?> key) {
		colorToButton.get(key).setBackground(new Color(0, 0, 0, 0));
	}

}
