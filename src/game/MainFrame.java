package game;

import java.awt.Component;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * JFrame that displays each level. Buffers changes to width/height between each
 * frame.
 * <p>
 * The usual methods for resizing a JFrame should not be used for this. The
 * methods {@link #resize(int, Direction)} and {@link #resizeAll(Map)} should be
 * used instead. {@code incorporateChanges} should be called at the end of each
 * frame.
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
 * width/height and title. Then add each {@code Rectangle} with {@code add}. A
 * level should be loaded before calling {@code setVisible(true)} to avoid
 * graphical glitches.
 * <p>
 * Note: Assumes look-and-feel is FlatLightLaf
 * 
 * @author Frank Kormann
 */
public class MainFrame extends JFrame implements Resizable {

	public static final String TASKBAR_ICON = "/taskbar_icon.png";

	private static final String WINDOW_TITLE = "Block Game";

	private static final int WIDTH_MINIMUM = 150;
	private static final int HEIGHT_MINIMUM = 150;
	// TODO Figure out how to work around maximum window size

	private int xChange, yChange;
	private int widthChange, heightChange;

	private boolean interceptPropertyChangeEvent;

	private DrawingPane drawingPane;

	public enum Direction {
		NORTH, SOUTH, WEST, EAST;

		/**
		 * {@code NORTH} if this is {@code SOUTH}, {@code WEST} if this is
		 * {@code EAST}, etc.
		 * 
		 * @return The opposite of this {@code Direction}
		 */
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

	/**
	 * Creates a new {@code MainFrame}. {@code gameInputHandler} is registered
	 * to this as a {@code KeyListener} and {@code FocusListener}.
	 * 
	 * @param gameInputHandler {@code GameInputHandler} to register
	 */
	public MainFrame(GameInputHandler gameInputHandler) {
		super(WINDOW_TITLE);

		// width and height are instantiated in setUpLevel()
		xChange = 0;
		yChange = 0;
		widthChange = 0;
		heightChange = 0;

		interceptPropertyChangeEvent = false;

		drawingPane = new DrawingPane();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createWindow(gameInputHandler);
			}
		});
	}

	private static int getTitlePaneHeight() {
		return UIManager.getDimension("TitlePane.buttonSize").height;
	}

	private static int getTitlePaneButtonsWidth() {
		return 2 * UIManager.getDimension("TitlePane.buttonSize").width;
	}

	private void createWindow(GameInputHandler gameInputHandler) {
		try {
			setIconImage(ImageIO.read(getClass().getResource(TASKBAR_ICON)));
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Failed to load resource for taskbar icon",
					e).setVisible(true);
		}
		addKeyListener(gameInputHandler);
		addFocusListener(gameInputHandler);
		setLayout(null);
		setResizable(false);

		getLayeredPane().add(drawingPane);

		for (Direction direction : Direction.values()) {
			ResizingSide side = new ResizingSide(direction, gameInputHandler);
			getLayeredPane().add(side, 0);
		}
	}

	/**
	 * Sets width, height, and title for a {@code Level}. Also clears the
	 * previous level.
	 * 
	 * @param level Level to set up
	 */
	public void setUpLevel(Level level) {
		drawingPane.clearDrawables();
		drawingPane.setOffsets(0, 0);

		pack();  // set insets
		int width = level.width + getInsets().left + getInsets().right;
		int height = level.height + getInsets().top + getInsets().bottom
				+ getTitlePaneHeight();
		setSize(width, height);

		updateTitleBarText(WINDOW_TITLE + " - " + level.name);

		arrangeComponents();
	}

	/**
	 * Add {@code drawable} to this at {@code index}. {@code Drawable}s with a
	 * higher index will be drawn on top of those with a lower index.
	 * <p>
	 * The drawing order for {@code Drawable}s with the same index is undefined.
	 * 
	 * @param drawable {@code Drawable} to draw
	 * @param index    layer to put {@code drawable}
	 */
	public void add(Drawable drawable, int index) {
		drawingPane.add(drawable, index);
	}

	public void resize(int change, Direction direction) {
		if (direction == Direction.NORTH || direction == Direction.WEST) {
			change *= -1;
		}

		if ((direction == Direction.NORTH || direction == Direction.SOUTH)
				&& (getHeight() + change < HEIGHT_MINIMUM)) {
			change = HEIGHT_MINIMUM - getHeight();
		}
		if ((direction == Direction.WEST || direction == Direction.EAST)
				&& (getWidth() + change < WIDTH_MINIMUM)) {
			change = WIDTH_MINIMUM - getWidth();
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
	 * Resizes this frame's preferred bounds. Respects minimum bounds as defined
	 * by {@code WIDTH_MINIMUM} and {@code HEIGHT_MINIMUM}.
	 * 
	 * @param resizes Map from {@code Direction} of each resize to change amount
	 * 
	 * @see MainFrame#resize(int, Direction)
	 */
	public void resizeAll(Map<Direction, Integer> resizes) {
		for (Direction direction : resizes.keySet()) {
			resize(resizes.get(direction), direction);
		}
	}

	/**
	 * Incorporates all pending changes to x, y, width, and height, then lays
	 * out components and repaints this.
	 * <p>
	 * If this is called from anywhere other than the AWT event dispatching
	 * thread, it will call itself from the AWT event dispatching thread.
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
				new ErrorDialog("Error", "Something went wrong", e);
			}
		}

		drawingPane.setOffsets(drawingPane.getXOffset() + xChange,
				drawingPane.getYOffset() + yChange);
		// Paint before setting bounds to minimize stuttering
		drawingPane.paintImmediately(0, 0, drawingPane.getWidth(),
				drawingPane.getHeight());

		setBounds(getX() + xChange, getY() + yChange, getWidth() + widthChange,
				getHeight() + heightChange);

		xChange = 0;
		yChange = 0;
		widthChange = 0;
		heightChange = 0;

		arrangeComponents();
	}

	public void moveToMiddleOfScreen() {
		setLocationRelativeTo(null);
	}

	/**
	 * Lays out all components within this using its current width and height.
	 */
	private void arrangeComponents() {

		for (Component comp : getLayeredPane().getComponents()) {

			int insetsX = getInsets().left + getInsets().right;
			int insetsY = getInsets().top + getInsets().bottom;

			if (comp instanceof ResizingSide) {
				switch (((ResizingSide) comp).getDirection()) {
					case NORTH:
						comp.setBounds(0, 0, getWidth()
								- getTitlePaneButtonsWidth() - insetsX,
								ResizingSide.THICKNESS / 2);
						break;
					case SOUTH:
						comp.setBounds(0,
								getHeight() - ResizingSide.THICKNESS - insetsY,
								getWidth(), ResizingSide.THICKNESS);
						break;
					case WEST:
						comp.setBounds(0, ResizingSide.THICKNESS / 2,
								ResizingSide.THICKNESS, getHeight()
										- (int) (1.5 * ResizingSide.THICKNESS));
						break;
					case EAST:
						comp.setBounds(
								getWidth() - ResizingSide.THICKNESS - insetsX,
								getTitlePaneHeight(), ResizingSide.THICKNESS,
								getHeight() - ResizingSide.THICKNESS
										- getTitlePaneHeight());
				}
			}

			if (comp instanceof DrawingPane) {
				comp.setBounds(0, getTitlePaneHeight(), getWidth() - insetsX,
						getHeight() - insetsY - getTitlePaneHeight());
			}

		}
	}

	/**
	 * Sets only the text in the title pane. Task bar text is unaffected.
	 * 
	 * @param newText text to display
	 */
	private void updateTitleBarText(String newText) {
		String taskbarText = getTitle();
		setTitle(newText);
		interceptPropertyChangeEvent = true;  // Block FlatTitlePane from
		setTitle(taskbarText);  				// setting its text back
		interceptPropertyChangeEvent = false;
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

	@Override
	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		if (interceptPropertyChangeEvent) {
			return;
		}

		super.firePropertyChange(propertyName, oldValue, newValue);
	}

}
