package blockgame.sound;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;

/**
 * Collection of methods to alter {@code Line} properties.
 * 
 * @author Frank Kormann
 */
public class SoundChanger {

	/**
	 * Sets the volume of {@code line} to {@code percent} of its default volume.
	 * For example, to halve the volume, {@code percent} should be {@code 0.5}.
	 * <p>
	 * This converts {@code percent} into a number of decibels to set the
	 * {@code MASTER_GAIN} control on {@code line}. If {@code line} does not
	 * support {@code MASTER_GAIN}, this does nothing.
	 * 
	 * @param line    {@code Line} to change the volume of
	 * @param percent new volume
	 */
	public static void setVolume(Line line, float percent) {
		if (!line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			System.err.println(
					"In SoundChanger.java#setVolume: MASTER_GAIN control is not"
							+ " supported for " + line);
			return;
		}
		FloatControl control = (FloatControl) line
				.getControl(FloatControl.Type.MASTER_GAIN);
		float gain = (float) (20 * Math.log10(percent));
		gain = Math.min(control.getMaximum(),
				Math.max(gain, control.getMinimum()));
		control.setValue(gain);
	}

	/**
	 * Sets how the {@code line} will be distributed between two stereo
	 * speakers. Valid values vary from {@code -1} (fully left channel) to
	 * {@code 1} (fully right channel), centered at {@code 0} (balanced).
	 * 
	 * @param line  {@code Line} to set
	 * @param value relative balance between speakers
	 */
	public static void setLeftRightPosition(Line line, float value) {
		FloatControl control;
		if (line.isControlSupported(FloatControl.Type.PAN)) {
			control = (FloatControl) line.getControl(FloatControl.Type.PAN);
		}
		else if (line.isControlSupported(FloatControl.Type.BALANCE)) {
			control = (FloatControl) line.getControl(FloatControl.Type.BALANCE);
		}
		else {
			System.err.println(
					"In SoundChanger.java#setVolume: neither PAN nor BALANCE control"
							+ " is supported for " + line);
			return;
		}
		value = Math.min(control.getMaximum(),
				Math.max(value, control.getMinimum()));
		control.setValue(value);
	}

}
