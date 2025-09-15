package blockgame.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.util.UIScale;

import blockgame.Level;
import blockgame.input.GameInputHandler;
import blockgame.input.ParameterMapper;
import blockgame.input.ParameterMapper.Parameter;
import blockgame.input.ValueChangeListener;

/**
 * {@code JFrame} that displays each level. Buffers changes to width/height
 * between each frame.
 * <p>
 * The usual methods for resizing a {@code JFrame} should not be used for this.
 * The methods {@link #resize(int, Direction)} and {@link #resizeAll(Map)}
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
 * width/height and title. Then add each {@code Rectangle} with {@code add}. A
 * level should be loaded before calling {@code setVisible(true)} to avoid
 * graphical glitches.
 * <p>
 * Note: Assumes look-and-feel is FlatLightLaf
 * 
 * @author Frank Kormann
 */
public class MainFrame extends JFrame implements ValueChangeListener {

	private static final String TASKBAR_ICON = "/taskbar_icon.png";
	private static final String WINDOW_TITLE = "Resizer";
	private static final int DEFAULT_FONT_SIZE = 13;

	private static final int WIDTH_MINIMUM = 150;
	private static final int HEIGHT_MINIMUM = 150;

	private float scale;
	private int idealWidth, idealHeight, idealXOffset, idealYOffset;
	private int centerX, centerY;
	private int xChange, yChange;
	private int widthChange, heightChange;

	private int titlePaneHeight;
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

		// idealXxx, width, height are instantiated in
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
	 * Sets the amount to scale GUI elements by.
	 * 
	 * @param scale size multiplier
	 */
	private void setGuiScale(float scale) {
		Font font = UIManager.getFont("defaultFont");
		UIManager.put("defaultFont",
		        font.deriveFont(DEFAULT_FONT_SIZE * scale));

		updateTitleBarText(title);
		arrangeComponents();
	}

	private void createWindow(GameInputHandler gameInputHandler) {
		try {
			setIconImage(ImageIO.read(getClass().getResource(TASKBAR_ICON)));
		}
		catch (IOException | IllegalArgumentException e) {
			e.printStackTrace();
			ErrorDialog.showDialog("Failed to load resource for taskbar icon",
			        e);
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
				// so that this will not be moved back by arrangeComponents()
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
	 * <p>
	 * This should be called from the AWT Event Dispatching Thread.
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
		titlePaneHeight = getTitlePaneHeight();

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

	public void resize(int change, Direction direction) {
		if (direction == Direction.NORTH || direction == Direction.WEST) {
			change *= -1;
		}

		if (direction == Direction.NORTH || direction == Direction.SOUTH) {
			if (idealHeight + change < HEIGHT_MINIMUM) {
				change = HEIGHT_MINIMUM - idealHeight;
			}
			int screenHeight = Toolkit.getDefaultToolkit()
			        .getScreenSize().height;
			if (getHeight() + (int) (change * scale) > screenHeight) {
				change = (int) (screenHeight - getHeight() / scale);
			}
		}
		if (direction == Direction.WEST || direction == Direction.EAST) {
			if (idealWidth + change < WIDTH_MINIMUM) {
				change = WIDTH_MINIMUM - idealWidth;
			}
			int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
			if (getWidth() + (int) (change * scale) > screenWidth) {
				change = (int) ((screenWidth - getWidth()) / scale);
			}
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
	 * Resizes this frame's preferred bounds. The amount of each resize is
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
	 * This should be called from the AWT Event Dispatching Thread.
	 */
	public void incorporateChanges() {
		idealWidth += widthChange;
		idealHeight += heightChange;
		idealXOffset += xChange;
		idealYOffset += yChange;

		drawingPane.setOffsets(idealXOffset, idealYOffset);

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
		if (titlePaneHeight != getTitlePaneHeight()) {
			centerY += titlePaneHeight - getTitlePaneHeight();
			titlePaneHeight = getTitlePaneHeight();
		}

		setBounds(getPreferredX(), getPreferredY(), getPreferredWidth(),
		        getPreferredHeight());

		int insetsX = getInsets().left + getInsets().right;
		int insetsY = getInsets().top + getInsets().bottom;

		for (Component comp : getLayeredPane().getComponents()) {

			if (comp instanceof ResizingSide) {

				int resizingSideThickness = paramMapper
				        .getInt(Parameter.RESIZING_AREA_WIDTH);
				int northThickness = Math.min(resizingSideThickness,
				        getTitlePaneHeight() / 2);

				switch (((ResizingSide) comp).getDirection()) {
					case NORTH:
						comp.setBounds(0, 0, getWidth()
						        - getTitlePaneButtonsWidth() - insetsX,
						        northThickness);
						break;
					case SOUTH:
						comp.setBounds(0,
						        getHeight() - resizingSideThickness - insetsY,
						        getWidth(), resizingSideThickness);
						break;
					case WEST:
						comp.setBounds(0, northThickness, resizingSideThickness,
						        getHeight() - resizingSideThickness
						                - northThickness - insetsY);
						break;
					case EAST:
						comp.setBounds(
						        getWidth() - resizingSideThickness - insetsX,
						        getTitlePaneHeight(), resizingSideThickness,
						        getHeight() - resizingSideThickness
						                - getTitlePaneHeight() - insetsY);
				}
			}

		}
		if (drawingPane != null) {
			drawingPane.setBounds(0, 0,
			        (int) Math.min(getContentPane().getWidth(),
			                idealWidth * scale),
			        (int) Math.max(getContentPane().getHeight(),
			                idealHeight * scale));
		}
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
		int textWidth = getFontMetrics(
		        UIManager.getFont("TitlePane.font").deriveFont(Font.BOLD))
		        .stringWidth(newText)
		        + UIManager.getInt("TitlePane.buttonMinimumWidth");
		// Need to unscale it because FlatLaf re-scales it
		UIManager.put("TitlePane.titleMinimumWidth",
		        UIScale.unscale(textWidth)); // Need to unscale it because
		                                     // FlatLaf re-scales it
		SwingUtilities.updateComponentTreeUI(this);

		newText = "<html><b>" + newText + "</b></html>";
		newText = newText.replace(' ', ' ');
// Normal space -^ ^- Non-breaking space
		String taskbarText = getTitle();
		setTitle(newText);
		interceptPropertyChangeEvent = true; // Block FlatTitlePane from
		setTitle(taskbarText);               // setting its text back
		interceptPropertyChangeEvent = false;
	}

	/**
	 * Returns the buffered ideal width.
	 * 
	 * @return Current width + pending width changes
	 */
	public int getNextWidth() {
		return idealWidth + widthChange;
	}

	/**
	 * Returns the buffered ideal height.
	 * 
	 * @return Current height + pending height changes
	 */
	public int getNextHeight() {
		return idealHeight + heightChange;
	}

	/**
	 * Returns the buffered ideal x-offset.
	 * 
	 * @return Current x-offset + pending changes
	 */
	public int getNextXOffset() {
		return idealXOffset + xChange;
	}

	/**
	 * Returns the buffered ideal y-offset.
	 * 
	 * @return Current y-offset + pending changes
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
