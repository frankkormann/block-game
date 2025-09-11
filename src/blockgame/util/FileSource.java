package blockgame.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

/**
 * Methods to manage reading files either from a user-defined zip file or Java
 * resources.
 * 
 * @author Frank Kormann
 */
public class FileSource {

	private static String source = null;

	/**
	 * Sets the zip to retrieve files from, or {@code null} to retrieve files
	 * from {@link Class#getResourceAsStream(String)}.
	 * 
	 * @param path file path to the archive or {@code null}
	 */
	public static void setSource(String path) {
		source = path;
	}

	/**
	 * Retrieve an {@code InputStream} to read from the file named {@code name}.
	 * 
	 * @param name name of the file to get
	 * 
	 * @return the {@code InputStream}
	 */
	public static InputStream getStream(String name) {
		if (source == null) {
			return FileSource.class.getResourceAsStream(name);
		}

		try {
			@SuppressWarnings("resource")
			ZipFile zfile = new ZipFile(source);
			if (name.startsWith("/")) {
				name = name.substring(1);
			}
			return zfile.getInputStream(zfile.getEntry(name));
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
