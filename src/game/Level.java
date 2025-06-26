package game;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for level information while reading level JSON.
 * 
 * @author Frank Kormann
 */
public class Level {

	public String name;
	public int width, height;

	public List<MovingRectangle> movingRectangles;
	public List<WallRectangle> walls;
	public List<Area> areas;
	public List<GoalArea> goals;
	public List<HintRectangle> hints;

	public Level() {
		name = "";
		width = height = 0;
		movingRectangles = new ArrayList<>();
		walls = new ArrayList<>();
		areas = new ArrayList<>();
		goals = new ArrayList<>();
		hints = new ArrayList<>();
	}

}
