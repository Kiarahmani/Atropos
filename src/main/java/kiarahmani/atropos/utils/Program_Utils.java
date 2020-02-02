package kiarahmani.atropos.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DDL.vc.VC;
import kiarahmani.atropos.DDL.vc.VC.VC_Agg;
import kiarahmani.atropos.DDL.vc.VC.VC_Type;
import kiarahmani.atropos.DDL.vc.VC_Constraint;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_Arg;
import kiarahmani.atropos.DML.expression.E_BinOp;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.E_Size;
import kiarahmani.atropos.DML.expression.E_UnOp;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.E_UnOp.UnOp;
import kiarahmani.atropos.DML.expression.constants.E_Const_Bool;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.query.Delete_Query;
import kiarahmani.atropos.DML.query.Insert_Query;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.program.Block;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.program.Block.BlockType;
import kiarahmani.atropos.program.statements.If_Statement;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.refactoring_engine.Refactoring_Engine;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTO.Query_ReAtomicizer;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTO.SELECT_Redirector;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.SELECT_Splitter;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.UPDATE_Duplicator;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.UPDATE_Splitter;
import kiarahmani.atropos.refactoring_engine.Modifiers.TTO.SELECT_Merger;
import kiarahmani.atropos.refactoring_engine.Modifiers.TTO.UPDATE_Merger;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;

public class Program_Utils {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	// basic program meta data
	private String program_name;
	private int version;
	private boolean lock;
	/*
	 * string to object mappings
	 */
	// schema
	private HashMap<String, TableName> tableNameMap;
	private HashMap<String, Table> tableMap;
	private HashMap<String, FieldName> fieldNameMap;
	// program
	private HashMap<String, Transaction> trasnsactionMap;
	private HashMap<String, E_Arg> argsMap;
	private HashMap<String, Variable> variableMap;
	private HashMap<String, If_Statement> ifStatementMap;
	/*
	 * Value Corresponce
	 */
	private HashMap<String, VC> vcMap;

	/*
	 * Variables to Proj Expressions Mapping
	 */
	private HashMap<Variable, HashSet<E_Proj>> variableToExpressionSetMap;

	// Transaction to Args Mapping
	private HashMap<String, ArrayList<E_Arg>> transactionToArgsSetMap;

	// Transaction to Variables Mapping
	private HashMap<String, ArrayList<Variable>> transactionToVariableSetMap;

	// Meta data mapping transaction instances the number of components
	private HashMap<String, Integer> transactionToSelectCount;
	private HashMap<String, Integer> transactionToUpdateCount;
	private HashMap<String, Integer> transactionToStatement;
	private HashMap<String, Integer> transactionToIf;
	private HashMap<String, Integer> transactionToPoCnt;

	// Program Refactoring objects
	Refactoring_Engine re;
	SELECT_Redirector select_red;
	SELECT_Splitter select_splt;
	UPDATE_Splitter upd_splt;
	UPDATE_Merger upd_merger;
	SELECT_Merger select_merger;
	UPDATE_Duplicator upd_dup;
	Query_ReAtomicizer qry_atom;

	public void lock() {
		this.lock = true;
	}

	/*
	 * 
	 * 
	 * REFACTORING METHODS
	 * 
	 * 
	 */

	public boolean refactor(Delta delta) {
		re.refactor(this, delta);
		return true;
	}

	public boolean redirect_select(String txn_name, String src_table, String dest_table, int qry_po) {
		this.select_red.set(this, txn_name, src_table, dest_table);
		if (select_red.isValid(getQueryByPo(txn_name, qry_po))) {
			re.applyAndPropagate(this, select_red, qry_po, txn_name);
			return true;
		} else
			return false;
	}

	public boolean merge_select(String txn_name, int qry_po) {
		select_merger.set(this, txn_name);
		if (select_merger.isValid(getQueryByPo(txn_name, qry_po), getQueryByPo(txn_name, qry_po + 1))) {
			re.applyAndPropagate(this, select_merger, qry_po, txn_name);
			return true;
		} else
			return false;
	}

	public boolean split_select(String txn_name, ArrayList<FieldName> excluded_fns, int qry_po) {
		select_splt.set(this, txn_name, excluded_fns);
		if (select_splt.isValid(getQueryByPo(txn_name, qry_po))) {
			re.applyAndPropagate(this, select_splt, qry_po, txn_name);
			return true;
		} else
			return false;

	}

	public boolean merge_update(String txn_name, int qry_po) {
		upd_merger.set(this, txn_name);
		if (upd_merger.isValid(getQueryByPo(txn_name, qry_po), getQueryByPo(txn_name, qry_po + 1))) {
			re.applyAndPropagate(this, upd_merger, qry_po, txn_name);
			return true;
		} else
			return false;

	}

	public boolean split_update(String txn_name, ArrayList<FieldName> excluded_fns_upd, int qry_po) {
		upd_splt.set(this, txn_name, excluded_fns_upd);
		if (upd_splt.isValid(getQueryByPo(txn_name, qry_po))) {
			re.applyAndPropagate(this, upd_splt, qry_po, txn_name);
			return true;
		} else
			return false;

	}

	public boolean duplicate_update(String txn_name, String source_table, String target_table, int qry_po) {
		upd_dup.set(this, txn_name, source_table, target_table);
		if (upd_dup.isValid(getQueryByPo(txn_name, qry_po))) {
			re.applyAndPropagate(this, upd_dup, qry_po, txn_name);
			return true;
		} else
			return false;
	}

	public int getNewSelectId(String txnName) {
		int result = this.transactionToSelectCount.get(txnName);
		this.transactionToSelectCount.put(txnName, result + 1);
		return result;
	}

	public int getNewUpdateId(String txnName) {
		int result = this.transactionToUpdateCount.get(txnName);
		this.transactionToUpdateCount.put(txnName, result + 1);
		return result;
	}

	/*
	 * Generate and return a program from the meta-data. Note that a program object
	 * is simply a packaging for currently stored meta-data
	 */
	public Program generateProgram() {
		Program program = new Program(program_name, version++);
		for (Table t : tableMap.values())
			program.addTable(t);
		for (Transaction t : getTrasnsactionMap().values())
			program.addTransaction(t);
		for (VC vc : vcMap.values())
			program.addVC(vc);
		program.setMaxQueryCount();
		return program;
	}

	/*
	 * Constructor
	 */
	public Program_Utils(String pn) {
		// allocate new objects for all data structures
		this.program_name = pn;
		this.vcMap = new HashMap<>();
		tableMap = new HashMap<>();
		tableNameMap = new HashMap<>();
		fieldNameMap = new HashMap<>();
		argsMap = new HashMap<>();
		trasnsactionMap = new HashMap<>();
		transactionToVariableSetMap = new HashMap<>();
		transactionToArgsSetMap = new HashMap<>();
		transactionToSelectCount = new HashMap<>();
		transactionToStatement = new HashMap<>();
		transactionToUpdateCount = new HashMap<>();
		transactionToIf = new HashMap<>();
		ifStatementMap = new HashMap<>();
		transactionToPoCnt = new HashMap<>();
		variableMap = new HashMap<>();
		variableToExpressionSetMap = new HashMap<>();
		re = new Refactoring_Engine();
		select_red = new SELECT_Redirector();
		select_splt = new SELECT_Splitter();
		upd_splt = new UPDATE_Splitter();
		upd_merger = new UPDATE_Merger();
		select_merger = new SELECT_Merger();
		upd_dup = new UPDATE_Duplicator();
		qry_atom = new Query_ReAtomicizer();
		lock = false;
	}

	/* Create a new table, store it locally and return it */
	public Table mkTable(String tn_name, FieldName... fns) {
		TableName tn = new TableName(tn_name);
		FieldName is_alive = new FieldName("is_alive", false, false, F_Type.BOOL);
		this.tableNameMap.put(tn.getName(), tn);
		Table newTable = new Table(tn, is_alive, fns);
		this.tableMap.put(tn.getName(), newTable);
		for (FieldName fn : fns)
			this.fieldNameMap.put(fn.getName(), fn);
		this.fieldNameMap.put(tn_name + "_is_alive", is_alive);
		return newTable;
	}

	/* Create a new table, store it locally and return it */
	public Table mkCRDTTable(String tn_name, FieldName... fns) {
		TableName tn = new TableName(tn_name);
		FieldName is_alive = new FieldName("is_alive", false, false, F_Type.BOOL);
		this.tableNameMap.put(tn.getName(), tn);
		Table newTable = new Table(tn, is_alive, fns);
		this.tableMap.put(tn.getName(), newTable);
		for (FieldName fn : fns)
			this.fieldNameMap.put(fn.getName(), fn);
		this.fieldNameMap.put(tn_name + "_is_alive", is_alive);
		newTable.setCrdt(true);
		return newTable;
	}

	public Table mkBasicTable(String tn_name, String... fns) {
		TableName tn = new TableName(tn_name);
		this.tableNameMap.put(tn_name, tn);

		ArrayList<FieldName> fresh_fn_list = new ArrayList<>();
		boolean isPK = true, isSK = true;
		for (String fn : fns) {
			FieldName new_fn = new FieldName(fn, isPK, isSK, F_Type.NUM);
			fresh_fn_list.add(new_fn);
			this.fieldNameMap.put(fn, new_fn);
			isPK = false;
			isPK = false;
		}
		Table newTable = new Table(tn, fresh_fn_list);
		this.tableMap.put(tn_name, newTable);
		return newTable;
	}

	/*
	 * Create a new VC, store it locally and return it
	 */
	public VC mkVC(String T_1, String T_2, VC_Agg vc_agg, VC_Type vc_type, VC_Constraint... constraints) {
		String name = "vc_" + this.vcMap.size();
		VC vc = new VC(name, this.getTableName(T_1), this.getTableName(T_2), vc_agg, vc_type);
		for (VC_Constraint vcc : constraints)
			vc.addConstraint(vcc);
		this.vcMap.put(name, vc);
		return vc;
	}

	public void addFieldTupleToVC(String vcName, String F_1, String F_2) {
		assert (this.vcMap.get(vcName) != null) : "cannot add tuple to a non-existing VC";
		this.vcMap.get(vcName).addFieldTuple(getFieldName(F_1), getFieldName(F_2));
	}

	/*
	 * Create a new transaction, store it locally and return it
	 */
	public Transaction mkTrnasaction(String txn_name, String... args) {
		assert (!lock) : "cannot call this function after locking";
		Transaction txn = new Transaction(txn_name);
		String transaction_name = txn.getName();
		this.getTrasnsactionMap().put(transaction_name, txn);
		transactionToArgsSetMap.put(transaction_name, new ArrayList<>());
		transactionToVariableSetMap.put(transaction_name, new ArrayList<>());
		for (String arg : args) {
			String[] parts = arg.split(":");
			E_Arg current_arg = new E_Arg(txn_name, parts[0], F_Type.stringTypeToFType(parts[1]));
			argsMap.put(parts[0], current_arg);
			txn.addArg(current_arg);
			transactionToArgsSetMap.get(transaction_name).add(current_arg);
		}
		return txn;
	}

	public Expression mkAssertion(String txn, Expression exp) {
		assert (!lock) : "cannot call this function after locking";
		this.getTrasnsactionMap().get(txn).addAssertion(exp);
		return exp;
	}

	/*
	 * Create a fresh variable, store it and return it
	 */
	public Variable mkVariable(String tn, String txn) {
		String fresh_variable_name = txn + "_v" + transactionToVariableSetMap.get(txn).size();
		Variable fresh_variable = new Variable(tn, fresh_variable_name);
		transactionToVariableSetMap.get(txn).add(fresh_variable);
		variableMap.put(fresh_variable_name, fresh_variable);
		variableToExpressionSetMap.put(fresh_variable, new HashSet<E_Proj>());
		return fresh_variable;
	}

	public String getFreshVariableName(String txn) {
		assert (!lock) : "cannot call this function after locking";
		return txn + "_v" + transactionToVariableSetMap.get(txn).size();
	}

	/*
	 * Create fresh expressions based on a variable
	 */
	public E_Proj mkProjExpr(String txn, int var_id, String fn, int order) {
		assert (!lock) : "cannot call this function after locking";
		Variable v = getVariable(txn, var_id);
		assert (v != null);
		assert (getFieldName(fn) != null);
		E_Proj exp = new E_Proj(v, getFieldName(fn), new E_Const_Num(order));
		assert (variableToExpressionSetMap
				.get(v) != null) : "cannot make a project expression on a non-existing variable";
		variableToExpressionSetMap.get(v).add(exp);
		return exp;
	}

	public E_Size mkSizeExpr(String txn, int id) {
		return new E_Size(getVariable(txn, id));
	}

	/*
	 * Return locally stored objects
	 */
	public Variable getVariable(String txn, int id) {
		return variableMap.get(txn + "_v" + id);
	}

	public Variable getVariable(String key) {
		return variableMap.get(key);
	}

	public TableName getTableName(String tn) {
		assert (this.tableNameMap.get(tn) != null);
		return this.tableNameMap.get(tn);
	}

	public FieldName getFieldName(String fn) {
		assert (this.fieldNameMap.get(fn) != null) : "something unholy happened on (" + fn + ")";
		return this.fieldNameMap.get(fn);
	}

	public FieldName getIsAliveFieldName(String table_name) {
		assert (this.fieldNameMap.get(
				table_name + "_is_alive") != null) : "something went wrong! the table does not contain is_alive field";
		return this.fieldNameMap.get(table_name + "_is_alive");
	}

	public E_Arg getArg(String arg) {
		return this.argsMap.get(arg);
	}

	public Query_Statement addQueryStatement(String txn, Query q) {
		assert (!lock) : "cannot call this function after locking";
		int stmt_counts = (transactionToStatement.containsKey(txn)) ? transactionToStatement.get(txn) : 0;
		transactionToStatement.put(txn, stmt_counts + 1);
		Query_Statement result = new Query_Statement(stmt_counts, q);
		getTrasnsactionMap().get(txn).addStatement(result);
		result.setPathCondition(new E_Const_Bool(true));
		return result;
	}

	public Query_Statement addQueryStatementInIf(String txn, int if_id, Query q) {
		assert (!lock) : "cannot call this function after locking";
		int stmt_counts = (transactionToStatement.containsKey(txn)) ? transactionToStatement.get(txn) : 0;
		transactionToStatement.put(txn, stmt_counts + 1);
		Query_Statement result = new Query_Statement(stmt_counts, q);
		If_Statement if_stmt = ifStatementMap.get(txn + "-if-" + if_id);
		Expression new_path_condition = new E_BinOp(BinOp.AND, if_stmt.getPathCondition(), if_stmt.getCondition());
		result.setPathCondition(new_path_condition);
		if_stmt.addStatementInIf(result);
		return result;
	}

	public Query_Statement addQueryStatementInElse(String txn, int if_id, Query q) {
		assert (!lock) : "cannot call this function after locking";
		int stmt_counts = (transactionToStatement.containsKey(txn)) ? transactionToStatement.get(txn) : 0;
		transactionToStatement.put(txn, stmt_counts + 1);
		Query_Statement result = new Query_Statement(stmt_counts, q);
		If_Statement if_stmt = ifStatementMap.get(txn + "-if-" + if_id);
		Expression new_path_condition = new E_BinOp(BinOp.AND, if_stmt.getPathCondition(),
				new E_UnOp(UnOp.NOT, if_stmt.getCondition()));
		result.setPathCondition(new_path_condition);
		if_stmt.addStatementInElse(result);
		return result;
	}

	public If_Statement addIfStatementInIf(String txn, int if_id, Expression c) {
		assert (!lock) : "cannot call this function after locking";
		int if_stmt_counts = (transactionToIf.containsKey(txn)) ? transactionToIf.get(txn) : 0;
		transactionToIf.put(txn, if_stmt_counts + 1);
		If_Statement result = new If_Statement(if_stmt_counts, c);
		Expression old_path_condition = ifStatementMap.get(txn + "-if-" + if_id).getPathCondition();
		Expression old_condition = ifStatementMap.get(txn + "-if-" + if_id).getCondition();
		result.setPathCondition(new E_BinOp(BinOp.AND, old_path_condition, old_condition));
		ifStatementMap.put(txn + "-if-" + if_stmt_counts, result);
		ifStatementMap.get(txn + "-if-" + if_id).addStatementInIf(result);
		return result;
	}

	public If_Statement addIfStatementInElse(String txn, int if_id, Expression c) {
		assert (!lock) : "cannot call this function after locking";
		int if_stmt_counts = (transactionToIf.containsKey(txn)) ? transactionToIf.get(txn) : 0;
		transactionToIf.put(txn, if_stmt_counts + 1);
		If_Statement result = new If_Statement(if_stmt_counts, c);
		Expression old_path_condition = ifStatementMap.get(txn + "-if-" + if_id).getPathCondition();
		Expression old_condition = ifStatementMap.get(txn + "-if-" + if_id).getCondition();
		result.setPathCondition(new E_BinOp(BinOp.AND, old_path_condition, new E_UnOp(UnOp.NOT, old_condition)));
		ifStatementMap.put(txn + "-if-" + if_stmt_counts, result);
		ifStatementMap.get(txn + "-if-" + if_id).addStatementInElse(result);
		return result;
	}

	/*
	 * create and add an empty if statement.
	 */
	public If_Statement addIfStatement(String txn, Expression c) {
		assert (!lock) : "cannot call this function after locking";
		int if_stmt_counts = (transactionToIf.containsKey(txn)) ? transactionToIf.get(txn) : 0;
		transactionToIf.put(txn, if_stmt_counts + 1);
		If_Statement result = new If_Statement(if_stmt_counts, c);
		result.setPathCondition(new E_Const_Bool(true));
		ifStatementMap.put(txn + "-if-" + if_stmt_counts, result);
		getTrasnsactionMap().get(txn).addStatement(result);// the enclosed statements will be added later
		return result;
	}

	public Select_Query addSelectQuery(String txn, String tableName, boolean isAtomic, WHC whc, String... fieldNames) {
		assert (!lock) : "cannot call this function after locking";
		int po = transactionToPoCnt.containsKey(txn) ? transactionToPoCnt.get(txn) : 0;
		transactionToPoCnt.put(txn, po + 1);
		Variable fresh_variable = mkVariable(tableName, txn);
		int select_counts = (transactionToSelectCount.containsKey(txn)) ? transactionToSelectCount.get(txn) : 0;
		transactionToSelectCount.put(txn, select_counts + 1);
		ArrayList<FieldName> fresh_field_names = new ArrayList<>();
		for (String fn : fieldNames)
			fresh_field_names.add(fieldNameMap.get(fn));
		Select_Query result = new Select_Query(po, select_counts, isAtomic, tableNameMap.get(tableName),
				fresh_field_names, fresh_variable, whc);
		return result;
	}

	public Update_Query addUpdateQuery(String txn, String tableName, boolean isAtomic, WHC whc) {
		assert (!lock) : "cannot call this function after locking";
		int po = transactionToPoCnt.containsKey(txn) ? transactionToPoCnt.get(txn) : 0;
		transactionToPoCnt.put(txn, po + 1);
		int update_counts = (transactionToUpdateCount.containsKey(txn)) ? transactionToUpdateCount.get(txn) : 0;
		transactionToUpdateCount.put(txn, update_counts + 1);
		Update_Query result = new Update_Query(po, update_counts, isAtomic, tableNameMap.get(tableName), whc);
		return result;
	}

	public Insert_Query addInsertQuery(String txn, String tableName, boolean isAtomic, WHC_Constraint... pks) {
		assert (!lock) : "cannot call this function after locking";
		int po = transactionToPoCnt.containsKey(txn) ? transactionToPoCnt.get(txn) : 0;
		transactionToPoCnt.put(txn, po + 1);
		int update_counts = (transactionToUpdateCount.containsKey(txn)) ? transactionToUpdateCount.get(txn) : 0;
		transactionToUpdateCount.put(txn, update_counts + 1);
		Insert_Query result = new Insert_Query(po, update_counts, tableMap.get(tableName),
				this.getIsAliveFieldName(tableName));
		result.addPKExp(pks);

		for (WHC_Constraint pk : pks)
			result.addInsertExp(pk.getFieldName(), pk.getExpression());
		return result;
	}

	public Delete_Query addDeleteQuery(String txn, String tableName, boolean isAtomic, WHC whc) {
		assert (!lock) : "cannot call this function after locking";
		int po = transactionToPoCnt.containsKey(txn) ? transactionToPoCnt.get(txn) : 0;
		transactionToPoCnt.put(txn, po + 1);
		int update_counts = (transactionToUpdateCount.containsKey(txn)) ? transactionToUpdateCount.get(txn) : 0;
		transactionToUpdateCount.put(txn, update_counts + 1);
		Delete_Query result = new Delete_Query(po, update_counts, isAtomic, tableNameMap.get(tableName),
				this.getIsAliveFieldName(tableName), whc);
		return result;
	}

	/*
	 * Update a table
	 */
	public void addFieldNameToTable(String tableName, FieldName fn) {
		Table table = this.tableMap.get(tableName);
		table.addFieldName(fn);
		this.fieldNameMap.put(fn.getName(), fn);
	}

	/*
	 * 
	 * Getters
	 * 
	 */

	public HashMap<String, Transaction> getTrasnsactionMap() {
		return trasnsactionMap;
	}

	// returns null if no VC is found
	public VC getVCByTables(TableName TN1, TableName TN2) {
		List<VC> result = this.vcMap.values().stream()
				.filter(vc -> ((vc.getTableName(1).equals(TN1)) && (vc.getTableName(2).equals(TN2))
						|| ((vc.getTableName(1).equals(TN2)) && (vc.getTableName(2).equals(TN1)))))
				.collect(Collectors.toList());
		if (result.size() == 0)
			return null;
		else
			return result.get(0);
	}

	public Table getTable(String tableName) {
		return this.tableMap.get(tableName);
	}

	public Query getQueryByPo(String txnName, int po) {
		Transaction txn = getTrasnsactionMap().get(txnName);
		for (Query q : txn.getAllQueries())
			if (q.getPo() == po)
				return q;
		logger.debug("no query was found in txn: " + txnName + " at po: " + po);
		return null;
	}

	public boolean checkPotKeyForFn(TableName tn, ArrayList<FieldName> pot_keys, FieldName fn) {
		// cases: either pot_keys are PK of tn, or there are VCs that state
		// pot_keys are sufficient for uniquness
		// case1: check if pot_keys are PK of tn
		Table t = tableMap.get(tn.getName());
		boolean contains_fn = t.getFieldNames().contains(fn);
		if (contains_fn) {
			logger.debug("first guard passed: the field " + fn + " is in the table " + tn);
			boolean contained_pks = pot_keys.containsAll(t.getPKFields());
			if (contained_pks) {
				logger.debug(
						"potential keys are in fact PK of the table, hence there is no need to do further analysis on VCs");
				return true;
			} else { // case 2: look for proper VCs
				logger.debug("potential keys were not PK of the original table, hence must search for proper VCs");
				for (Table other_t : this.tableMap.values()) {
					VC vc = getVCByTables(other_t.getTableName(), t.getTableName());
					if (vc != null) {
						logger.debug("a vc found between " + other_t.getTableName() + " and " + t.getTableName());
						logger.debug(
								"must now check if " + pot_keys + " has corresponding PK in " + other_t.getTableName());
						List<FieldName> constrained_fns = vc.getVCC().stream().map(vcc -> vcc.getF_1())
								.collect(Collectors.toList());
						logger.debug(
								"constrained fields in table " + other_t.getTableName() + " are: " + constrained_fns);
						List<FieldName> corresponding_constrained_fns = constrained_fns.stream()
								.map(mfn -> vc.getCorrespondingFN(mfn)).collect(Collectors.toList());
						logger.debug("corresponding fields for above fields are " + other_t.getTableName() + " are: "
								+ corresponding_constrained_fns);
						if (pot_keys.containsAll(corresponding_constrained_fns))
							return true;
					} else {
						logger.debug("no vc found between " + other_t.getTableName() + " and " + t.getTableName()
								+ ": continue");
					}
				}
			}
		}
		return false;
	}

	public boolean checkPotKeyForFns(TableName tn, ArrayList<FieldName> pot_keys, ArrayList<FieldName> fns) {
		for (FieldName fn : fns)
			if (!checkPotKeyForFn(tn, pot_keys, fn))
				return false;
		return true;
	}

	/*****************************************************************************************************************/
	/*
	 * Testing functions
	 */

	public Query_Statement mkTestQryStmt(String txnName) {
		assert (!lock) : "cannot call this function after locking";
		Variable v = new Variable("accounts", "v_test_" + 999);
		WHC GetAccount0_WHC = new WHC(getIsAliveFieldName("accounts"),
				new WHC_Constraint(getTableName("accounts"), getFieldName("a_custid"), BinOp.EQ, new E_Const_Num(999)));
		ArrayList<FieldName> fns = new ArrayList<>();
		fns.add(getFieldName("a_custid"));
		Select_Query q = new Select_Query(9, 999, true, getTableName("accounts"), fns, v, GetAccount0_WHC);
		return new Query_Statement(-1, q);
	}

	public Query_Statement mkTestQryStmt_6(String txnName) {
		Variable v = new Variable("accounts", "v_test_" + 666);
		WHC GetAccount0_WHC = new WHC(getIsAliveFieldName("accounts"),
				new WHC_Constraint(getTableName("accounts"), getFieldName("a_custid"), BinOp.EQ, new E_Const_Num(666)));
		ArrayList<FieldName> fns = new ArrayList<>();
		fns.add(getFieldName("a_custid"));
		Select_Query q = new Select_Query(6, 666, true, getTableName("accounts"), fns, v, GetAccount0_WHC);
		return new Query_Statement(-1, q);
	}

	public Block getBlockByPo(String txnName, int po) {
		Transaction txn = getTrasnsactionMap().get(txnName);
		return getBlockByPo_rec(new Block(BlockType.INIT, 0, -1), txnName, po, txn.getStatements());
	}

	public Block getBlockByPo_rec(Block current_block, String txnName, int po, ArrayList<Statement> inputList) {
		Block result = null;
		for (Statement stmt : inputList) {
			switch (stmt.getClass().getSimpleName()) {
			case "Query_Statement":
				Query_Statement qry_stmt = (Query_Statement) stmt;
				Query qry = qry_stmt.getQuery();
				if (qry.getPo() == po) {
					return current_block;
				}
				break;
			case "If_Statement":
				If_Statement if_stmt = (If_Statement) stmt;
				Block if_result = getBlockByPo_rec(
						new Block(BlockType.IF, current_block.getDepth() + 1, if_stmt.getIntId()), txnName, po,
						if_stmt.getIfStatements());
				Block else_result = getBlockByPo_rec(
						new Block(BlockType.ELSE, current_block.getDepth() + 1, if_stmt.getIntId()), txnName, po,
						if_stmt.getElseStatements());

				if (if_result != null && else_result == null)
					result = if_result;
				if (if_result == null && else_result != null)
					result = else_result;
				if (if_result == null && else_result == null)
					result = current_block;
				break;
			}
		}
		return result;
	}

}
