package blockgame;

import java.util.ArrayList;
import java.util.List;

import blockgame.gui.HintRectangle;
import blockgame.physics.Area;
import blockgame.physics.MovingRectangle;
import blockgame.physics.WallRectangle;

/**
 * Container for level information while reading level JSON.
 * 
 * @author Frank Kormann
 */
public class Level {

	public String name;
	public int width, height;
	public String solution;
	public String newTitle;
	public boolean gameComplete;

	public List<MovingRectangle> movingRectangles;
	public List<WallRectangle> walls;
	public List<Area> areas;

	public List<HintRectangle> hints;

	public Level() {
		name = "";
		width = height = 0;
		solution = "";
		newTitle = "";
		gameComplete = false;
		movingRectangles = new ArrayList<>();
		walls = new ArrayList<>();
		areas = new ArrayList<>();
		hints = new ArrayList<>();
	}

}
