package blockgame.input;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Simple {@code Mapper} to control sound settings. Volume is stored on a linear
 * scale where 0.0 represents muted and 1.0 represents normal volume.
 * 
 * @author Frank Kormann
 */
public class SoundMapper extends Mapper<Number> {

	public enum SoundControl {
		MUSIC, SFX, LR_BALANCE
	}

	private static final String SAVE_PATH = "/sound.json";
	private static final String DEFAULT_RESOURCE = "/sound_default.json";

	public SoundMapper() {
		super(SAVE_PATH, DEFAULT_RESOURCE);
	}

	@Override
	public TypeReference<EnumValues<Number>> getJsonTypeReference() {
		return new TypeReference<EnumValues<Number>>() {};
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Enum<?>>[] getEnumClasses() {
		return new Class[] { SoundControl.class };
	}

	@Override
	public Number getDefaultValue() {
		return 1;
	}

	@Override
	public boolean allowUnset() {
		return false;
	}

}
