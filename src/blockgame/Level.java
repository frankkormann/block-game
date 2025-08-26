package blockgame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blockgame.gui.HintRectangle;
import blockgame.physics.Area;
import blockgame.physics.MovingRectangle;
import blockgame.physics.WallRectangle;

/**
 * Container for level information while reading level JSON.
 * <p>
 * Default level number is {@code -1} if the level does not override it.
 * 
 * @author Frank Kormann
 */
public class Level {

	public String name;
	public int number;
	public int width, height;
	public String solution;

	public String popup;
	public Map<String, String> storeValues;

	public List<MovingRectangle> movingRectangles;
	public List<WallRectangle> walls;
	public List<Area> areas;

	public List<HintRectangle> hints;

	public Level() {
		name = "";
		number = -1;
		width = height = 0;
		solution = "";
		popup = "";
		storeValues = new HashMap<>();
		movingRectangles = new ArrayList<>();
		walls = new ArrayList<>();
		areas = new ArrayList<>();
		hints = new ArrayList<>();
	}

}
