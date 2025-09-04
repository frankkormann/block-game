package blockgame.mocks;

import com.fasterxml.jackson.core.type.TypeReference;

import blockgame.input.EnumValues;
import blockgame.input.Mapper;
import blockgame.physics.Rectangle.Colors;

public class IntMapper extends Mapper<Integer> {

	public boolean shouldAllowUnset;

	public IntMapper(String savePath, String defaultValuesResource) {
		super(savePath, defaultValuesResource);
		shouldAllowUnset = true;
	}

	@Override
	public TypeReference<EnumValues<Integer>> getJsonTypeReference() {
		return new TypeReference<EnumValues<Integer>>() {};
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Enum<?>>[] getEnumClasses() {
		return new Class[] { Colors.class };
	}

	@Override
	public Integer getDefaultValue() {
		return 0;
	}

	@Override
	public boolean allowUnset() {
		return shouldAllowUnset;
	}

}