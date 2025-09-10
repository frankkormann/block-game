package blockgame.physics;

import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;

import blockgame.util.DrawUtils;

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

	private static final int INACTIVE_OUTLINE_THICKNESS = 2;

	private Set<MovingRectangle> rectsInside;
	private boolean isActive;

	public Area(int x, int y, int width, int height, Colors colorEnum) {
		super(x, y, width, height, colorEnum, ResizeBehavior.STAY);
		rectsInside = new HashSet<>();
		isActive = true;
	}

	@Override
	public void draw(Graphics g) {
		if (getColor().getAlpha() == 0) {
			return;
		}

		g = g.create();
		g.setColor(getColor());

		if (isActive) {
			g.fillRect(getX(), getY(), getWidth(), getHeight());
		}
		else {
			DrawUtils.drawRectOutline(g, INACTIVE_OUTLINE_THICKNESS, getX(),
					getY(), getWidth(), getHeight());
		}

		g.dispose();
	}

	/**
	 * Called when {@code rect} enters this.
	 * 
	 * @param rect {@code MovingRectangle} which entered
	 */
	public abstract void onEnter(MovingRectangle rect);

	/**
	 * Called when {@code rect} exits this.
	 * 
	 * @param rect {@code MovingRectangle} which exited
	 */
	public abstract void onExit(MovingRectangle rect);

	/**
	 * Called on every frame that {@code rect} is inside this.
	 * 
	 * @param rect {@code MovingRectangle} which is inside
	 */
	public abstract void everyFrame(MovingRectangle rect);

	/**
	 * Adds or removes {@code rect} from the list of {@code MovingRectangles} in
	 * this {@code Area}. Calls {@code onEnter} or {@code onExit} if necessary.
	 * <p>
	 * This method should be called on every frame for every
	 * {@code MovingRectangle}.
	 * 
	 * @param rect {@code MovingRectangle} to handle
	 */
	public void handle(MovingRectangle rect) {
		boolean alreadyInside = rectsInside.contains(rect);
		boolean intersects = intersectsX(rect) && intersectsY(rect) && isActive;

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

	/**
	 * Sets whether this is "active". See {@link #setActive(boolean)} for a
	 * description of what activity means.
	 * 
	 * @return {@code true} if this is "active"
	 */
	public boolean getActive() {
		return isActive;
	}

	/**
	 * Sets whether this affects {@code MovingRectangle}s or not. If a
	 * {@code MovingRectangle} is already inside this when it becomes inactive,
	 * {@code onExit} is called on it on the next frame.
	 * 
	 * @param active {@code true} if this should affect {@code MovingRectangle}s
	 */
	public void setActive(boolean active) {
		isActive = active;
	}

}
