package blockgame.physics;

import java.util.HashSet;
import java.util.Set;

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
