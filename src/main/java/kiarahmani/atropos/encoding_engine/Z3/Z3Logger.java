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
	String file_path;

	public Z3Logger(String file_path) {
		this.file_path = file_path;
		this.file = new File(file_path);
		try {
			writer = new FileWriter(file, false);
			printer = new PrintWriter(writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reset() {
		this.file = new File(this.file_path);
		try {
			writer = null;
			writer = new FileWriter(file, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		printer = new PrintWriter(writer);
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
			String star = "\n;; " + String.format("%0" + s.length() + "d", 0).replace("0", "-");
			LogZ3(star + "\n;; " + s + star);
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
