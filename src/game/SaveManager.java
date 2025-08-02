package game;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Convenience class for writing and reading from a save file directory.
 * <p>
 * On Windows, the default directory is in the user's AppData folder. On other
 * operating systems, it is in their home directory.
 * {@link #setDirectory(String)} should be called before any other methods to
 * configure the directory.
 * <p>
 * All file names should be prefixed with "/".
 */
public class SaveManager {

	private static final String DIRECTORY_NAME = "/BlockGame/save";
	private static final String SAVE_FILE_NAME = "/savedata";

	private static String saveDirectory;
	private static Map<String, String> cachedValues;
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

	/**
	 * Gets a value from the save file. If there is no saved value, returns
	 * {@code defaultValue}.
	 * 
	 * @param name         which value to get
	 * @param defaultValue backup value in case there is no saved value
	 * 
	 * @return the value
	 */
	public static String getValue(String name, String defaultValue) {
		if (cachedValues == null) {
			readValuesFromSave();
		}
		return cachedValues.get(name) == null ? defaultValue
				: cachedValues.get(name);
	}

	/**
	 * Stores a value into the save data.
	 * 
	 * @param name  which value to store
	 * @param value what to store
	 */
	public static void putValue(String name, String value) {
		if (cachedValues == null) {
			readValuesFromSave();
		}
		cachedValues.put(name, value);
		saveToFile();
	}

	/**
	 * Reads values from disk into {@code cachedValues}.
	 */
	private static void readValuesFromSave() {
		InputStream stream = readFile(SAVE_FILE_NAME);
		ObjectMapper mapper = new ObjectMapper();
		try {
			cachedValues = mapper.readValue(stream,
					new TypeReference<Map<String, String>>() {});
			stream.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Can't read save data", e)
					.setVisible(true);
			return;
		}
	}

	/**
	 * Saves all values to disk.
	 */
	private static void saveToFile() {
		OutputStream outstream = writeFile(SAVE_FILE_NAME);
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(outstream, cachedValues);
			outstream.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Error", "Can't write save data", e)
					.setVisible(true);
		}
	}

}
