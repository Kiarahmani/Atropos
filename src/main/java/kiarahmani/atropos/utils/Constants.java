package kiarahmani.atropos.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Constants {
	public static int _MAX_CYCLE_LENGTH;
	public static boolean _SHOULD_WRITE_ASSERTIONS_TO_FILE;
	public static int _MAX_BV_SIZE;
	public static int _MAX_EXECECUTION_LENGTH;
	public static int _MAX_PARTITION_NUMBER;
	public static int _MAX_TXN_INSTANCES;
	public static int _MAX_FIELD_INT;
	public static int _MAX_FIELD_STRING;

	public Constants() throws IOException {
		Properties prop = new Properties();
		InputStream input = new FileInputStream("src/main/java/kiarahmani/atropos/config.properties");
		prop.load(input);
		Constants._MAX_FIELD_INT = Integer.parseInt(prop.getProperty("_MAX_FIELD_INT"));
		Constants._MAX_FIELD_STRING = Integer.parseInt(prop.getProperty("_MAX_FIELD_STRING"));
		Constants._MAX_CYCLE_LENGTH = Integer.parseInt(prop.getProperty("_MAX_CYCLE_LENGTH"));
		Constants._MAX_TXN_INSTANCES = Integer.parseInt(prop.getProperty("_MAX_TXN_INSTANCES"));
		Constants._MAX_PARTITION_NUMBER = Integer.parseInt(prop.getProperty("_MAX_PARTITION_NUMBER"));
		Constants._MAX_EXECECUTION_LENGTH = Integer.parseInt(prop.getProperty("_MAX_EXECECUTION_LENGTH"));
		Constants._MAX_BV_SIZE = Integer.parseInt(prop.getProperty("_MAX_BV_SIZE"));
		Constants._SHOULD_WRITE_ASSERTIONS_TO_FILE = Boolean
				.parseBoolean(prop.getProperty("_SHOULD_WRITE_ASSERTIONS_TO_FILE"));

	}
}
