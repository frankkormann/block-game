package game;

import java.awt.Component;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * JFrame that displays each level. Buffers changes to width/height between each
 * frame.
 * <p>
 * The usual methods for resizing and moving a JFrame should not be used for
 * this. The methods {@code resize2}, {@code resizeAll}, and {@code move2}
 * should be used instead. {@code incorporateChanges} should be called at the
 * end of each frame.
 * <p>
 * Actual resizing of the window is delayed until {@code incorporateChanges} is
 * called at the end of each frame, after the new window size has been verified
 * by {@code PhysicsSimulator}. This ensures that the window is never visually
 * in an invalid state. Such an invalid state would be correctly resized almost
 * immediately (at the next frame), leading to stuttering.
 * <p>
 * Moving this around the screen without resizing it is not buffered. This has
 * no effect on game physics and should not cause visual issues.
 * <p>
 * Whenever a level needs to be loaded, use {@code setUpLevel} to load the
 * width/height and title. Then add each {@code Rectangle} with {@code add}.
 * 
 * @author Frank Kormann
 */
public class MainFrame extends JFrame {

	private static final String TASKBAR_ICON = "/taskbar_icon.png";

	private static final String WINDOW_TITLE = "Block Game";

	private static final int WIDTH_MINIMUM = 150;
	private static final int HEIGHT_MINIMUM = 150;
	// TODO Figure out how to work around maximum window size

	private int x, y, width, height;
	private int xChange, yChange;
	private int widthChange, heightChange;

	private TitleBar titleBar;
	private DrawingPane drawingPane;

	public enum Direction {
		NORTH, SOUTH, WEST, EAST;

		public Direction getOpposite() {
			switch (this) {
				case NORTH:
					return SOUTH;
				case SOUTH:
					return NORTH;
				case WEST:
					return EAST;
				case EAST:
					return WEST;
				default:
					return null;
			}
		}
	}

	public MainFrame(GameInputHandler gameInputHandler) {
		super(WINDOW_TITLE);

		// x, y, width, and height are instantiated in createAndShowWindow()
		xChange = 0;
		yChange = 0;
		widthChange = 0;
		heightChange = 0;

		titleBar = new TitleBar(this);
		drawingPane = new DrawingPane();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowWindow(gameInputHandler);
			}
		});
	}

	private void createAndShowWindow(GameInputHandler gameInputHandler) {
		try {
			setIconImage(ImageIO.read(getClass().getResource(TASKBAR_ICON)));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		addKeyListener(gameInputHandler);
		setLayout(null);
		setResizable(false);

		getLayeredPane().add(titleBar, -1);
		getLayeredPane().add(drawingPane);

		for (Direction direction : Direction.values()) {
			ResizingSide side = new ResizingSide(direction, gameInputHandler);
			getLayeredPane().add(side, 0);
		}
	}

	/**
	 * Sets width, height, and title for a {@code Level}. Also clears the previous
	 * level.
	 * 
	 * @param level Level to set up
	 */
	public void setUpLevel(Level level) {
		drawingPane.clear();
		drawingPane.setOffsets(0, 0);

		pack();  // set insets
		width = level.width + getInsets().left + getInsets().right;
		height = level.height + getInsets().top + getInsets().bottom + TitleBar.HEIGHT;
		titleBar.setTitle(WINDOW_TITLE + " - " + level.name);
	}

	public void add(Rectangle rect, int index) {
		drawingPane.add(rect, index);
	}

	/**
	 * Resizes this frame's preferred bounds. Respects minimum bounds as defined by
	 * {@code WIDTH_MINIMUM} and {@code HEIGHT_MINIMUM}.
	 * 
	 * @param change    new dimension size minus old dimension size
	 * @param direction side of the window to move
	 */
	public void resize2(int change, Direction direction) {
		if (direction == Direction.NORTH || direction == Direction.WEST) {
			change *= -1;
		}

		if ((direction == Direction.NORTH || direction == Direction.SOUTH)
				&& (height + change < HEIGHT_MINIMUM)) {
			change = HEIGHT_MINIMUM - height;
		}
		if ((direction == Direction.WEST || direction == Direction.EAST)
				&& (width + change < WIDTH_MINIMUM)) {
			change = WIDTH_MINIMUM - width;
		}

		switch (direction) {
			case NORTH:
				yChange -= change;
			case SOUTH:
				heightChange += change;
				break;
			case WEST:
				xChange -= change;
			case EAST:
				widthChange += change;
				break;
		}
	}

	/**
	 * @param resizes Map from Direction of each resize to change amount
	 * 
	 * @see MainFrame#resize2(int, Direction)
	 */
	public void resizeAll(Map<Direction, Integer> resizes) {
		for (Direction direction : resizes.keySet()) {
			resize2(resizes.get(direction), direction);
		}
	}

	/**
	 * Incorporates all pending changes to x, y, width, and height, then lays out
	 * components and repaints this.
	 * <p>
	 * If this is called from anywhere other than the AWT event dispatching thread,
	 * it will call itself from the AWT event dispatching thread.
	 */
	public void incorporateChanges() {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						incorporateChanges();
					}
				});
			}
			catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
		}

		drawingPane.setOffsets(drawingPane.getXOffset() + xChange,
				drawingPane.getYOffset() + yChange);
		// Paint before packing to avoid stuttering issues
		drawingPane.paintImmediately(0, 0, drawingPane.getWidth(),
				drawingPane.getHeight());
		titleBar.paintImmediately(0, 0, titleBar.getWidth(), titleBar.getHeight());
		x += xChange;
		y += yChange;
		width += widthChange;
		height += heightChange;

		xChange = 0;
		yChange = 0;
		widthChange = 0;
		heightChange = 0;

		arrangeComponents();
	}

	public void move2(int xDifference, int yDifference) {
		x += xDifference;
		y += yDifference;
		setLocation(x, y);
	}

	public void moveToMiddleOfScreen() {
		setLocationRelativeTo(null);
		x = getX();
		y = getY();
	}

	public void arrangeComponents() {
		setBounds(x, y, width, height);
		width = getWidth();
		height = getHeight();

		for (Component c : getLayeredPane().getComponents()) {

			if (c instanceof ResizingSide) {
				int insetsX = getInsets().left + getInsets().right;
				int insetsY = getInsets().top + getInsets().bottom;
				switch (((ResizingSide) c).getDirection()) {
					case NORTH:
						c.setBounds(0, 0, titleBar.getButtonsX(),
								ResizingSide.THICKNESS / 2);
						break;
					case SOUTH:
						c.setBounds(0, height - ResizingSide.THICKNESS - insetsY, width,
								ResizingSide.THICKNESS);
						break;
					case WEST:
						c.setBounds(0, ResizingSide.THICKNESS / 2,
								ResizingSide.THICKNESS,
								height - (int) (1.5 * ResizingSide.THICKNESS));
						break;
					case EAST:
						c.setBounds(width - ResizingSide.THICKNESS - insetsX,
								TitleBar.HEIGHT, ResizingSide.THICKNESS,
								height - ResizingSide.THICKNESS - TitleBar.HEIGHT);
				}
			}

			if (c instanceof DrawingPane) {
				int insetsX = getInsets().left + getInsets().right;
				int insetsY = getInsets().top + getInsets().bottom;
				c.setBounds(0, TitleBar.HEIGHT, getWidth() - insetsX,
						getHeight() - insetsY - TitleBar.HEIGHT);
			}

			if (c instanceof TitleBar) {
				c.setBounds(0, 0, getWidth(), TitleBar.HEIGHT);
			}

		}
	}

	/**
	 * @return Current width + pending width changes
	 */
	public int getNextWidth() {
		return drawingPane.getWidth() + widthChange;
	}

	/**
	 * @return Current height + pending height changes
	 */
	public int getNextHeight() {
		return drawingPane.getHeight() + heightChange;
	}

	/**
	 * @return Current x offset + pending changes
	 */
	public int getNextXOffset() {
		return drawingPane.getXOffset() + xChange;
	}

	/**
	 * @return Current y offset + pending changes
	 */
	public int getNextYOffset() {
		return drawingPane.getYOffset() + yChange;
	}

}
