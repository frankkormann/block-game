package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.nio.file.Path;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import game.Rectangle.Colors;
import mocks.IntMapper;
import mocks.ValueChangerPanelMock;

//These tests will skip if in a headless environment (an environment that doesn't
//support keyboard, display, etc.)
public class ValueChangerPanelTest {

	ValueChangerPanelMock panel;
	Mapper<Integer> mapper;

	@BeforeEach
	void setUp(@TempDir Path dir) {
		assumeFalse(GraphicsEnvironment.isHeadless());
		SaveManager.setDirectory(dir.toString());

		mapper = new IntMapper("/mappings", "/colors_default.json");
		JFrame parent = new JFrame();
		panel = new ValueChangerPanelMock(parent.getRootPane(), mapper);
		parent.add(panel);
	}

	@Test
	void each_GetterSetter_returns_what_is_in_Mapper() {
		for (Colors key : Colors.values()) {
			assertEquals(mapper.get(key), panel.getterSetters.get(key).get());
		}
	}

	@Test
	void setting_a_GetterSetter_changes_mapping_in_Mapper() {
		panel.getterSetters.get(Colors.BLACK).set(60);

		assertEquals(60, (int) mapper.get(Colors.BLACK));
	}

	@Test
	void changing_a_value_in_Mapper_updates_the_GetterSetter() {
		mapper.set(Colors.RED, 45);

		assertEquals(45, panel.getterSetters.get(Colors.RED).get());
	}

	@Test
	void closing_the_window_saves_mappings() {
		panel.getterSetters.get(Colors.ORANGE).set(18);
		Window window = SwingUtilities.getWindowAncestor(panel);
		window.dispatchEvent(
				new WindowEvent(window, WindowEvent.WINDOW_CLOSING));

		IntMapper newMapper = new IntMapper("/mappings",
				"/colors_default.json");
		assertEquals(18, newMapper.get(Colors.ORANGE));
	}

	@Nested
	class UndoResetButtons {

		JButton undoButton;
		JButton resetButton;

		@BeforeEach
		void findButtons() {
			undoButton = (JButton) ((JPanel) panel.getComponent(1))
					.getComponent(0);
			resetButton = (JButton) ((JPanel) panel.getComponent(1))
					.getComponent(1);
		}

		@Test
		void undo_button_undoes_unsaved_changes() {
			panel.getterSetters.get(Colors.BLACK).set(67);
			mapper.save();
			int originalGreen = mapper.get(Colors.GREEN);
			panel.getterSetters.get(Colors.GREEN).set(432);
			undoButton.doClick();

			assertEquals(67, mapper.get(Colors.BLACK));
			assertEquals(originalGreen, mapper.get(Colors.GREEN));
		}

		@Test
		void reset_button_resets_to_defaults() {
			panel.getterSetters.get(Colors.BLACK).set(67);
			mapper.save();
			panel.getterSetters.get(Colors.GREEN).set(432);
			resetButton.doClick();

			assertEquals(-16777216, mapper.get(Colors.BLACK));
			assertEquals(-10159516, mapper.get(Colors.GREEN));
		}
	}
}
