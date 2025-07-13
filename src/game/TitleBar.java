package game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Emulates the Windows title bar.
 * <p>
 * This is necessary to place a {@code ResizingSide} above the title bar. If the
 * default Windows window decorations were kept, nothing could be placed above
 * it.
 * 
 * @author Frank Kormann
 */
public class TitleBar extends JPanel
		implements ActionListener, MouseListener, MouseMotionListener {

	public static final int HEIGHT = 30;

	public static final String TITLE_ICON = "/title_icon.png";
	public static final String MINIMIZE_ICON = "/minimize_icon.png";
	public static final String CLOSE_ICON = "/close_icon.png";
	public static final String CLOSE_HOVER_ICON = "/close_icon_hover.png";

	private int mouseX, mouseY;

	private Movable movementListener;
	private JFrame eventListener;

	private JLabel titleLabel;
	private JPanel buttonHolder;

	/**
	 * Creates a {@code TitleBar} and lays out its buttons and icons.
	 * <p>
	 * When the user drags this, it will call {@code movementListener}'s
	 * {@link Movable#move2(int, int)} method. When they activate the close or
	 * minimize buttons, this will close or minimize {@code eventListener}.
	 * 
	 * @param movementListener {@code Movable} to send dragging movement to
	 * @param eventListener    {@code JFrame} to send close/minimize events to
	 */
	public TitleBar(Movable movementListener, JFrame eventListener) {
		this.movementListener = movementListener;
		this.eventListener = eventListener;

		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		setLayout(new BorderLayout());
		addMouseListener(this);
		addMouseMotionListener(this);

		titleLabel = new JLabel();
		buttonHolder = createButtons();

		add(buttonHolder, BorderLayout.EAST);
		add(createIconAndTitle(titleLabel), BorderLayout.WEST);
	}

	private JPanel createButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

		TitleButton minimizeButton = new TitleButton(MINIMIZE_ICON,
				new Color(0, 0, 0, 0), new Color(0, 0, 0, 16));
		minimizeButton.setActionCommand("minimize");
		minimizeButton.addActionListener(this);

		TitleButton closeButton = new TitleButton(CLOSE_ICON, CLOSE_HOVER_ICON,
				new Color(0, 0, 0, 0), new Color(210, 46, 29));
		closeButton.setActionCommand("exit");
		closeButton.addActionListener(this);

		panel.add(minimizeButton);
		panel.add(closeButton);

		return panel;
	}

	private JPanel createIconAndTitle(JLabel titleLabel) {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		JLabel icon = new JLabel();
		try {
			icon.setIcon(
					new ImageIcon(ImageIO.read(getClass().getResource(TITLE_ICON))));
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Failed to load resource for titlebar icon", e)
					.setVisible(true);
		}

		panel.add(icon);
		panel.add(titleLabel);

		return panel;
	}

	public void setTitle(String title) {
		this.titleLabel.setText(title);
	}

	/**
	 * Get the x value where buttons start on this title bar.
	 * 
	 * @return x coordinate of first button
	 */
	public int getButtonsX() {
		return buttonHolder.getX();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case "exit":
				eventListener.dispatchEvent(
						new WindowEvent(eventListener, WindowEvent.WINDOW_CLOSING));
				break;
			case "minimize":
				eventListener.setState(JFrame.ICONIFIED);
				break;
		}
	}

	public void mousePressed(MouseEvent e) {
		mouseX = e.getXOnScreen();
		mouseY = e.getYOnScreen();
	}

	public void mouseDragged(MouseEvent e) {
		int xDifference = e.getXOnScreen() - mouseX;
		int yDifference = e.getYOnScreen() - mouseY;

		movementListener.move2(xDifference, yDifference);

		mouseX = e.getXOnScreen();
		mouseY = e.getYOnScreen();
	}

	@Override
	public void mouseMoved(MouseEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	/**
	 * Simple button which sets its color depending on whether it is being hovered
	 * over or not.
	 */
	private class TitleButton extends JButton {

		private TitleButton(String icon, Color color, Color hoverColor) {
			this(icon, icon, color, hoverColor);
		}

		private TitleButton(String icon, String iconHover, Color color,
				Color hoverColor) {

			setFocusable(false);
			setBorder(BorderFactory.createEmptyBorder());
			setBackground(color);
			setIcon(icon);

			getModel().addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					if (getModel().isRollover()) {
						setBackground(hoverColor);
						setIcon(iconHover);
					}
					else {
						setBackground(color);
						setIcon(icon);
					}
				}
			});

		}

		private void setIcon(String path) {
			try {
				setIcon(new ImageIcon(ImageIO.read(getClass().getResource(path))));
			}
			catch (IOException e) {
				e.printStackTrace();
				new ErrorDialog("Error", "Failed to load resource for button icon", e)
						.setVisible(true);
			}
		}

	}

}
