package blockgame.sound;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;

/**
 * Contains a single static method, {@link #setVolume(Line, float)}, to adjust
 * the volume of a {@code Line}.
 * 
 * @author Frank Kormann
 */
public class VolumeChanger {

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
			System.err.println("MASTER_GAIN control is not supported");
			return;
		}
		FloatControl control = (FloatControl) line
				.getControl(FloatControl.Type.MASTER_GAIN);
		float gain = 20 * (float) Math.log10(percent);
		gain = Math.min(control.getMaximum(),
				Math.max(gain, control.getMinimum()));
		control.setValue(gain);
	}

}
