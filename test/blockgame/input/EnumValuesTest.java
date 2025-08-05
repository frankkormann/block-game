package blockgame.input;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import blockgame.input.EnumValues;
import blockgame.input.ParameterMapper.Parameter;
import blockgame.physics.Rectangle.Colors;

public class EnumValuesTest {

	@Test
	void maps_enum_names_to_values_correctly() {
		EnumValues<Enum<?>> enumValues = new EnumValues<>();
		enumValues.enumClasses = new Class[] { Colors.class, Parameter.class };

		Map<String, Enum<?>> enumNames = new HashMap<>();
		for (Class<? extends Enum<?>> enumClass : enumValues.enumClasses) {
			Arrays.stream(enumClass.getEnumConstants())
					.forEach(e -> enumNames.put(e.toString(), e));
		}

		enumValues.setValues(enumNames);

		assertTrue(enumValues.values.entrySet()
				.stream()
				.allMatch(e -> e.getKey() == e.getValue()));
	}
}
