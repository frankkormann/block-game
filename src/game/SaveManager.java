package game;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * Convenience class for writing and reading from a save file directory. On
 * Windows, this directory is in the user's AppData folder. On other operating
 * systems, it is in their home directory. {@link #setUp()} should be called
 * before any other methods to configure the directory.
 */
public class SaveManager {

	private static final String DIRECTORY_NAME = "/BlockGame/save";

	private static String saveDirectory;

	/**
	 * Sets the internal directory path to read/write files from.
	 */
	public static void setUp() {
		if (System.getProperty("os.name").startsWith("Windows")) {
			saveDirectory = System.getenv("APPDATA") + DIRECTORY_NAME;
		}
		else {
			saveDirectory = System.getProperty("user.home") + DIRECTORY_NAME;
		}
	}

	/**
	 * Returns an {@code InputStream} to read from the file named {@code name}.
	 * Returns {@code null} if the file does not exist or an I/O error occurs.
	 * 
	 * @param name which file to open
	 * 
	 * @return {@code InputStream} to read from
	 */
	public static InputStream readFile(String name) {
		File file = new File(saveDirectory + name);
		if (!file.exists()) {
			return null;
		}
		try {
			return Files
					.newInputStream(new File(saveDirectory + name).toPath());
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Can't open save file " + name, e)
					.setVisible(true);
			return null;
		}
	}

	/**
	 * Returns an {@code OutputStream} to write to the file named {@code name}.
	 * Returns {@code null} if an I/O error occurs.
	 * 
	 * @param name which file to open
	 * 
	 * @return {@code OutputStream} to write to
	 */
	public static OutputStream writeFile(String name) {
		File file = new File(saveDirectory + name);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		try {
			return Files.newOutputStream(file.toPath());
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Can't write to save file " + name, e)
					.setVisible(true);
			return null;
		}
	}

}
