package blockgame.gui;

import javax.swing.JRootPane;

import blockgame.input.SoundMapper;
import blockgame.input.SoundMapper.SoundControl;

/**
 * {@code NumberChangerPanel} which allows the user to remap values in
 * {@code SoundMapper}. Automatically calls {@code Mapper.save} the parent
 * window is closed.
 * 
 * @author Frank Kormann
 */
public class SoundChangerPanel extends NumberChangerPanel {

	/**
	 * Creates a {@code SoundChangerPanel} which will alter
	 * {@code volumeMapper}.
	 * 
	 * @param rootPane    {@code JRootPane} of parent window
	 * @param soundMapper {@code SoundMapper} to alter
	 */
	public SoundChangerPanel(JRootPane rootPane, SoundMapper soundMapper) {
		super(rootPane, soundMapper);
	}

	@Override
	protected SliderSpinner createSliderSpinner(Enum<?> enumValue) {
		if (enumValue == SoundControl.SFX || enumValue == SoundControl.MUSIC) {
			return new SliderSpinner(0, 200, 0, 200, 5, true, "%");
		}
		if (enumValue == SoundControl.LR_BALANCE) {
			return new SliderSpinner(-100, 100, -100, 100, 10, true, null);
		}

		return null;
	}

	@Override
	protected String paramToName(Enum<?> enumValue) {
		if (enumValue == SoundControl.SFX) {
			return "Sound Effects Volume";
		}
		if (enumValue == SoundControl.MUSIC) {
			return "Music Volume";
		}
		if (enumValue == SoundControl.LR_BALANCE) {
			return "Left/right Stereo Balance";
		}

		return enumValue.toString();
	}

	@Override
	protected String paramToTooltip(Enum<?> enumValue) {
		if (enumValue == SoundControl.SFX || enumValue == SoundControl.MUSIC) {
			return "0% is muted, 100% is default, 200% is twice as loud\nLarge"
					+ " values may not play correctly on some devices";
		}
		if (enumValue == SoundControl.LR_BALANCE) {
			return "Distribution of sound between left and right speakers\n-100"
					+ " is fully left and 100 is fully right";
		}

		return null;
	}

	@Override
	protected Enum<?>[] getEnumValues() {
		return SoundControl.values();
	}

}
