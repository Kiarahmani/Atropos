package kiarahmani.atropos.encoding_engine.Z3;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.DatatypeSort;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Sort;
import com.microsoft.z3.Symbol;

import kiarahmani.atropos.utils.Constants;

public class DeclaredObjects {
	private Map<String, Sort> sorts;
	private Map<String, Symbol> symbols;
	private Map<String, FuncDecl> funcs;
	private Map<String, DatatypeSort> datatypes;
	private Map<String, BoolExpr> assertions;
	private Map<String, Map<String, FuncDecl>> constructors;
	PrintWriter printer;

	public DeclaredObjects(PrintWriter printer) {
		this.printer = printer;
		this.sorts = new HashMap<String, Sort>();
	}

	public void addSort(String key, Sort value) {
		sorts.put(key, value);
		LogZ3("(declare-sort " + value.toString() + ")");
	}

	private void LogZ3(String s) {
		if (Constants._SHOULD_WRITE_ASSERTIONS_TO_FILE) {
			printer.append(s + "\n");
			printer.flush();
		}
	}

}
