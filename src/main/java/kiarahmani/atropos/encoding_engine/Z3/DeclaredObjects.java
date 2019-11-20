package kiarahmani.atropos.encoding_engine.Z3;

import java.util.HashMap;
import java.util.Map;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.DatatypeSort;
import com.microsoft.z3.EnumSort;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Sort;
import com.microsoft.z3.Symbol;

public class DeclaredObjects {
	private Map<String, Sort> sorts;
	private Map<String, Symbol> symbols;
	private Map<String, FuncDecl> funcs;
	private Map<String, DatatypeSort> datatypes;
	private Map<String, Expr> constants;
	private Map<String, BoolExpr> assertions;
	private Map<String, EnumSort> enums;
	private Map<String, Map<String, FuncDecl>> constructors;

	public DeclaredObjects() {
		this.sorts = new HashMap<String, Sort>();
		this.datatypes = new HashMap<String, DatatypeSort>();
		this.constructors = new HashMap<>();
		this.funcs = new HashMap<>();
		this.assertions = new HashMap<>();

		this.symbols = new HashMap<>();
		this.enums = new HashMap<>();
	}

	public EnumSort getEnum(String key) {
		return this.enums.get(key);
	}

	public void addEnum(String key, EnumSort v) {
		assert (!this.enums.keySet().contains(key));
		Z3Logger.SubHeaderZ3("Enum: " + v);
		for (FuncDecl f : v.getConstDecls())
			Z3Logger.LogZ3(f.toString());

		this.enums.put(key, v);
	}

	public Symbol getSymbol(String k) {
		return this.symbols.get(k);
	}

	public void addSymbol(String k, Symbol s) {
		assert (!this.symbols.keySet().contains(k));
		// Z3Logger.LogZ3("Symbol: "+s);
		this.symbols.put(k, s);
	}

	public FuncDecl getfuncs(String key) {
		FuncDecl result = funcs.get(key);
		if (result == null)
			assert (false) : "function " + key + " does not exist";
		return result;
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

	public Expr getEnumConstructor(String enum_name, String cnstrctrName) {
		Expr[] all_constructors = this.enums.get(enum_name).getConsts();
		Expr result = null;
		for (Expr e : all_constructors)
			if (e.toString().contains(cnstrctrName)) {
				result = e;
				break;
			}
		assert (result != null) : "enum_name:" + enum_name + "  cnstrctrName:" + cnstrctrName;
		return result;
	}

	public void addSort(String key, Sort value) {
		sorts.put(key, value);
		Z3Logger.LogZ3("(declare-sort " + key + ": " + value.toString() + ")");
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
