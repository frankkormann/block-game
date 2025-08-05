package mocks;

import com.fasterxml.jackson.core.type.TypeReference;

import game.EnumValues;
import game.Mapper;
import game.Rectangle.Colors;

public class IntMapper extends Mapper<Integer> {

	public IntMapper(String savePath, String defaultValuesResource) {
		super(savePath, defaultValuesResource);
	}

	@Override
	public TypeReference<EnumValues<Integer>> getJsonTypeReference() {
		return new TypeReference<EnumValues<Integer>>() {};
	}

	@Override
	public Class<? extends Enum<?>>[] getEnumClasses() {
		return new Class[] { Colors.class };
	}

	@Override
	public Integer getDefaultValue() {
		return 0;
	}

}