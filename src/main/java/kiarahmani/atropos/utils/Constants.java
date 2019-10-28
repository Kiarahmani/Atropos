package kiarahmani.atropos.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Constants {
	public static int _MAX_CYCLE_LENGTH;
	public static boolean _SHOULD_WRITE_ASSERTIONS_TO_FILE;
	public static int _MAX_BV_SIZE;

	public Constants() throws IOException {
		Properties prop = new Properties();
		InputStream input = new FileInputStream("src/main/java/kiarahmani/atropos/config.properties");
		prop.load(input);
		Constants._MAX_CYCLE_LENGTH = Integer.parseInt(prop.getProperty("_MAX_CYCLE_LENGTH"));
		Constants._MAX_BV_SIZE = Integer.parseInt(prop.getProperty("_MAX_BV_SIZE"));
		Constants._SHOULD_WRITE_ASSERTIONS_TO_FILE = Boolean
				.parseBoolean(prop.getProperty("_SHOULD_WRITE_ASSERTIONS_TO_FILE"));

	}
}
