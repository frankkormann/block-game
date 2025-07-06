package game;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@code Rectangle} that manipulates {@code MovingRectangles} within its
 * bounds. {@code Areas} have no collision. Default {@code ResizeBehavior} is
 * {@code STAY}.
 * <p>
 * Each {@code Area} interacts with the {@code MovingRectangles} within it using
 * the {@link#onEnter}, {@link#onExit}, and {@link#everyFrame} methods.
 * <ul>
 * <li>{@code onEnter} is called when a {@code MovingRectangle} enters this.
 * <li>{@code onExit} is called when a {@code MovingRectangle} leaves this.
 * <li>{@code everyFrame} is called for each {@code MovingRectangle} within this
 * on every frame, including the frame the {@code MovingRectangle} enters.
 * </ul>
 * <p>
 * {@link#handle(MovingRectangle)} should be called every frame for every
 * {@code MovingRectangle}.
 *
 * @author Frank Kormann
 */
public abstract class Area extends Rectangle {

	private Set<MovingRectangle> rectsInside;

	public Area() {
		this(0, 0, 0, 0, Color.BLACK);
	}

	public Area(int x, int y, int width, int height, Color color) {
		super(x, y, width, height, color, Rectangle.ResizeBehavior.STAY);
		rectsInside = new HashSet<>();
	}

	@Override
	public void draw(Graphics g) {
		g = g.create();
		g.setColor(getColor());
		g.fillRect(getX(), getY(), getWidth(), getHeight());
	}

	public abstract void onEnter(MovingRectangle rect);

	public abstract void onExit(MovingRectangle rect);

	public abstract void everyFrame(MovingRectangle rect);

	/**
	 * Adds or removes {@code rect} from the list of {@code Rectangles} in this
	 * {@code Area}. Calls {@code onEnter} or {@code onExit} if necessary.
	 * 
	 * This method should be called on every frame for every {@code Rectangle}.
	 * 
	 * @param rect {@code Rectangle} to handle
	 */
	public void handle(MovingRectangle rect) {
		boolean alreadyInside = rectsInside.contains(rect);
		// Apply Areas based on positions at the start of the frame
		boolean intersects = intersectsX(rect) && intersectsY(rect);

		if (alreadyInside && !intersects) {
			rectsInside.remove(rect);
			onExit(rect);
		}
		else if (!alreadyInside && intersects) {
			rectsInside.add(rect);
			onEnter(rect);
		}

		if (rectsInside.contains(rect)) {
			everyFrame(rect);
		}
	}

}
