package blockgame;

import java.util.ArrayList;
import java.util.List;

import blockgame.gui.HintRectangle;
import blockgame.gui.Image;
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

	public List<MovingRectangle> movingRectangles;
	public List<WallRectangle> walls;
	public List<Area> areas;

	public List<HintRectangle> hints;
	public List<Image> images;

	public Level() {
		name = "";
		width = height = 0;
		solution = "";
		movingRectangles = new ArrayList<>();
		walls = new ArrayList<>();
		areas = new ArrayList<>();
		hints = new ArrayList<>();
		images = new ArrayList<>();
	}

}
