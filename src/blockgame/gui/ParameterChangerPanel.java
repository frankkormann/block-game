package blockgame.gui;

import java.awt.Window;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComboBox;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import blockgame.input.GetterSetter;
import blockgame.input.ParameterMapper;
import blockgame.input.ParameterMapper.BracketType;
import blockgame.input.ParameterMapper.Parameter;

/**
 * {@code NumberChangerPanel} which allows the user to remap values in
 * {@code ParameterMapper}, such as GUI scaling. Automatically calls
 * {@code Mapper.save} the parent window is closed.
 * 
 * @author Frank Kormann
 */
public class ParameterChangerPanel extends NumberChangerPanel {

	private static final int SPACE = 3;

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
					if (window != null) {
						SwingUtilities.updateComponentTreeUI(window);
						window.pack();
					}
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
		if (enumValue == Parameter.BRACKET_TYPE) {
			DropDownGetterSetter bracketController = new DropDownGetterSetter();
			for (BracketType bracket : BracketType.values()) {
				String name = bracket.toString().toLowerCase();
				name = name.substring(0, 1).toUpperCase() + name.substring(1);
				bracketController.addItem(
						bracket.left + " " + name + " " + bracket.right,
						bracket.ordinal());
			}
			return bracketController;
		}

		return null;
	}

	@Override
	protected String paramToName(Enum<?> enumValue) {
		if (enumValue == Parameter.GAME_SPEED) {
			return "Game Speed";
		}
		if (enumValue == Parameter.GAME_SCALING) {
			return "Game Scaling";
		}
		if (enumValue == Parameter.GUI_SCALING) {
			return "GUI Scaling";
		}
		if (enumValue == Parameter.OPACITY_MULTIPLIER) {
			return "Hint Opacity";
		}
		if (enumValue == Parameter.KEYBOARD_RESIZING_AMOUNT) {
			return "Keyboard Resizing Rate";
		}
		if (enumValue == Parameter.RESIZING_AREA_WIDTH) {
			return "Window-edge Resizing Area Size";
		}
		if (enumValue == Parameter.BRACKET_TYPE) {
			return "Level Title Bracket Type";
		}

		return enumValue.toString();

	}

	@Override
	protected String paramToTooltip(Enum<?> enumValue) {
		if (enumValue == Parameter.GAME_SPEED) {
			return "Number of milliseconds between each frame\nLower is faster";
		}
		if (enumValue == Parameter.GAME_SCALING) {
			return "Multiplier for size of game objects";
		}
		if (enumValue == Parameter.GUI_SCALING) {
			return "Multiplier for size of GUI elements";
		}
		if (enumValue == Parameter.OPACITY_MULTIPLIER) {
			return "Transparency of hint blocks\nLower is more transparent";
		}
		if (enumValue == Parameter.KEYBOARD_RESIZING_AMOUNT) {
			return "Amount the window will be resized with each keyboard input";
		}
		if (enumValue == Parameter.RESIZING_AREA_WIDTH) {
			return "Width of the window regions that can be click-dragged to resize it";
		}
		if (enumValue == Parameter.BRACKET_TYPE) {
			return "Which bracket symbols to use in the level title";
		}

		return null;
	}

	@Override
	protected Enum<?>[] getEnumValues() {
		return Parameter.values();
	}

	private class DropDownGetterSetter extends JComboBox<String>
			implements GetterSetter<Number> {

		private Map<Integer, String> itemIndex;
		private int currentItem;

		private Runnable changeListener;

		public DropDownGetterSetter() {
			itemIndex = new HashMap<>();
			currentItem = -1;
			changeListener = () -> {};

			setEditable(false);
			addItemListener(e -> {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					for (Entry<Integer, String> entry : itemIndex.entrySet()) {
						if (entry.getValue().equals(e.getItem())) {
							currentItem = entry.getKey();
							changeListener.run();
							break;
						}
					}
				}
			});
		}

		public void addItem(String label, int index) {
			addItem(label);
			itemIndex.put(index, label);
		}

		@Override
		public Number get() {
			return currentItem;
		}

		@Override
		public void set(Number value) {
			boolean changed = value.intValue() != currentItem;
			currentItem = value.intValue();
			setSelectedItem(itemIndex.get(currentItem));

			if (changed) {
				changeListener.run();
			}
		}

		@Override
		public void addChangeListener(Runnable runnable) {
			changeListener = runnable;
		}

	}

}
