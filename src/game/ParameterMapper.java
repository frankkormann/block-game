package game;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * {@code Mapper} for semi-constant parameters which should only be changed by
 * the user in an {@code OptionsDialog}. A parameter is something like game
 * speed or GUI scaling.
 */
public class ParameterMapper extends Mapper<Float> {

	/**
	 * Keys for values which are held by {@code ParameterMapper}.
	 */
	public enum Parameter {
		/**
		 * Number of milliseconds between each frame
		 */
		GAME_SPEED,
		/**
		 * Multiplier for size of game area
		 * 
		 * @implNote Not implemented
		 */ // TODO
		GAME_SCALING,
		/**
		 * Multiplier for size of GUI elements
		 * 
		 * @implNote Not implemented
		 */ // TODO
		GUI_SCALING,
		/**
		 * Opacity to use for the inside of {@code HintRectangle}s
		 */
		HINT_OPACITY,
		/**
		 * Number of units to move a side by with each keyboard resizing input
		 * 
		 * @implNote Not implemented
		 */ // TODO
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
	public TypeReference<EnumValues<Float>> getJsonTypeReference() {
		return new TypeReference<EnumValues<Float>>() {};
	}

	@Override
	public Class<? extends Enum<?>>[] getEnumClasses() {
		return new Class[] { Parameter.class };
	}

	@Override
	public Float getDefaultValue() {
		return 0f;
	}

	/**
	 * Gets a parameter value as an {@code int}.
	 * 
	 * @param parameter key
	 * 
	 * @return value as {@code int}
	 */
	public int getInt(Enum<?> parameter) {
		return (int) (float) get(parameter);
	}

}
