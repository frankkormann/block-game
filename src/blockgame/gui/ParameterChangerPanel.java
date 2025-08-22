package blockgame.gui;

import java.awt.Window;

import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import blockgame.input.ParameterMapper;
import blockgame.input.ParameterMapper.Parameter;

/**
 * {@code NumberChangerPanel} which allows the user to remap values in
 * {@code ParameterMapper}, such as GUI scaling. Automatically calls
 * {@code Mapper.save} the parent window is closed.
 * 
 * @author Frank Kormann
 */
public class ParameterChangerPanel extends NumberChangerPanel {

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
	protected SliderSpinner createSliderSpinner(Enum<?> enumValue) {
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

		return enumValue.toString();
	}

	@Override
	protected Enum[] getEnumValues() {
		return Parameter.values();
	}

}
