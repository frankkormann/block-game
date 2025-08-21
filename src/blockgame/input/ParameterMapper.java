package blockgame.input;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * {@code Mapper} for semi-constant parameters which should only be changed by
 * the user in an {@code OptionsDialog}. A parameter is something like game
 * speed or GUI scaling.
 * 
 * @author Frank Kormann
 */
public class ParameterMapper extends Mapper<Number> {

	/**
	 * Keys for values which are held by {@code ParameterMapper}.
	 */
	public enum Parameter {
		/**
		 * Number of milliseconds between each frame
		 */
		GAME_SPEED,
		/**
		 * Volume of sound effects where 0 is muted and 1 is normal volume
		 */
		VOLUME,
		/**
		 * Multiplier for size of game area
		 */
		GAME_SCALING,
		/**
		 * Multiplier for size of GUI elements
		 */
		GUI_SCALING,
		/**
		 * Opacity to use for the inside of {@code HintRectangle}s
		 */
		OPACITY_MULTIPLIER,
		/**
		 * Number of units to move a side by with each keyboard resizing input
		 */
		KEYBOARD_RESIZING_AMOUNT,
		/**
		 * Amount each {@code ResizingSide} extends into the frame
		 */
		RESIZING_AREA_WIDTH
	}

	private static final String SAVE_PATH = "/parameters.json";
	private static final String DEFAULT_RESOURCE = "/parameters_default.json";

	/**
	 * Creates a {@code ParameterMapper} with default values for each
	 * {@code Parameter}.
	 */
	public ParameterMapper() {
		super(SAVE_PATH, DEFAULT_RESOURCE);
	}

	@Override
	public TypeReference<EnumValues<Number>> getJsonTypeReference() {
		return new TypeReference<EnumValues<Number>>() {};
	}

	@Override
	public Class<? extends Enum<?>>[] getEnumClasses() {
		return new Class[] { Parameter.class };
	}

	@Override
	public Integer getDefaultValue() {
		return 0;
	}

	@Override
	public boolean allowUnset() {
		return false;
	}

	/**
	 * Gets a parameter value as an {@code int}.
	 * 
	 * @param parameter which to get
	 * 
	 * @return value as {@code int}
	 */
	public int getInt(Enum<?> parameter) {
		return get(parameter) == null ? 0 : get(parameter).intValue();
	}

	/**
	 * Gets a parameter value as a {@code float}.
	 * 
	 * @param parameter which to get
	 * 
	 * @return value as {@code float}
	 */
	public float getFloat(Enum<?> parameter) {
		return get(parameter) == null ? 0 : get(parameter).floatValue();
	}

}
