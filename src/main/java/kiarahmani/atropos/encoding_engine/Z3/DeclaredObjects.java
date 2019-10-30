package kiarahmani.atropos.encoding_engine.Z3;

import java.util.HashMap;
import java.util.Map;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.DatatypeSort;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Sort;
import com.microsoft.z3.Symbol;

public class DeclaredObjects {
	private Map<String, Sort> sorts;
	private Map<String, Symbol> symbols;
	private Map<String, FuncDecl> funcs;
	private Map<String, DatatypeSort> datatypes;
	private Map<String, BoolExpr> assertions;
	private Map<String, Map<String, FuncDecl>> constructors;

	public DeclaredObjects() {
		this.sorts = new HashMap<String, Sort>();
		this.datatypes = new HashMap<String, DatatypeSort>();
		this.constructors = new HashMap<>();
		this.funcs = new HashMap<>();
		this.assertions = new HashMap<>();
	}

	public FuncDecl getfuncs(String key) {
		return funcs.get(key);
	}

	public void addAssertion(String key, BoolExpr value) {
		assertions.put(key, value);
		Z3Logger.LogZ3(value.toString());
	}

	public void addFunc(String key, FuncDecl value) {
		funcs.put(key, value);
		Z3Logger.LogZ3(value.toString());
	}

	public FuncDecl getConstructor(String type, String cnstrctrName) {
		return this.constructors.get(type).get(cnstrctrName);
	}

	public void addSort(String key, Sort value) {
		sorts.put(key, value);
		Z3Logger.LogZ3("(declare-sort " + value.toString() + ")");
	}

	public Sort getSort(String key) {
		return sorts.get(key);
	}

	public DatatypeSort getDataTypes(String key) {
		return datatypes.get(key);
	}

	public void addDataType(String key, DatatypeSort value) {
		datatypes.put(key, value);
		Z3Logger.LogZ3(key);
		String s = "";
		for (FuncDecl x : value.getConstructors()) {
			this.addConstructor(key, x.getName().toString(), x);
			s += ("	" + x + "\n");
		}
		Z3Logger.LogZ3(s);
	}

	public void addConstructor(String type, String cnstrctrName, FuncDecl cnstrctr) {
		if (this.constructors.get(type) == null)
			this.constructors.put(type, new HashMap<String, FuncDecl>());
		Map<String, FuncDecl> map = this.constructors.get(type);
		map.put(cnstrctrName, cnstrctr);
		this.constructors.put(type, map);
	}

}
