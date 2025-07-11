package game;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;

/**
 * {@code Rectangle} that manipulates {@code MovingRectangle}s within its
 * bounds. {@code Areas} have no collision. Default {@code ResizeBehavior} is
 * {@code STAY}.
 * <p>
 * {@link#handle(MovingRectangle)} should be called every frame for every
 * {@code MovingRectangle}.
 * <p>
 * Subclasses of {@code Area} should override
 * {@link#onEnter(MovingRectangle)}, {@link#onExit(MovingRectangle)}, and
 * {@link#everyFrame(MovingRectangle)}.
 *
 * @author Frank Kormann
 */
public abstract class Area extends Rectangle {

	private Set<MovingRectangle> rectsInside;

	public Area() {
		this(0, 0, 0, 0, Color.BLACK);
	}

	public Area(int x, int y, int width, int height, Color color) {
		super(x, y, width, height, color, ResizeBehavior.STAY);
		rectsInside = new HashSet<>();
	}

	@Override
	public void draw(Graphics g) {
		g = g.create();
		g.setColor(getColor());
		g.fillRect(getX(), getY(), getWidth(), getHeight());

		g.dispose();
	}

	/**
	 * Called when {@code rect} enters this.
	 * 
	 * @param rect {@code MovingRectangle} which entered
	 */
	protected abstract void onEnter(MovingRectangle rect);

	/**
	 * Called when {@code rect} exits this.
	 * 
	 * @param rect {@code MovingRectangle} which exited
	 */
	protected abstract void onExit(MovingRectangle rect);

	/**
	 * Called on every frame that {@code rect} is inside this.
	 * 
	 * @param rect {@code MovingRectangle} which is inside
	 */
	protected abstract void everyFrame(MovingRectangle rect);

	/**
	 * Adds or removes {@code rect} from the list of {@code Rectangles} in this
	 * {@code Area}. Calls {@code onEnter} or {@code onExit} if necessary.
	 * <p>
	 * This method should be called on every frame for every {@code Rectangle}.
	 * 
	 * @param rect {@code MovingRectangle} to handle
	 */
	public void handle(MovingRectangle rect) {
		boolean alreadyInside = rectsInside.contains(rect);
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
