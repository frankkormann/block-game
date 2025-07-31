package game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;

/**
 * Convenience class for writing and reading from a save file directory.
 * <p>
 * On Windows, the default directory is in the user's AppData folder. On other
 * operating systems, it is in their home directory.
 * {@link #setDirectory(String)} should be called before any other methods to
 * configure the directory.
 */
public class SaveManager {

	private static final String DIRECTORY_NAME = "/BlockGame/save";
	private static final String CURRENT_LEVEL_FILE = "/current_level";

	private static String saveDirectory;
	static {
		if (System.getProperty("os.name").startsWith("Windows")) {
			saveDirectory = System.getenv("APPDATA") + DIRECTORY_NAME;
		}
		else {
			saveDirectory = System.getProperty("user.home") + DIRECTORY_NAME;
		}
	}

	/**
	 * Sets the directory path to read/write files from. Makes no change if
	 * {@code path} is {@code null}.
	 * 
	 * @param path {@code String} representation of the directory path
	 */
	public static void setDirectory(String path) {
		if (path == null) {
			return;
		}
		saveDirectory = path + DIRECTORY_NAME;
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
		if (saveDirectory == null) {
			System.err.println("Call SaveManager.setUp() first");
			return null;
		}

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
		if (saveDirectory == null) {
			System.err.println("Call SaveManager.setUp() first");
			return null;
		}

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

	/**
	 * Gets the most recently saved level resource.
	 * 
	 * @param defaultValue resource to use if there is no saved level or it is
	 *                     not accessible
	 * 
	 * @return the most recently saved level
	 * 
	 * @see #setCurrentLevel(String)
	 */
	public static String getCurrentLevel(String defaultValue) {
		InputStream stream = readFile(CURRENT_LEVEL_FILE);
		if (stream == null) {
			return defaultValue;
		}
		else {

			String returnValue;
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(stream));
				returnValue = reader.readLine();
				reader.close();
				return returnValue;
			}
			catch (IOException e) {
				e.printStackTrace();
				new ErrorDialog("Error", "Can't read save data", e)
						.setVisible(true);
				return defaultValue;
			}
		}
	}

	/**
	 * Saves {@code currentLevel} to disk.
	 * 
	 * @param currentLevel level resource to save
	 * 
	 * @see #getCurrentLevel(String)
	 */
	public static void setCurrentLevel(String currentLevel) {
		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(writeFile(CURRENT_LEVEL_FILE)));
		try {
			writer.write(currentLevel);
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Can't write save data", e)
					.setVisible(true);
		}
		try {
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
