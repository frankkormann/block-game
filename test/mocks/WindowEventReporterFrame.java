package mocks;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

public class WindowEventReporterFrame extends JFrame implements WindowListener {

	public int lastEventReceived;

	public WindowEventReporterFrame() {
		lastEventReceived = 0;
		addWindowListener(this);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		lastEventReceived = e.getID();
	}

	@Override
	public void windowClosing(WindowEvent e) {
		lastEventReceived = e.getID();
	}

	@Override
	public void windowClosed(WindowEvent e) {
		lastEventReceived = e.getID();
	}

	@Override
	public void windowIconified(WindowEvent e) {
		lastEventReceived = e.getID();
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		lastEventReceived = e.getID();
	}

	@Override
	public void windowActivated(WindowEvent e) {
		lastEventReceived = e.getID();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		lastEventReceived = e.getID();
	}

}
