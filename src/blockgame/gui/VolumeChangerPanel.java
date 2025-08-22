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
		return new SliderSpinner(0, 200, 0, 200, 5, true, "%");
	}

	@Override
	protected String paramToName(Enum<?> enumValue) {
		if (enumValue == Volume.SFX) {
			return "Sound Effects Volume";
		}
		if (enumValue == Volume.MUSIC) {
			return "Music Volume";
		}

		return enumValue.toString();
	}

	@Override
	protected String paramToTooltip(Enum<?> enumValue) {
		return null;
	}

	@Override
	protected Enum[] getEnumValues() {
		return Volume.values();
	}

}
