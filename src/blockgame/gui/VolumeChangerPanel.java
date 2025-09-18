package blockgame.gui;

import javax.swing.JRootPane;

import blockgame.input.VolumeMapper;
import blockgame.input.VolumeMapper.Volume;

/**
 * {@code NumberChangerPanel} which allows the user to remap values in
 * {@code VolumeMapper}. Automatically calls {@code Mapper.save} the parent
 * window is closed.
 * 
 * @author Frank Kormann
 */
public class VolumeChangerPanel extends NumberChangerPanel {

	/**
	 * Creates a {@code VolumeChangerPanel} which will alter
	 * {@code volumeMapper}.
	 * 
	 * @param rootPane     {@code JRootPane} of parent window
	 * @param volumeMapper {@code VolumeMapper} to alter
	 */
	public VolumeChangerPanel(JRootPane rootPane, VolumeMapper volumeMapper) {
		super(rootPane, volumeMapper);
	}

	@Override
	protected SliderSpinner createSliderSpinner(Enum<?> enumValue) {
		if (enumValue == Volume.SFX || enumValue == Volume.MUSIC) {
			return new SliderSpinner(0, 200, 0, 200, 5, true, "%");
		}
		if (enumValue == Volume.LR_BALANCE) {
			return new SliderSpinner(-100, 100, -100, 100, 10, true, null);
		}

		return null;
	}

	@Override
	protected String paramToName(Enum<?> enumValue) {
		if (enumValue == Volume.SFX) {
			return "Sound Effects Volume";
		}
		if (enumValue == Volume.MUSIC) {
			return "Music Volume";
		}
		if (enumValue == Volume.LR_BALANCE) {
			return "Left/right Stereo Balance";
		}

		return enumValue.toString();
	}

	@Override
	protected String paramToTooltip(Enum<?> enumValue) {
		if (enumValue == Volume.SFX || enumValue == Volume.MUSIC) {
			return "0% is muted, 100% is default, 200% is twice as loud\nLarge"
					+ " values may not play correctly on some devices";
		}
		if (enumValue == Volume.LR_BALANCE) {
			return "Distribution of sound between left and right speakers\n-100"
					+ " is fully left and 100 is fully right";
		}

		return null;
	}

	@Override
	protected Enum<?>[] getEnumValues() {
		return Volume.values();
	}

}
