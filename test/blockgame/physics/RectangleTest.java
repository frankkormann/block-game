package blockgame.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import blockgame.physics.Rectangle.AttachmentOption;

class RectangleTest {

	Rectangle rect;

	@BeforeEach
	void setUp() {
		rect = new WallRectangle(10, 5, 100, 50);
	}

	@Test
	void last_position_is_updated_when_instantiated() {
		assertEquals(rect.getX(), rect.getLastX());
		assertEquals(rect.getY(), rect.getLastY());
		assertEquals(rect.getWidth(), rect.getLastWidth());
		assertEquals(rect.getHeight(), rect.getLastHeight());
	}

	@Nested
	class Intersections {

		Rectangle other;

		@BeforeEach
		void createSecondRectangle() {
			other = new WallRectangle(rect.getX(), rect.getY(), rect.getWidth(),
					rect.getHeight());
		}

		@Test
		void does_intersect_x() {
			other.setX(other.getX() - 10);

			assertTrue(rect.intersectsX(other));
			assertTrue(other.intersectsX(rect));
		}

		@Test
		void does_not_intersect_x() {
			other.setX(other.getX() - 2 * other.getWidth());

			assertFalse(rect.intersectsX(other));
			assertFalse(other.intersectsX(rect));
		}

		@Test
		void does_intersect_y() {
			other.setY(other.getY() - 10);

			assertTrue(rect.intersectsY(other));
			assertTrue(other.intersectsY(rect));
		}

		@Test
		void does_not_intersect_y() {
			other.setY(other.getY() - 2 * other.getHeight());

			assertFalse(rect.intersectsY(other));
			assertFalse(other.intersectsY(rect));
		}

		@Test
		void completely_overlapping_does_intersect_both() {
			assertTrue(rect.intersectsX(other));
			assertTrue(other.intersectsX(rect));

			assertTrue(rect.intersectsY(other));
			assertTrue(other.intersectsY(rect));
		}
	}

	@Nested
	class WithAttachedArea {

		Area attachedArea;

		@BeforeEach
		void addAttachment() {
			attachedArea = new AntigravityArea(0, 0, 0, 0);
			rect.addAttachment(attachedArea, AttachmentOption.GLUED_NORTH);
		}

		@Test
		void area_moves_as_rect_moves() {
			rect.setX(200);

			assertEquals(200, attachedArea.getX());
		}

		Area addNewAttachment(AttachmentOption... attachmentOptions) {
			Area newAttachment = new AntigravityArea(0, 0, 20, 20);
			rect.addAttachment(newAttachment, attachmentOptions);
			return newAttachment;
		}

		@Test
		void width_is_updated_with_same_width_option() {
			Area newAttachment = addNewAttachment(AttachmentOption.SAME_WIDTH);
			rect.setWidth(700);

			assertEquals(rect.getWidth(), newAttachment.getWidth());
		}

		@Test
		void height_is_updated_with_same_height_option() {
			Area newAttachment = addNewAttachment(AttachmentOption.SAME_HEIGHT);
			rect.setHeight(700);

			assertEquals(rect.getHeight(), newAttachment.getHeight());
		}

		@Test
		void glued_north_option_puts_area_above() {
			Area newAttachment = addNewAttachment(AttachmentOption.GLUED_NORTH);

			assertEquals(rect.getY(),
					newAttachment.getY() + newAttachment.getHeight());
		}

		@Test
		void glued_south_option_puts_area_below() {
			Area newAttachment = addNewAttachment(AttachmentOption.GLUED_SOUTH);

			assertEquals(rect.getY() + rect.getHeight(), newAttachment.getY());
		}

		@Test
		void glue_west_option_puts_area_to_the_left() {
			Area newAttachment = addNewAttachment(AttachmentOption.GLUED_WEST);

			assertEquals(rect.getX(),
					newAttachment.getX() + newAttachment.getWidth());
		}

		@Test
		void glue_east_option_puts_area_to_the_right() {
			Area newAttachment = addNewAttachment(AttachmentOption.GLUED_EAST);

			assertEquals(rect.getX() + rect.getWidth(), newAttachment.getX());
		}
	}
}
