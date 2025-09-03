package blockgame.physics;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Disables a {@code MovingRectangle}'s gravity on enter and re-enables it on
 * exit.
 * 
 * @author Frank Kormann
 */
public class AntigravityArea extends Area {

	private static Map<MovingRectangle, Integer> areaCounter = new HashMap<>();

	@JsonCreator
	public AntigravityArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height) {
		super(x, y, width, height, Colors.TRANSLUCENT_PINK);
	}

	/**
	 * Removes {@code rect}'s gravity.
	 * 
	 * @param rect {@code MovingRectangle} to affect
	 */
	@Override
	public void onEnter(MovingRectangle rect) {
		if (!areaCounter.containsKey(rect)) {
			areaCounter.put(rect, 0);
		}
		areaCounter.put(rect, areaCounter.get(rect) + 1);
		rect.setHasGravity(false);
	}

	/**
	 * Gives {@code rect} gravity.
	 * 
	 * @param rect {@code MovingRectangle} to affect
	 */
	@Override
	public void onExit(MovingRectangle rect) {
		if (!areaCounter.containsKey(rect)) {
			System.err.println(
					"In AntigravityArea#onExit: rect not in areaCounter");
			return;
		}
		areaCounter.put(rect, areaCounter.get(rect) - 1);
		if (areaCounter.get(rect) <= 0) {
			areaCounter.remove(rect);
			rect.setHasGravity(true);
		}
	}

	/**
	 * Not implemented.
	 * 
	 * @param rect unused
	 */
	@Override
	public void everyFrame(MovingRectangle rect) {}

}
