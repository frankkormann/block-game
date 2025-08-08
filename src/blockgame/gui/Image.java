package blockgame.gui;

import java.awt.Graphics;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code Drawable} which draws an image from a resource.
 * 
 * @author Frank Kormann
 */
public class Image implements Drawable {

	private int x, y;
	private java.awt.Image image;

	@JsonCreator
	public Image(@JsonProperty("x") int x, @JsonProperty("y") int y,
			@JsonProperty("source") String source) {
		this.x = x;
		this.y = y;
		try {
			image = ImageIO.read(Image.class.getResourceAsStream(source));
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Failed to read image '" + source + "'", e)
					.setVisible(true);
		}
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(image, x, y, null);
	}

}
