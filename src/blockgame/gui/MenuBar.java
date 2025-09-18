package blockgame.gui;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import blockgame.GameController;
import blockgame.input.ColorMapper;
import blockgame.input.InputMapper;
import blockgame.input.ParameterMapper;
import blockgame.input.SoundMapper;
import blockgame.input.ValueChangeListener;
import blockgame.sound.MusicPlayer;
import blockgame.util.Pair;

/**
 * {@code JMenuBar} to handle inputs which are not directly related to playing
 * the game, for example pausing, reloading the level, or saving a recording
 * file.
 * <p>
 * If an input requires multiple pieces to be acted on, this will interrupt the
 * game to get the extra information. For instance, loading a file requires
 * asking the player which file to load.
 * 
 * @author Frank Kormann
 */
public class MenuBar extends JMenuBar implements ValueChangeListener {

	public enum MetaInput {
		PAUSE, FRAME_ADVANCE, RELOAD_LEVEL, TOGGLE_HINTS, PLAY_SOLUTION,
		SAVE_RECORDING, PLAY_RECORDING, STOP_RECORDING
	}

	private GameController listener;
	private InputMapper inputMapper;

	private JFileChooser fileChooser;
	private Map<Enum<?>, JMenuItem> inputToMenuItem;
	private List<JMenu> menus;
	private Map<JMenu, Integer> menuWidths;

	private JMenu hintMenu;
	private JMenu levelSelectButton;
	private JMenu moreMenu;
	private JMenuItem showHintItem;
	private JMenuItem showSolutionItem;
	private JMenuItem pauseItem;
	private JMenuItem frameAdvanceItem;

	/**
	 * Creates a new {@code MenuBar} to handle {@code MetaInput}s. Inputs are
	 * sent to {@code listener}. {@code MetaInput} keybinds are taken from
	 * {@code inputMapper}.
	 * <p>
	 * Includes an {@code OptionsDialog} to change mappings in
	 * {@code inputMapper}, {@code colorMapper}, and {@code paramMaper}, and to
	 * change song in {@code musicPlayer}.
	 * 
	 * @param inputMapper {@code InputMapper} to take keybinds from and to alter
	 *                    in {@code OptionsDialog}
	 * @param colorMapper {@code ColorMapper} to alter in {@code OptionsDialog}
	 * @param paramMapper {@code ParameterMapper} to alter in
	 *                    {@code OptionsDialog}
	 * @param soundMapper {@code SoundMapper} to alter in {@code OptionsDialog}
	 * @param musicPlayer {@code MusicPlayer} to play music
	 * @param listener    {@code GameController} which will process inputs
	 */
	public MenuBar(InputMapper inputMapper, ColorMapper colorMapper,
			ParameterMapper paramMapper, SoundMapper soundMapper,
			MusicPlayer musicPlayer, GameController listener) {
		this.listener = listener;
		this.inputMapper = inputMapper;
		inputToMenuItem = new HashMap<>();
		menus = new ArrayList<>();
		menuWidths = new HashMap<>();

		// Global file chooser so it remembers which directory the user was in
		// if they open it multiple times
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(
				new FileNameExtensionFilter("Recording Files (.rec)", "rec"));

		inputMapper.addListener(this);
		hintMenu = createHintMenu();
		levelSelectButton = createLevelSelectButton(listener);
		moreMenu = new JMenu("More");

		add(hintMenu);
		add(createPauseRestartMenu());
		add(createRecordingMenu());
		add(createOptionsButton(colorMapper, paramMapper, soundMapper,
				musicPlayer));
		add(levelSelectButton);
		super.add(moreMenu);
	}

	@Override
	public JMenu add(JMenu menu) {
		menus.add(menu);
		return super.add(menu);
	}

	@Override
	public Dimension getPreferredSize() {
		int sumWidth = 0;
		for (JMenu menu : menus) {
			if (menu.isVisible()) {
				sumWidth += menuWidths.get(menu);
			}
		}
		return new Dimension(sumWidth, super.getPreferredSize().height);
	}

	/**
	 * Lays out all the {@code JMenu}s in this, collapsing them into
	 * {@code moreMenu} if necessary.
	 */
	@Override
	public void doLayout() {
		int x = 0;
		JMenu menu, nextMenu;
		boolean overflowing = false;
		for (int i = 0; i < menus.size(); i++) {
			menu = menus.get(i);
			nextMenu = i + 1 < menus.size() ? menus.get(i + 1) : null;
			if (!menu.isVisible()) {
				continue;
			}

			int nextMenuWidth = 0;
			if (nextMenu != null && nextMenu.isVisible()) {
				nextMenuWidth = Math.min(menuWidths.get(nextMenu),
						menuWidths.get(moreMenu));
			}
			if (x + menuWidths.get(menu) + nextMenuWidth > getWidth()) {
				overflowing = true;
			}

			layoutMenu(menu, x, !overflowing);
			if (!overflowing) {
				x += menu.getPreferredSize().width;
			}
		}
		moreMenu.setVisible(moreMenu.getItemCount() > 0);
		layoutMenu(moreMenu, x, moreMenu.isVisible());
	}

	/**
	 * Sets the bounds of {@code menu} and displays it as a top-level menu or in
	 * {@code moreMenu}.
	 * 
	 * @param menu       {@code JMenu} to layout
	 * @param x          position in the menu bar if it is top-level
	 * @param isTopLevel {@code true} if {@code menu} should be a top-level
	 *                   menu; otherwise, it will be added to {@code moreMenu}
	 */
	private void layoutMenu(JMenu menu, int x, boolean isTopLevel) {
		if (isTopLevel) {
			super.add(menu);
			menu.setBounds(x, 0, menuWidths.get(menu), getHeight() - 1);
		}
		else {
			if (menu != moreMenu) {
				moreMenu.add(menu);
			}
			menu.setBounds(0, 0, 0, 0);
		}
	}

	@Override
	public void updateUI() {
		super.updateUI();
		if (menus != null) {
			// Make sure preferredSize reflects size as a top-level menu
			menus.forEach(m -> super.add(m));
			menus.forEach(m -> menuWidths.put(m, m.getPreferredSize().width));
			menuWidths.put(moreMenu, moreMenu.getPreferredSize().width);
		}
	}

	/**
	 * Resets the enabledness and selectedness of certain {@code JMenuItem}s
	 * back to their original states.
	 */
	public void reset() {
		showHintItem.setSelected(false);
		pauseItem.setSelected(false);
		showSolutionItem.setEnabled(false);
		frameAdvanceItem.setEnabled(false);
	}

	/**
	 * Sets visible or not the {@code JMenu} which contains options to show
	 * hints and level solution.
	 * 
	 * @param show {@code true} if it should be shown
	 */
	public void showHintsMenu(boolean show) {
		hintMenu.setVisible(show);
	}

	/**
	 * Sets visible or not the {@code JMenu} which contains a button to show a
	 * {@code LevelSelectDialog}.
	 * 
	 * @param show {@code true} if it should be shown
	 */
	public void showLevelSelect(boolean show) {
		levelSelectButton.setVisible(show);
	}

	private JMenu createHintMenu() {
		JMenu menu = new JMenu("Hint");

		showHintItem = createMenuItem("Show hint", MetaInput.TOGGLE_HINTS,
				this::hintsAction, true);

		showSolutionItem = createMenuItem("Show solution",
				MetaInput.PLAY_SOLUTION, this::showSolutionAction, false);
		showSolutionItem.addPropertyChangeListener("enabled", e -> {
			showSolutionItem.setToolTipText(showSolutionItem.isEnabled() ? null
					: "Please view the hint before using this option");
		});
		showSolutionItem.setEnabled(false);

		menu.add(showHintItem);
		menu.add(showSolutionItem);

		return menu;
	}

	private JMenu createRecordingMenu() {
		JMenu menu = new JMenu("Recordings");

		JMenuItem openItem = createMenuItem("Open", MetaInput.PLAY_RECORDING,
				this::openRecordingAction, false);

		JMenuItem saveItem = createMenuItem("Save current",
				MetaInput.SAVE_RECORDING, this::saveRecordingAction, false);

		JMenuItem stopItem = createMenuItem("Stop", MetaInput.STOP_RECORDING,
				this::stopRecordingAction, false);

		menu.add(openItem);
		menu.add(saveItem);
		menu.add(stopItem);

		return menu;
	}

	private JMenu createPauseRestartMenu() {
		JMenu menu = new JMenu("Pause/Retry");

		pauseItem = createMenuItem("Pause", MetaInput.PAUSE, this::pauseAction,
				true);

		JMenuItem restartItem = createMenuItem("Retry", MetaInput.RELOAD_LEVEL,
				this::restartAction, false);

		frameAdvanceItem = createMenuItem("Frame advance",
				MetaInput.FRAME_ADVANCE, this::frameAdvanceAction, false);

		menu.add(pauseItem);
		menu.add(restartItem);
		menu.add(frameAdvanceItem);

		return menu;
	}

	private JMenu createOptionsButton(ColorMapper colorMapper,
			ParameterMapper paramMapper, SoundMapper soundMapper,
			MusicPlayer musicPlayer) {
		Runnable openDialog = () -> {
			new OptionsDialog(SwingUtilities.getWindowAncestor(this),
					inputMapper, colorMapper, paramMapper, soundMapper,
					musicPlayer).setVisible(true);
		};

		return createMenuButton("Options", openDialog);
	}

	private JMenu createLevelSelectButton(GameController gameController) {
		Runnable openDialog = () -> {
			new LevelSelectDialog(SwingUtilities.getWindowAncestor(this),
					gameController).setVisible(true);
		};

		return createMenuButton("Level Select", openDialog);
	}

	/**
	 * Creates a {@code JMenuItem} with the specified properties. {@code action}
	 * will be run when the item is selected or the shortcut from
	 * {@code metaInput} is pressed.
	 * 
	 * @param text       to display on the menu item
	 * @param metaInput  {@code MetaInput} to pass to pull keybind information
	 *                   from
	 * @param action     {@code Runnable} which will run when this is activated
	 * @param isCheckBox whether to use a {@code JCheckBoxMenuItem} or not
	 * 
	 * @return the {@code JMenuItem}
	 */
	private JMenuItem createMenuItem(String text, MetaInput metaInput,
			Runnable action, boolean isCheckBox) {

		JMenuItem menuItem;
		if (isCheckBox) {
			menuItem = new JCheckBoxMenuItem(text);
		}
		else {
			menuItem = new JMenuItem(text);
		}

		Pair<Integer, Integer> keybind = inputMapper.get(metaInput);
		menuItem.addActionListener(e -> action.run());
		if (keybind != null && keybind.first != 0) {
			menuItem.setAccelerator(
					KeyStroke.getKeyStroke(keybind.first, keybind.second));
		}

		inputToMenuItem.put(metaInput, menuItem);

		return menuItem;
	}

	/**
	 * Creates a {@code JMenu} which acts as a button. When it is pressed, it
	 * runs {@code action}.
	 * 
	 * @param text   label on the button
	 * @param action {@code Runnable} to run when the button is pressed
	 * 
	 * @return the {@code JMenu}
	 */
	private JMenu createMenuButton(String text, Runnable action) {
		JMenu button = new JMenu(text);
		button.addMenuKeyListener(new MenuKeyListener() {
			@Override
			public void menuKeyPressed(MenuKeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_ENTER
						|| e.getKeyCode() == KeyEvent.VK_SPACE)
						&& button.isSelected()) {
					action.run();
				}
			}

			@Override
			public void menuKeyTyped(MenuKeyEvent e) {}

			@Override
			public void menuKeyReleased(MenuKeyEvent e) {}
		});
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// In a new thread to fix a problem related to updating
				// component UI while in a dialog; AWT tries to send a mouse
				// pressed event to the old UI, but its component is now null
				new Thread(action).start();
			}
		});

		return button;
	}

	private File promptFileSaveLocation() {

		int fileChooserResult;
		File saveFile;

		boolean canSave;
		do {
			canSave = true;

			fileChooser.setSelectedFile(new File("Untitled.rec"));

			fileChooserResult = fileChooser
					.showSaveDialog(SwingUtilities.getWindowAncestor(this));
			saveFile = fileChooser.getSelectedFile();
			if (saveFile == null) {
				return null;
			}

			if (!saveFile.getName().matches("^.*\\..*$")) {
				saveFile = new File(saveFile.getPath() + ".rec");
			}

			if (fileChooserResult == JFileChooser.APPROVE_OPTION
					&& saveFile.exists()) {
				String message = saveFile.getName()
						+ " already exists.\nDo you want to replace it?";
				canSave = JOptionPane.showConfirmDialog(fileChooser, message,
						"Confirm save",
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION;
			}
		} while (!canSave);

		if (fileChooserResult == JFileChooser.APPROVE_OPTION) {
			return saveFile;
		}

		return null;
	}

	private File promptFileOpenLocation() {
		int result = fileChooser
				.showOpenDialog(SwingUtilities.getWindowAncestor(this));
		File openFile = fileChooser.getSelectedFile();

		if (result == JFileChooser.APPROVE_OPTION) {
			return openFile;
		}

		return null;
	}

	private void hintsAction() {
		listener.processMetaInput(MetaInput.TOGGLE_HINTS);
		showSolutionItem.setEnabled(true);
	}

	private void showSolutionAction() {
		listener.processMetaInput(MetaInput.PLAY_SOLUTION);
	}

	private void openRecordingAction() {
		File file = promptFileOpenLocation();
		if (file == null) {
			return;
		}
		listener.processMetaInput(MetaInput.PLAY_RECORDING, file);
	}

	private void saveRecordingAction() {
		File saveFile = promptFileSaveLocation();
		if (saveFile == null) {
			return;
		}
		listener.processMetaInput(MetaInput.SAVE_RECORDING, saveFile);
	}

	private void stopRecordingAction() {
		listener.processMetaInput(MetaInput.STOP_RECORDING);
	}

	private void pauseAction() {
		listener.processMetaInput(MetaInput.PAUSE);
		frameAdvanceItem.setEnabled(!frameAdvanceItem.isEnabled());
	}

	private void restartAction() {
		listener.processMetaInput(MetaInput.RELOAD_LEVEL);
	}

	private void frameAdvanceAction() {
		listener.processMetaInput(MetaInput.FRAME_ADVANCE);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void valueChanged(Enum<?> input, Object newKeybind) {
		if (inputToMenuItem.containsKey(input)) {
			Pair<Integer, Integer> keybind = (Pair<Integer, Integer>) newKeybind;
			inputToMenuItem.get(input)
					.setAccelerator(KeyStroke.getKeyStroke(keybind.first,
							keybind.second));
		}
	}

	@Override
	public void valueRemoved(Enum<?> input) {
		if (inputToMenuItem.containsKey(input)) {
			inputToMenuItem.get(input).setAccelerator(null);
		}
	}

}
