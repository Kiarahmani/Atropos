package kiarahmani.atropos.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.org.apache.xpath.internal.functions.Function;

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
import kiarahmani.atropos.DML.expression.E_BinUp;
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
import kiarahmani.atropos.DML.query.Query.Kind;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.program.statements.If_Statement;
import kiarahmani.atropos.program.statements.Query_Statement;

public class Program_Utils {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	// basic program meta data
	private String program_name;
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
		Program program = new Program(program_name);
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

	/*
	 * Create fresh expressions based on a variable
	 */
	public E_Proj mkProjExpr(String txn, int id, String fn, int order) {
		Variable v = getVariable(txn, id);
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
		int stmt_counts = (transactionToStatement.containsKey(txn)) ? transactionToStatement.get(txn) : 0;
		transactionToStatement.put(txn, stmt_counts + 1);
		Query_Statement result = new Query_Statement(stmt_counts, q);
		getTrasnsactionMap().get(txn).addStatement(result);
		result.setPathCondition(new E_Const_Bool(true));
		return result;
	}

	public Query_Statement addQueryStatementInIf(String txn, int if_id, Query q) {
		int stmt_counts = (transactionToStatement.containsKey(txn)) ? transactionToStatement.get(txn) : 0;
		transactionToStatement.put(txn, stmt_counts + 1);
		Query_Statement result = new Query_Statement(stmt_counts, q);
		If_Statement if_stmt = ifStatementMap.get(txn + "-if-" + if_id);
		Expression new_path_condition = new E_BinUp(BinOp.AND, if_stmt.getPathCondition(), if_stmt.getCondition());
		result.setPathCondition(new_path_condition);
		if_stmt.addStatementInIf(result);
		return result;
	}

	public Query_Statement addQueryStatementInElse(String txn, int if_id, Query q) {
		int stmt_counts = (transactionToStatement.containsKey(txn)) ? transactionToStatement.get(txn) : 0;
		transactionToStatement.put(txn, stmt_counts + 1);
		Query_Statement result = new Query_Statement(stmt_counts, q);
		If_Statement if_stmt = ifStatementMap.get(txn + "-if-" + if_id);
		Expression new_path_condition = new E_BinUp(BinOp.AND, if_stmt.getPathCondition(),
				new E_UnOp(UnOp.NOT, if_stmt.getCondition()));
		result.setPathCondition(new_path_condition);
		if_stmt.addStatementInElse(result);
		return result;
	}

	public If_Statement addIfStatementInIf(String txn, int if_id, Expression c) {
		int if_stmt_counts = (transactionToIf.containsKey(txn)) ? transactionToIf.get(txn) : 0;
		transactionToIf.put(txn, if_stmt_counts + 1);
		If_Statement result = new If_Statement(if_stmt_counts, c, new ArrayList<Statement>());
		Expression old_path_condition = ifStatementMap.get(txn + "-if-" + if_id).getPathCondition();
		Expression old_condition = ifStatementMap.get(txn + "-if-" + if_id).getCondition();
		result.setPathCondition(new E_BinUp(BinOp.AND, old_path_condition, old_condition));
		ifStatementMap.put(txn + "-if-" + if_stmt_counts, result);
		ifStatementMap.get(txn + "-if-" + if_id).addStatementInIf(result);
		return result;
	}

	public If_Statement addIfStatementInElse(String txn, int if_id, Expression c) {
		int if_stmt_counts = (transactionToIf.containsKey(txn)) ? transactionToIf.get(txn) : 0;
		transactionToIf.put(txn, if_stmt_counts + 1);
		If_Statement result = new If_Statement(if_stmt_counts, c, new ArrayList<Statement>());
		Expression old_path_condition = ifStatementMap.get(txn + "-if-" + if_id).getPathCondition();
		Expression old_condition = ifStatementMap.get(txn + "-if-" + if_id).getCondition();
		result.setPathCondition(new E_BinUp(BinOp.AND, old_path_condition, new E_UnOp(UnOp.NOT, old_condition)));
		ifStatementMap.put(txn + "-if-" + if_stmt_counts, result);
		ifStatementMap.get(txn + "-if-" + if_id).addStatementInElse(result);
		return result;
	}

	/*
	 * create and add an empty if statement.
	 */
	public If_Statement addIfStatement(String txn, Expression c) {
		int if_stmt_counts = (transactionToIf.containsKey(txn)) ? transactionToIf.get(txn) : 0;
		transactionToIf.put(txn, if_stmt_counts + 1);
		If_Statement result = new If_Statement(if_stmt_counts, c, new ArrayList<Statement>());
		result.setPathCondition(new E_Const_Bool(true));
		ifStatementMap.put(txn + "-if-" + if_stmt_counts, result);
		getTrasnsactionMap().get(txn).addStatement(result);// the enclosed statements will be added later
		return result;
	}

	public Select_Query addSelectQuery(String txn, String tableName, boolean isAtomic, WHC whc, String... fieldNames) {
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
		int po = transactionToPoCnt.containsKey(txn) ? transactionToPoCnt.get(txn) : 0;
		transactionToPoCnt.put(txn, po + 1);
		int update_counts = (transactionToUpdateCount.containsKey(txn)) ? transactionToUpdateCount.get(txn) : 0;
		transactionToUpdateCount.put(txn, update_counts + 1);
		Update_Query result = new Update_Query(po, update_counts, isAtomic, tableNameMap.get(tableName), whc);
		return result;
	}

	public Insert_Query addInsertQuery(String txn, String tableName, boolean isAtomic, WHC_Constraint... pks) {
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

	public VC getVCByTables(Table T1, Table T2) {
		TableName TN1 = T1.getTableName();
		TableName TN2 = T2.getTableName();
		return this.vcMap.values().stream()
				.filter(vc -> ((vc.getTableName(1).equals(TN1)) && (vc.getTableName(2).equals(TN2))
						|| ((vc.getTableName(1).equals(TN2)) && (vc.getTableName(2).equals(TN1)))))
				.collect(Collectors.toList()).get(0);
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

	/*****************************************************************************************************************/
	/*
	 * Examine if a where clause entails atomicity of the query or not
	 */

	public boolean whcIsAtomic(WHC whc) {
		return true;
	}

	/*****************************************************************************************************************/
	/*
	 * Testing functions
	 */

	public Query_Statement mkTestQryStmt(String txnName) {
		int id = getNewSelectId(txnName);
		Variable v = new Variable("accounts", "v_test_" + 999);
		WHC GetAccount0_WHC = new WHC(getIsAliveFieldName("accounts"),
				new WHC_Constraint(getTableName("accounts"), getFieldName("a_custid"), BinOp.EQ, new E_Const_Num(999)));
		ArrayList<FieldName> fns = new ArrayList<>();
		fns.add(getFieldName("a_custid"));
		Select_Query q = new Select_Query(9, 999, true, getTableName("accounts"), fns, v, GetAccount0_WHC);
		return new Query_Statement(-1, q);
	}
	
	public Query_Statement mkTestQryStmt_6(String txnName) {
		int id = getNewSelectId(txnName);
		Variable v = new Variable("accounts", "v_test_" + 666);
		WHC GetAccount0_WHC = new WHC(getIsAliveFieldName("accounts"),
				new WHC_Constraint(getTableName("accounts"), getFieldName("a_custid"), BinOp.EQ, new E_Const_Num(666)));
		ArrayList<FieldName> fns = new ArrayList<>();
		fns.add(getFieldName("a_custid"));
		Select_Query q = new Select_Query(6, 666, true, getTableName("accounts"), fns, v, GetAccount0_WHC);
		return new Query_Statement(-1, q);
	}

}
