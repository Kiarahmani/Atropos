package kiarahmani.atropos.encoding_engine.Z3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.utils.Constants;

public class Z3Driver {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	Context ctx;
	Solver slv;
	File file;
	Model model;
	FileWriter writer;
	PrintWriter printer;
	DeclaredObjects objs;

	public Z3Driver() {
		logger.debug("new Z3 Driver object is being created");
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		cfg.put("unsat_core", "true");
		ctx = new Context(cfg);
		logger.debug("new Z3 context is created");
		slv = ctx.mkSolver();
		this.file = new File("z3-encoding.smt2");
		try {
			writer = new FileWriter(file, false);
			printer = new PrintWriter(writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.objs = new DeclaredObjects(printer);
		LogZ3(";; SMTLIB2 representation of the encoding");
		HeaderZ3("SORTS & DATATYPES");

		objs.addSort("TXN", ctx.mkUninterpretedSort("TXN"));
		objs.addSort("Query", ctx.mkUninterpretedSort("Query"));
		objs.addSort("Bool", ctx.mkBoolSort());
		objs.addSort("Int", ctx.mkIntSort());
		objs.addSort("String", ctx.mkStringSort());
		objs.addSort("BitVec", ctx.mkBitVecSort(Constants._MAX_BV_SIZE));
	}

	/*
	 * 
	 * 
	 * Z3 Logging Helper Functions
	 * 
	 * 
	 */
	private void LogZ3(String s) {
		if (Constants._SHOULD_WRITE_ASSERTIONS_TO_FILE) {
			printer.append(s + "\n");
			printer.flush();
		}
	}

	private void SubHeaderZ3(String s) {
		if (Constants._SHOULD_WRITE_ASSERTIONS_TO_FILE) {
			LogZ3("\n;" + s.toUpperCase());
			printer.flush();
		}
	}

	private void HeaderZ3(String s) {
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
