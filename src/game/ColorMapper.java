package game;

import java.awt.Color;
import java.io.File;

import com.fasterxml.jackson.core.type.TypeReference;

import game.Area.TranslucentColors;
import game.MovingRectangle.Colors;
import game.WallRectangle.WallColors;

/**
 * {@code Mapper} for {@code Color}s represented as RGB {@code Integer}s. The
 * methods {@code getColor} and {@code setColor} are provided to interact with
 * this using {@code java.awt.Color} objects instead.
 * <p>
 * Mappings will be read from a save file if possible, or a resource if the save
 * file is unavailable.
 */
// Implemented as Integer instead of Color because Jackson serializes Color
// very verbosely
public class ColorMapper extends Mapper<Integer> {

	private static final String COLORS_PATH = "./save/colors.json";
	private static final String DEFAULT_COLORS_RESOURCE = "/colors_default.json";

	public ColorMapper() {
		super(new File(COLORS_PATH), DEFAULT_COLORS_RESOURCE);
	}

	@Override
	public TypeReference<EnumValues<Integer>> getJsonTypeReference() {
		return new TypeReference<EnumValues<Integer>>() {};
	}

	@Override
	public Class<? extends Enum<?>>[] getEnumClasses() {
		return new Class[] { Colors.class, TranslucentColors.class,
				WallColors.class };
	}

	/**
	 * Convenience method to set with a {@code Color}.
	 * 
	 * @param colorEnum enum value to set
	 * @param color     {@code Color} object to set
	 */
	public void setColor(Enum<?> colorEnum, Color color) {
		set(colorEnum, color.getRGB());
	}

	/**
	 * Convenience method to get a {@code Color}.
	 * 
	 * @param colorEnum enum value to get
	 * 
	 * @return the {@code Color} associated with {@code colorEnum}
	 */
	public Color getColor(Enum<?> colorEnum) {
		if (get(colorEnum) == null) {
			return null;
		}
		return new Color(get(colorEnum), true);
	}

}
