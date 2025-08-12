package blockgame.physics;

import java.util.HashSet;
import java.util.Set;

/**
 * Controls the activity of {@code SwitchRectangle}s based on
 * {@code SwitchArea}s. As long as any of the {@code SwitchArea}s that point to
 * this have a {@code MovingRectangle} within, all of this's
 * {@code SwitchRectangle}s will be active.
 * 
 * @author Frank Kormann
 */
public class SwitchController {

	private Set<SwitchRectangle> rects;
	private int activatedAreas;

	public SwitchController() {
		rects = new HashSet<>();
		activatedAreas = 0;
	}

	public void areaActivated() {
		activatedAreas++;
		if (activatedAreas == 1) {
			rects.forEach(r -> r.setActive(true));
		}
	}

	public void areaDeactivated() {
		activatedAreas--;
		if (activatedAreas == 0) {
			rects.forEach(r -> r.setActive(false));
		}
	}

	public void addSwitchRectangle(SwitchRectangle rect) {
		rects.add(rect);
	}

}
