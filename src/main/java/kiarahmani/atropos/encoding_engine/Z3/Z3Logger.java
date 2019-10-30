package kiarahmani.atropos.encoding_engine.Z3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import kiarahmani.atropos.utils.Constants;

public class Z3Logger {
	static PrintWriter printer;
	File file;
	FileWriter writer;

	public Z3Logger(String file_path) {
		this.file = new File(file_path);
		try {
			writer = new FileWriter(file, false);
			printer = new PrintWriter(writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 
	 * 
	 * Z3 Logging Helper Functions
	 * 
	 * 
	 */
	public static void LogZ3(String s) {
		if (Constants._SHOULD_WRITE_ASSERTIONS_TO_FILE) {
			printer.append(s + "\n");
			printer.flush();
		}
	}

	public static void SubHeaderZ3(String s) {
		if (Constants._SHOULD_WRITE_ASSERTIONS_TO_FILE) {
			LogZ3("\n;" + s.toUpperCase());
			printer.flush();
		}
	}

	public static void HeaderZ3(String s) {
		if (Constants._SHOULD_WRITE_ASSERTIONS_TO_FILE) {
			int line_length = 110;
			int white_space_length = (line_length - s.length()) / 2;
			String line = ";" + String.format("%0" + line_length + "d", 0).replace("0", "-");
			String white_space = String.format("%0" + white_space_length + "d", 0).replace("0", " ");
			LogZ3("\n" + line);
			LogZ3(";" + white_space + s);
			LogZ3(line);
			printer.flush();
		}
	}
}
