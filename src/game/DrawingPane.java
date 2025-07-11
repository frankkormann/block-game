package game;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JPanel;

/**
 * Draws all {@code Drawable}s to the window using their {@code draw} methods.
 * 
 * The x- and y-offsets need to be set properly with {@code setOffsets}. Because
 * the top-left corner will not always be at (0, 0), this keeps track of its
 * offset relative to (0, 0) to determine where to draw each {@code Drawable}.
 *
 * @author Frank Kormann
 */
public class DrawingPane extends JPanel {

	private SortedMap<Integer, List<Drawable>> drawableLists;

	private int xOffset, yOffset;

	public DrawingPane() {
		super();

		this.xOffset = 0;
		this.yOffset = 0;

		setBackground(Color.WHITE);

		drawableLists = new TreeMap<>();
	}

	public void add(Drawable drawable, int index) {
		if (drawableLists.get(index) == null) {
			drawableLists.put(index, new ArrayList<>());
		}
		drawableLists.get(index).add(drawable);
	}

	/**
	 * Remove all drawables from this.
	 */
	public void clearDrawables() {
		drawableLists.clear();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.translate(-xOffset, -yOffset);

		for (Map.Entry<Integer, List<Drawable>> entry : drawableLists.entrySet()) {
			for (Drawable drawable : entry.getValue()) {
				drawable.draw(g);
			}
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
