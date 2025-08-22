package blockgame.gui;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code Drawable} that animates an exploding cloud of small square particles.
 * 
 * @author Frank Kormann
 */
public class ParticleExplosion implements Drawable {

	private List<Particle> particles;

	public ParticleExplosion() {
		particles = new ArrayList<>();
	}

	@Override
	public void draw(Graphics g) {
		particles.forEach(p -> p.draw(g));
	}

	/**
	 * Starts the explosion, centered at {@code (centerX, centerY)}. Each
	 * particle receives a random velocity.
	 * 
	 * @param count        number of particles
	 * @param size         side length of square particles
	 * @param centerX      location of center of cloud
	 * @param centerY      location of center of cloud
	 * @param xVelocityMin minimum x velocity to randomly assign
	 * @param xVelocityMax maximum x velocity to randomly assign
	 * @param yVelocityMin minimum y velocity to randomly assign
	 * @param yVelocityMax maximum y velocity to randomly assign
	 */
	public void start(int count, int size, int centerX, int centerY,
			int xVelocityMin, int xVelocityMax, int yVelocityMin,
			int yVelocityMax) {
		for (int i = 0; i < count; i++) {
			Particle p = new Particle(centerX, centerY, size);
			p.randomizeXVelocity(xVelocityMin, xVelocityMax);
			p.randomizeYVelocity(yVelocityMin, yVelocityMax);
			particles.add(p);
		}
	}

	/**
	 * Computes the location of each particle for the next frame in the
	 * animation.
	 */
	public void nextFrame() {
		particles.forEach(p -> p.nextFrame());
	}

	/**
	 * Stops the animation and deletes all particles.
	 */
	public void stop() {
		particles.clear();
	}

	/**
	 * Small square which has a random x and y velocity.
	 */
	private class Particle implements Drawable {

		// Use float position and velocity for a greater variety of random
		// values
		private float x, y;
		private int width, height;
		private float xVelocity, yVelocity;

		public Particle(int x, int y, int size) {
			this.x = x;
			this.y = y;
			width = height = size;
			xVelocity = yVelocity = 0;
		}

		@Override
		public void draw(Graphics g) {
			g.fillRect((int) x, (int) y, width, height);
		}

		public void nextFrame() {
			x += xVelocity;
			y += yVelocity;
		}

		public void randomizeXVelocity(int min, int max) {
			xVelocity = min + ((float) Math.random() * (max - min + 1));
		}

		public void randomizeYVelocity(int min, int max) {
			yVelocity = min + ((float) Math.random() * (max - min + 1));
		}

	}

}
