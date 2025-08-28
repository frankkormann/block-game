package blockgame.physics;

import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code Area} that reveals another {@code Area} when it is entered, by calling
 * the {@code Consumer} set in {@link #setRevealAction(Consumer)}. If a
 * {@code Consumer} is not set, this does nothing.
 * 
 * @author Frank Kormann
 */
public class RevealingArea extends Area {

	private Area area;
	private Consumer<Area> howToReveal;
	private boolean hasRevealed;

	@JsonCreator
	public RevealingArea(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("width") int width,
			@JsonProperty("height") int height,
			@JsonProperty("area") Area toReveal) {
		super(x, y, width, height, Colors.TRANSPARENT);
		area = toReveal;
		howToReveal = a -> {};
		hasRevealed = false;
	}

	/**
	 * Reveals the {@code Area} under this by calling the {@code Consumer} set
	 * in {@link #setRevealAction(Consumer)}.
	 * 
	 * @param rect {@code MovingRectangle} which entered
	 */
	@Override
	public void onEnter(MovingRectangle rect) {
		if (!hasRevealed) {
			howToReveal.accept(area);
			hasRevealed = true;
		}
	}

	/**
	 * Not implemented
	 * 
	 * @param rect unused
	 */
	@Override
	public void onExit(MovingRectangle rect) {}

	/**
	 * Not implemented
	 * 
	 * @param rect unused
	 */
	@Override
	public void everyFrame(MovingRectangle rect) {}

	/**
	 * Sets the {@code Consumer} to be ran when the {@code Area} under this
	 * needs to be revealed. The {@code Consumer} needs to take all necessary
	 * actions so that the {@code Area} is shown to user and interacts with
	 * other game objects.
	 * 
	 * @param consumer {@code Consumer} which reveals the {@code Area} under
	 *                 this
	 */
	public void setRevealAction(Consumer<Area> consumer) {
		howToReveal = consumer;
	}

}
