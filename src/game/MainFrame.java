package game;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import game.ParameterMapper.Parameter;

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
public class MainFrame extends JFrame
		implements Resizable, ValueChangeListener {

	private static final String TASKBAR_ICON = "/taskbar_icon.png";
	private static final String WINDOW_TITLE = "Block Game";
	private static final int DEFAULT_FONT_SIZE = 13;

	private static final int WIDTH_MINIMUM = 150;
	private static final int HEIGHT_MINIMUM = 150;
	// TODO Figure out how to work around maximum window size

	private float scale;
	private int idealWidth, idealHeight, idealXOffset, idealYOffset;
	private int centerX, centerY;
	private int xChange, yChange;
	private int widthChange, heightChange;

	private String title;

	private boolean interceptPropertyChangeEvent;

	private DrawingPane drawingPane;
	private ParameterMapper paramMapper;

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
	 * @param paramMapper      {@code ParameterMapper} to take parameter values
	 *                         from
	 */
	public MainFrame(GameInputHandler gameInputHandler,
			ParameterMapper paramMapper) {
		super(WINDOW_TITLE);

		// levelXXX, width, height are instantiated in
		// setUpLevel()
		xChange = 0;
		yChange = 0;
		widthChange = 0;
		heightChange = 0;

		title = getTitle();

		interceptPropertyChangeEvent = false;

		drawingPane = new DrawingPane();
		this.paramMapper = paramMapper;
		paramMapper.addListener(this);

		scale = 1;
		setGameScale(paramMapper.getFloat(Parameter.GAME_SCALING));
		setGuiScale(paramMapper.getFloat(Parameter.GUI_SCALING));

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createWindow(gameInputHandler);
			}
		});
	}

	/**
	 * Sets the amount to scale game elements by, then resizes this to match.
	 * 
	 * @param scale new size multiplier
	 */
	private void setGameScale(float scale) {
		this.scale = scale;
		int newWidth = (int) (idealWidth * scale);
		int newHeight = (int) (idealHeight * scale);

		centerX -= (newWidth - getWidth() + getInsets().left
				+ getInsets().right) / 2;
		centerY -= (newHeight - getHeight() + getTitlePaneHeight()
				+ getInsets().top + getInsets().bottom) / 2;
		if (getPreferredY() < 0) {
			centerY = -(int) (idealYOffset * scale);
		}
		if (getPreferredX() < 0) {
			centerX = -(int) (idealXOffset * scale);
		}

		getContentPane().setPreferredSize(new Dimension(newWidth, newHeight));
		pack();
		arrangeComponents();
		repaint();

		drawingPane.setScale(scale);
	}

	/**
	 * Sets the amount to scale GUI elements by, then updates the UI tree and
	 * packs this.
	 * 
	 * @param scale size multiplier
	 */
	private void setGuiScale(float scale) {
		Font font = UIManager.getFont("defaultFont");
		UIManager.put("defaultFont",
				font.deriveFont(DEFAULT_FONT_SIZE * scale));
		SwingUtilities.updateComponentTreeUI(this);

		updateTitleBarText(title);
		arrangeComponents();
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
		// Always return false so the event also gets dispatched to menu bar
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(e -> {
					if (e.getID() == KeyEvent.KEY_PRESSED) {
						gameInputHandler.keyPressed(e);
					}
					else if (e.getID() == KeyEvent.KEY_RELEASED) {
						gameInputHandler.keyReleased(e);
					}
					return false;
				});
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				// Move centerX|Y such that getPreferredX|Y() is getX|Y()
				if (getX() != getPreferredX()) {
					centerX = getX() - (int) (idealXOffset * scale);
				}
				if (getY() != getPreferredY()) {
					centerY = getY() - (int) (idealYOffset * scale);
				}
			}
		});

		setLayout(null);
		setResizable(false);

		getContentPane().add(drawingPane);

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

		idealWidth = level.width;
		idealHeight = level.height;
		idealXOffset = 0;
		idealYOffset = 0;
		getContentPane().setPreferredSize(new Dimension(
				(int) (idealWidth * scale), (int) (idealHeight * scale)));
		pack();

		updateTitleBarText(level.name);
		title = level.name;

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

	@Override
	public void resize(int change, Direction direction) {
		if (direction == Direction.NORTH || direction == Direction.WEST) {
			change *= -1;
		}

		if ((direction == Direction.NORTH || direction == Direction.SOUTH)
				&& (getHeight() + change < HEIGHT_MINIMUM * scale)) {
			change = (int) (HEIGHT_MINIMUM * scale) - getHeight();
		}
		if ((direction == Direction.WEST || direction == Direction.EAST)
				&& (getWidth() + change < WIDTH_MINIMUM * scale)) {
			change = (int) (WIDTH_MINIMUM * scale) - getWidth();
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
	 * Resizes this frame's preferred bounds, scaling resizes so the result is
	 * the same no matter how this is scaled. The amount of each resize is
	 * applied to the ideal size of this frame, not taking into account scaling.
	 * 
	 * @param resizes Map from {@code Direction} of each resize to change amount
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

		idealWidth += widthChange;
		idealHeight += heightChange;
		idealXOffset += xChange;
		idealYOffset += yChange;

		drawingPane.setOffsets(idealXOffset, idealYOffset);
		// Makes sure paintImmediately() doesn't interact badly with any Dialogs
		boolean existsVisibleDialog = Arrays.stream(Window.getWindows())
				.anyMatch(w -> (w instanceof Dialog && w.isVisible()));
		if (!existsVisibleDialog) {
			// Paint before arranging components to minimize stuttering
			drawingPane.paintImmediately(0, 0, drawingPane.getWidth(),
					drawingPane.getHeight());
		}

		xChange = 0;
		yChange = 0;
		widthChange = 0;
		heightChange = 0;

		arrangeComponents();
	}

	public void moveToMiddleOfScreen() {
		setLocationRelativeTo(null);
		centerX = getX();
		centerY = getY();
	}

	/**
	 * Lays out all components within this and sets its bounds.
	 */
	private void arrangeComponents() {

		setBounds(getPreferredX(), getPreferredY(), getPreferredWidth(),
				getPreferredHeight());

		int insetsX = getInsets().left + getInsets().right;
		int insetsY = getInsets().top + getInsets().bottom;

		for (Component comp : getLayeredPane().getComponents()) {

			if (comp instanceof ResizingSide) {

				int resizingSideThickness = paramMapper
						.getInt(Parameter.RESIZING_AREA_WIDTH);

				switch (((ResizingSide) comp).getDirection()) {
					case NORTH:
						if (resizingSideThickness > getTitlePaneHeight() / 2) {
							resizingSideThickness = getTitlePaneHeight() / 2;
						}
						comp.setBounds(0, 0, getWidth()
								- getTitlePaneButtonsWidth() - insetsX,
								resizingSideThickness);
						break;
					case SOUTH:
						comp.setBounds(0,
								getHeight() - resizingSideThickness - insetsY,
								getWidth(), resizingSideThickness);
						break;
					case WEST:
						comp.setBounds(0, resizingSideThickness,
								resizingSideThickness,
								getHeight() - resizingSideThickness);
						break;
					case EAST:
						comp.setBounds(
								getWidth() - resizingSideThickness - insetsX,
								getTitlePaneHeight(), resizingSideThickness,
								getHeight() - resizingSideThickness
										- getTitlePaneHeight());
				}
			}

		}
		if (drawingPane != null)
			drawingPane.setBounds(0, 0, getContentPane().getWidth(),
					getContentPane().getHeight());
	}

	private int getPreferredX() {
		return centerX + (int) (idealXOffset * scale);
	}

	private int getPreferredY() {
		return centerY + (int) (idealYOffset * scale);
	}

	private int getPreferredWidth() {
		return (int) (idealWidth * scale) + getInsets().left
				+ getInsets().bottom;
	}

	private int getPreferredHeight() {
		return (int) (idealHeight * scale) + getTitlePaneHeight()
				+ getInsets().top + getInsets().bottom;
	}

	/**
	 * Sets only the text in the title pane. Task bar text is unaffected.
	 * 
	 * @param newText text to display
	 */
	private void updateTitleBarText(String newText) {
		String taskbarText = getTitle();
		setTitle("<html><body><b>" + newText + "</b></body></html>");
		interceptPropertyChangeEvent = true;  // Block FlatTitlePane from
		setTitle(taskbarText);  				// setting its text back
		interceptPropertyChangeEvent = false;
	}

	/**
	 * @return Current width + pending width changes
	 */
	public int getNextWidth() {
		return idealWidth + widthChange;
	}

	/**
	 * @return Current height + pending height changes
	 */
	public int getNextHeight() {
		return idealHeight + heightChange;
	}

	/**
	 * @return Current x offset + pending changes
	 */
	public int getNextXOffset() {
		return idealXOffset + xChange;
	}

	/**
	 * @return Current y offset + pending changes
	 */
	public int getNextYOffset() {
		return idealYOffset + yChange;
	}

	private int getTitlePaneHeight() {
		getContentPane().validate();
		return getHeight() - getContentPane().getHeight() - getInsets().top
				- getInsets().bottom;
	}

	private int getTitlePaneButtonsWidth() {
		return 2 * UIManager.getDimension("TitlePane.buttonSize").width;
	}

	@Override
	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		if (interceptPropertyChangeEvent) {
			return;
		}

		super.firePropertyChange(propertyName, oldValue, newValue);
	}

	@Override
	public void valueChanged(Enum<?> key, Object newValue) {
		if (key == Parameter.GUI_SCALING) {
			setGuiScale(((Number) newValue).floatValue());
		}
		if (key == Parameter.GAME_SCALING) {
			setGameScale(((Number) newValue).floatValue());
		}
	}

	@Override
	public void valueRemoved(Enum<?> key) {}

}
