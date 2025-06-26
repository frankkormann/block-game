package game;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * Draws all {@code Rectangles} to the window using their {@code draw} methods.
 * 
 * The x- and y-offsets need to be set properly with {@code setOffsets}. Because
 * the top-left corner will not always be at (0, 0), this keeps track of its
 * offset relative to (0, 0) to determine where to draw each {@code Rectangle}.
 * 
 * {@code Areas} are drawn underneath other {@code Rectangles}.
 *
 * @author Frank Kormann
 */
public class DrawingPane extends JPanel {

	private List<Rectangle> rectangles;
	private List<Area> areas;

	private int xOffset, yOffset;

	public DrawingPane() {
		super();

		this.xOffset = 0;
		this.yOffset = 0;

		setBackground(Color.WHITE);

		rectangles = new ArrayList<>();
		areas = new ArrayList<>();
	}

	public void addRectangle(Rectangle rect) {
		rectangles.add(rect);
	}

	public void addArea(Area area) {
		areas.add(area);
	}

	public void clear() {
		rectangles.clear();
		areas.clear();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.translate(-xOffset, -yOffset);

		for (Area a : areas) { // Make sure Areas are painted on the bottom layer
			a.draw(g);
		}
		for (Rectangle r : rectangles) {
			r.draw(g);
		}
	}

	public void setOffsets(int xOffset, int yOffset) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}

	public int getXOffset() {
		return xOffset;
	}

	public int getYOffset() {
		return yOffset;
	}

	public void setXOffset(int xOffset) {
		this.xOffset = xOffset;
	}

	public void setYOffset(int yOffset) {
		this.yOffset = yOffset;
	}

}
