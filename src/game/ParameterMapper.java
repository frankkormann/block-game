package game;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * {@code Mapper} for semi-constant parameters which should only be changed by
 * the user in an {@code OptionsDialog}. A parameter is something like game
 * speed or GUI scaling.
 */
public class ParameterMapper extends Mapper<Integer> {

	/**
	 * Keys for values which are held by {@code ParameterMapper}.
	 */
	public enum Parameter {
		GAME_SPEED, GAME_SCALING, GUI_SCALING, HINT_OPACITY,
		KEYBOARD_RESIZING_AMOUNT, RESIZING_AREA_WIDTH
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
	public TypeReference<EnumValues<Integer>> getJsonTypeReference() {
		return new TypeReference<EnumValues<Integer>>() {};
	}

	@Override
	public Class<? extends Enum<?>>[] getEnumClasses() {
		return new Class[] { Parameter.class };
	}

	@Override
	public Integer getDefaultValue() {
		return 0;
	}

}
