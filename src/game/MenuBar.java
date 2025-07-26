package game;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import game.MetaInputHandler.MetaInput;

public class MenuBar extends JMenuBar {

	private GameController listener;

	private JMenuItem showHintItem;
	private JMenuItem showSolutionItem;

	public MenuBar(GameController listener) {
		this.listener = listener;

		JMenu hintMenu = createHintMenu();

		add(hintMenu);
	}

	public void reset() {
		showHintItem.setSelected(false);
		showSolutionItem.setEnabled(false);
	}

	private JMenu createHintMenu() {
		JMenu hintMenu = new JMenu("Hint");

		AbstractAction showHint = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listener.processMetaInput(MetaInput.TOGGLE_HINTS);
				showSolutionItem.setEnabled(true);
			}
		};
		showHint.putValue(Action.NAME, "Show hint");
		showHintItem = new JCheckBoxMenuItem(showHint);
		showHintItem.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0), "Action");
		showHintItem.getActionMap().put("Action", showHint);

		AbstractAction showSolution = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listener.processMetaInput(MetaInput.PLAY_SOLUTION);
			}
		};
		showSolution.putValue(Action.NAME, "Show solution");
		showSolutionItem = new JMenuItem(showSolution);
		showHintItem.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), "Action");
		showHintItem.getActionMap().put("Action", showSolution);
		showSolutionItem.setEnabled(false);

		hintMenu.add(showHintItem);
		hintMenu.add(showSolutionItem);

		return hintMenu;
	}

}
