package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import game.Rectangle.AttachmentOption;

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

	@Test
	void adding_to_left_changes_leftWidthChange() {
		rect.changeWidth(10, true);

		assertEquals(10, rect.getLeftWidthChange());
	}

	@Test
	void adding_to_right_does_not_change_leftWidthChange() {
		rect.changeWidth(10, false);

		assertEquals(0, rect.getLeftWidthChange());
	}

	@Test
	void adding_to_top_changes_topHeightChange() {
		rect.changeHeight(10, true);

		assertEquals(10, rect.getTopHeightChange());
	}

	@Test
	void adding_to_bottom_does_not_change_topHeightChange() {
		rect.changeWidth(10, false);

		assertEquals(0, rect.getTopHeightChange());
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

		void updateLastPositions() {
			rect.updateLastPosition();
			other.updateLastPosition();
		}

		@Test
		void used_to_intersect_x() {
			other.setX(other.getX() - 10);
			updateLastPositions();

			assertTrue(rect.usedToIntersectX(other));
			assertTrue(other.usedToIntersectX(rect));
		}

		@Test
		void used_to_not_intersect_x() {
			other.setX(other.getX() - 2 * other.getWidth());
			updateLastPositions();

			assertFalse(rect.usedToIntersectX(other));
			assertFalse(other.usedToIntersectX(rect));
		}

		@Test
		void used_to_intersect_y() {
			other.setY(other.getY() - 10);
			updateLastPositions();

			assertTrue(rect.usedToIntersectY(other));
			assertTrue(other.usedToIntersectY(rect));
		}

		@Test
		void used_to_not_intersect_y() {
			other.setY(other.getY() - 2 * other.getHeight());
			updateLastPositions();

			assertFalse(rect.usedToIntersectY(other));
			assertFalse(other.usedToIntersectY(rect));
		}

		@Test
		void completely_overlapping_used_to_intersect_both() {
			updateLastPositions();

			assertTrue(rect.usedToIntersectX(other));
			assertTrue(other.usedToIntersectX(rect));

			assertTrue(rect.usedToIntersectY(other));
			assertTrue(other.usedToIntersectY(rect));
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

			assertEquals(rect.getY(), newAttachment.getY() + newAttachment.getHeight());
		}

		@Test
		void glued_south_option_puts_area_below() {
			Area newAttachment = addNewAttachment(AttachmentOption.GLUED_SOUTH);

			assertEquals(rect.getY() + rect.getHeight(), newAttachment.getY());
		}

		@Test
		void glue_west_option_puts_area_to_the_left() {
			Area newAttachment = addNewAttachment(AttachmentOption.GLUED_WEST);

			assertEquals(rect.getX(), newAttachment.getX() + newAttachment.getWidth());
		}

		@Test
		void glue_east_option_puts_area_to_the_right() {
			Area newAttachment = addNewAttachment(AttachmentOption.GLUED_EAST);

			assertEquals(rect.getX() + rect.getWidth(), newAttachment.getX());
		}

		@Test
		void attached_areas_have_last_position_updated_too() {
			int previousX = attachedArea.getX();
			rect.updateLastPosition();
			rect.setX(200);

			assertEquals(previousX, attachedArea.getLastX());
		}
	}
}
