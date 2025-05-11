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

	private int mouseX, mouseY;
	private MainFrame actionListener;

	private JLabel titleLabel;
	private JPanel buttonHolder;

	public TitleBar(MainFrame actionListener) {
		this.actionListener = actionListener;

		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		setLayout(new BorderLayout());
		addMouseListener(this);
		addMouseMotionListener(this);

		// Create icon and title on the left
		JPanel leftHolder = new JPanel();
		leftHolder.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		add(leftHolder, BorderLayout.WEST);

		JLabel icon = new JLabel();
		try {
			icon.setIcon(new ImageIcon(
					ImageIO.read(getClass().getResource("/title_icon.png"))));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		leftHolder.add(icon);

		titleLabel = new JLabel();
		leftHolder.add(titleLabel);

		// Create both buttons on the right
		buttonHolder = new JPanel();
		buttonHolder.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		add(buttonHolder, BorderLayout.EAST);

		TitleButton minimizeButton = new TitleButton("/minimize_icon.png",
				new Color(0, 0, 0, 0), new Color(0, 0, 0, 16));
		minimizeButton.setActionCommand("minimize");
		minimizeButton.addActionListener(this);
		buttonHolder.add(minimizeButton);

		TitleButton closeButton = new TitleButton("/close_icon.png",
				"/close_icon_hover.png", new Color(0, 0, 0, 0), new Color(210, 46, 29));
		closeButton.setActionCommand("exit");
		closeButton.addActionListener(this);
		buttonHolder.add(closeButton);
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
				actionListener.dispatchEvent(
						new WindowEvent(actionListener, WindowEvent.WINDOW_CLOSING));
				break;
			case "minimize":
				actionListener.setState(JFrame.ICONIFIED);
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

		actionListener.move2(xDifference, yDifference);

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
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

}
