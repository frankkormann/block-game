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

		showHintItem = createMenuItem("Show hint", KeyEvent.VK_H, 0, () -> {
			listener.processMetaInput(MetaInput.TOGGLE_HINTS);
			showSolutionItem.setEnabled(true);
		}, true);

		showSolutionItem = createMenuItem("Show solution", KeyEvent.VK_H,
				KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK, () -> {
					listener.processMetaInput(MetaInput.PLAY_SOLUTION);
				}, false);
		showSolutionItem.setEnabled(false);

		hintMenu.add(showHintItem);
		hintMenu.add(showSolutionItem);

		return hintMenu;
	}

	/**
	 * Creates a {@code JMenuItem} with the specified properties. {@code toDo}
	 * will be run when the item is selected or {@code keyCode} is pressed.
	 * 
	 * @param text       to display on the menu item
	 * @param keyCode    key code which will activate it
	 * @param modifiers  modifier mask to the key code
	 * @param toDo       action to perform when selected
	 * @param isCheckBox whether to use a {@code JCheckBoxMenuItem} or not
	 * 
	 * @return the {@code JMenuItem}
	 */
	private JMenuItem createMenuItem(String text, int keyCode, int modifiers,
			Runnable toDo, boolean isCheckBox) {

		AbstractAction action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toDo.run();
			}
		};
		action.putValue(Action.NAME, text);

		JMenuItem menuItem;
		if (isCheckBox) {
			menuItem = new JCheckBoxMenuItem(action);
		}
		else {
			menuItem = new JMenuItem(action);
		}

		menuItem.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(keyCode, modifiers), "Action");
		menuItem.getActionMap().put("Action", action);

		return menuItem;
	}

}
