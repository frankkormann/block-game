package game;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import game.MainFrame.Direction;

/**
 * Allows the user to resize the window by dragging at an edge. This is a
 * replacement for Windows's normal behavior to allow control over the window
 * resizing process.
 * <p>
 * This does not pass resizes directly to the {@code JFrame}. It is intended to
 * be used with an {@code InputHandler} registered in the constructor.
 * <p>
 * The top-level {@code JFrame} should contain four instances of this, one for
 * each side.
 * 
 * @author Frank Kormann
 */
public class ResizingSide extends JPanel implements MouseListener, MouseMotionListener {

	public static final int THICKNESS = 20;

	private int mouseX, mouseY;

	private Direction direction;
	private Resizable resizeListener;

	public ResizingSide(Direction direction, Resizable resizeListener) {
		this.direction = direction;
		this.resizeListener = resizeListener;

		setOpaque(false);

		Cursor cursor;
		switch (direction) {
			default:
			case NORTH:
				cursor = new Cursor(Cursor.N_RESIZE_CURSOR);
				break;
			case SOUTH:
				cursor = new Cursor(Cursor.S_RESIZE_CURSOR);
				break;
			case WEST:
				cursor = new Cursor(Cursor.W_RESIZE_CURSOR);
				break;
			case EAST:
				cursor = new Cursor(Cursor.E_RESIZE_CURSOR);
				break;
		}
		setCursor(cursor);

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseX = e.getXOnScreen();
		mouseY = e.getYOnScreen();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int change = 0;

		switch (direction) {
			case NORTH:
			case SOUTH:
				change = e.getYOnScreen() - mouseY;
				break;
			case WEST:
			case EAST:
				change = e.getXOnScreen() - mouseX;
				break;
		}

		resizeListener.resize(change, direction);

		mouseX = e.getXOnScreen();
		mouseY = e.getYOnScreen();
	}

	public Direction getDirection() {
		return direction;
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseMoved(MouseEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

}
