package blockgame.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JPanel;

import blockgame.input.ColorMapper;
import blockgame.input.ValueChangeListener;
import blockgame.physics.Rectangle.Colors;

/**
 * Draws all {@code Drawable}s to the window using their {@code draw} methods.
 * 
 * The x- and y-offsets need to be set properly with {@code setOffsets}. Because
 * the top-left corner will not always be at (0, 0), this keeps track of its
 * offset relative to (0, 0) to determine where to draw each {@code Drawable}.
 *
 * @author Frank Kormann
 */
public class DrawingPane extends JPanel implements ValueChangeListener {

	private SortedMap<Integer, List<Drawable>> drawableLists;

	private int xOffset, yOffset;
	private float scale;
	private ColorMapper colorMapper;

	/**
	 * Creates an empty {@code DrawingPane} with both offsets set to {@code 0}.
	 * 
	 * @param colorMapper {@code ColorMapper} to take background color from
	 */
	public DrawingPane(ColorMapper colorMapper) {
		super();

		this.xOffset = 0;
		this.yOffset = 0;
		this.colorMapper = colorMapper;
		scale = 1;

		setBackground(colorMapper.getColor(Colors.BACKGROUND));
		colorMapper.addListener(this);

		drawableLists = new TreeMap<>();
	}

	/**
	 * Adds {@code drawable} to this at {@code index}. {@code Drawable}s with a
	 * higher index will be drawn on top of those with a lower index.
	 * <p>
	 * The drawing order for {@code Drawable}s with the same index is undefined.
	 * 
	 * @param drawable {@code Drawable} to draw
	 * @param index    layer to put {@code drawable}
	 */
	public synchronized void add(Drawable drawable, int index) {
		if (drawableLists.get(index) == null) {
			drawableLists.put(index, new ArrayList<>());
		}
		drawableLists.get(index).add(drawable);
	}

	/**
	 * Removes all {@code Drawable}s from this.
	 */
	public synchronized void clearDrawables() {
		drawableLists.clear();
	}

	@Override
	public synchronized void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		super.paintComponent(g2d);
		g2d.scale(scale, scale);
		g2d.translate(-xOffset, -yOffset);

		for (Map.Entry<Integer, List<Drawable>> entry : drawableLists
				.entrySet()) {
			for (Drawable drawable : entry.getValue()) {
				drawable.draw(g2d);
			}
		}

		g2d.dispose();
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public void setOffsets(int xOffset, int yOffset) {
		Graphics g = getGraphics();
		if (g != null) {
			g.copyArea(0, 0, getWidth(), getHeight(), this.xOffset - xOffset,
					this.yOffset - yOffset);
		}
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}

	public int getXOffset() {
		return xOffset;
	}

	public int getYOffset() {
		return yOffset;
	}

	@Override
	public void valueChanged(Enum<?> key, Object newValue) {
		if (key == Colors.BACKGROUND) {
			setBackground(colorMapper.getColor(Colors.BACKGROUND));
		}
	}

	@Override
	public void valueRemoved(Enum<?> key) {}

}
