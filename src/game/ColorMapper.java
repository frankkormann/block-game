package game;

import java.awt.Color;
import java.io.File;

import com.fasterxml.jackson.core.type.TypeReference;

public class ColorMapper extends Mapper<Color> {

	private static final String COLORS_PATH = "./save/colors.json";
	private static final String DEFAULT_COLORS_RESOURCE = "/colors_default.json";

	public ColorMapper(File saveFile, String defaultValuesResource) {
		super(saveFile, defaultValuesResource);
	}

	@Override
	public TypeReference<EnumValues<Color>> getJsonTypeReference() {
		return new TypeReference<EnumValues<Color>>() {};
	}

	@Override
	public Class<? extends Enum<?>>[] getEnumClasses() {
		return null;
	}

}
