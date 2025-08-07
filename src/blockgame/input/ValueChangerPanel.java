package blockgame.input;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

/**
 * {@code JPanel} that allows the user to change mappings in a {@code Mapper}.
 * <p>
 * This implements the functionality of binding {@code GetterSetter}s to the
 * {@code Mapper}, creating {@code JButton}s to undo or reset mappings, and
 * saving mappings once the parent window is closed. Subclasses must define how
 * to create a {@code GetterSetter} and how to lay out {@code GetterSetter}s.
 * 
 * @param <T> type of value to change
 * 
 * @author Frank Kormann
 */
public abstract class ValueChangerPanel<T> extends JPanel
		implements ValueChangeListener {

	private static final String UNDO_TEXT = "Undo changes";
	private static final String RESET_TEXT = "Reset to defaults";

	private Mapper<T> mapper;
	private Map<Enum<?>, GetterSetter<T>> enumToGetterSetter;

	public ValueChangerPanel(JRootPane rootPane, Mapper<T> mapper) {
		super();
		this.mapper = mapper;
		enumToGetterSetter = new HashMap<>();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		for (Class<? extends Enum<?>> enumClass : mapper.getEnumClasses()) {
			for (Enum<?> enumValue : enumClass.getEnumConstants()) {
				GetterSetter<T> getterSetter = createGetterSetter(enumValue);
				bindComponent(getterSetter, enumValue);
			}
		}
		add(createGetterSetterPanel(enumToGetterSetter));
		add(createUndoResetButtonPanel());

		Window window = SwingUtilities.getWindowAncestor(rootPane);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cleanUp();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				cleanUp();
			}
		});
		mapper.addListener(this);
	}

	/**
	 * Creates a {@code GetterSetter} for {@code enumValue}. It is automatically
	 * bound to {@code enumValue} in {@code Mapper}.
	 * 
	 * @param enumValue value to create a {@code GetterSetter} for
	 * 
	 * @return the {@code GetterSetter}
	 */
	protected abstract GetterSetter<T> createGetterSetter(Enum<?> enumValue);

	/**
	 * Creates a {@code JPanel} to display each {@code GetterSetter} in
	 * {@code getterSetters}.
	 * 
	 * @param getterSetters {@code Map} from the enum value for this
	 *                      {@code GetterSetter} to the {@code GetterSetter}
	 * 
	 * @return the {@code JPanel}
	 */
	protected abstract JPanel createGetterSetterPanel(
			Map<Enum<?>, GetterSetter<T>> getterSetters);

	/**
	 * Sets {@code component} to stick to and change the value of
	 * {@code enumValue} in the {@code Mapper}.
	 * 
	 * @param component {@code GetterSetter} to bind
	 * @param enumValue value to bind to in {@code Mapper}
	 */
	private void bindComponent(GetterSetter<T> component, Enum<?> enumValue) {
		if (mapper.get(enumValue) != null) {
			component.set(mapper.get(enumValue));
		}
		component.addChangeListener(
				() -> mapper.set(enumValue, component.get()));
		enumToGetterSetter.put(enumValue, component);
	}

	/**
	 * Creates a {@code JPanel} with a {@code JButton} to undo unsaved changes
	 * and a {@code JButton} to reset values back to defaults.
	 * 
	 * @return the {@code JPanel}
	 */
	protected JPanel createUndoResetButtonPanel() {
		JPanel panel = new JPanel();

		JButton undoButton = new JButton(UNDO_TEXT);
		undoButton.addActionListener(e -> {
			mapper.reload();
		});

		JButton resetButton = new JButton(RESET_TEXT);
		resetButton.addActionListener(e -> {
			mapper.setToDefaults();
		});

		panel.add(undoButton);
		panel.add(resetButton);

		return panel;
	}

	private void cleanUp() {
		mapper.removeListener(this);
		mapper.save();
	}

	@Override
	public void valueChanged(Enum<?> key, Object newValue) {
		if (enumToGetterSetter.containsKey(key)) {
			enumToGetterSetter.get(key).set((T) newValue);
		}
	}

	@Override
	public void valueRemoved(Enum<?> key) {
		if (enumToGetterSetter.containsKey(key)) {
			enumToGetterSetter.get(key).set(null);
		}
	}

}
