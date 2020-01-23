package kiarahmani.atropos.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

	/*
	 * Generate and return a program from the meta-data. Note that a program object
	 * is simply a packaging for currently stored meta-data
	 */
	public Program generateProgram() {
		Program program = new Program(program_name);
		for (Table t : tableMap.values())
			program.addTable(t);
		for (Transaction t : trasnsactionMap.values())
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
		trasnsactionMap = new HashMap<>();
		tableMap = new HashMap<>();
		tableNameMap = new HashMap<>();
		fieldNameMap = new HashMap<>();
		argsMap = new HashMap<>();
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
		this.trasnsactionMap.put(transaction_name, txn);
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
		this.trasnsactionMap.get(txn).addAssertion(exp);
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
		trasnsactionMap.get(txn).addStatement(result);
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
		trasnsactionMap.get(txn).addStatement(result);// the enclosed statements will be added later
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

	/*****************************************************************************************************************/
	/*
	 * perform a swap in the requested transaction
	 */
	public boolean swapQueries(String txnName, int q1_po, int q2_po) {
		assert (q1_po < q2_po) : "invalid args: first po must be less  than the second";
		Transaction txn = this.trasnsactionMap.get(txnName);
		assert (txn != null) : "swap request is made on a transaction that does not exist";
		// guard the swaps from invalid requests
		if (!swapChecks(txn, q1_po, q2_po))
			return false;
		swapQueries_rec(txn.getStatements(), q1_po, q2_po);
		return true;
	}

	/*
	 * Helping function used in swapQueries. It recusrsively checks continous blocks
	 * of statements
	 */
	private void swapQueries_rec(ArrayList<Statement> inputList, int q1_po, int q2_po) {
		int iter = 0;
		int index_po1 = -1, index_po2 = -1;
		loop_label: for (Statement stmt : inputList) {
			switch (stmt.getClass().getSimpleName()) {
			case "Query_Statement":
				Query_Statement qry_stmt = (Query_Statement) stmt;
				Query qry = qry_stmt.getQuery();
				if (qry.getPo() == q1_po) {
					qry.updatePO(q2_po);
					index_po1 = iter;
					break;
				}
				if (qry.getPo() == q2_po) {
					assert (index_po1 != -1) : "unexpected state: q2 is found in the current block but"
							+ " q1 belongs to another (possibly outer) block";
					qry.updatePO(q1_po);
					index_po2 = iter;
					break loop_label;
				}
				break;
			case "If_Statement":
				If_Statement if_stmt = (If_Statement) stmt;
				swapQueries_rec(if_stmt.getIfStatements(), q1_po, q2_po);
				swapQueries_rec(if_stmt.getElseStatements(), q1_po, q2_po);
				break;
			default:
				break;
			}
			iter++;
		}
		if (index_po1 != -1 && index_po2 != -1)
			Collections.swap(inputList, index_po1, index_po2);
	}

	/*
	 * Check if the requested swap is valid or not
	 */
	private boolean swapChecks(Transaction txn, int q1_po, int q2_po) {
		if (q1_po > q2_po)
			return false;
		Boolean result = swapChecks_rec(txn.getStatements(), q1_po, q2_po);
		assert (result != null) : "the requested swap is invalid and cannot be checked";
		return result;
	}

	/*
	 * Helping function used in swapChecks. Recursively analyzes the contineous
	 * blocks of statements
	 */
	private Boolean swapChecks_rec(ArrayList<Statement> inputList, int q1_po, int q2_po) {
		ArrayList<Query> pot_dep_qries = new ArrayList<>();
		Query qry1 = null;
		Query qry2 = null;
		boolean q1_seen_flag = false;
		loop_label: for (Statement stmt : inputList) {
			switch (stmt.getClass().getSimpleName()) {
			case "Query_Statement":
				Query_Statement qry_stmt = (Query_Statement) stmt;
				Query qry = qry_stmt.getQuery();
				if (qry.getPo() == q1_po) {
					q1_seen_flag = true;
					qry1 = qry;
					pot_dep_qries.add(qry);
					break;
				}
				if (qry.getPo() == q2_po) {
					assert (q1_seen_flag) : "unexpected state: q2 is found in the current block but"
							+ " q1 belongs to another (possibly outer) block";
					qry2 = qry;
					break loop_label;
				}
				if (q1_seen_flag && qryAreDep(qry1, qry))
					pot_dep_qries.add(qry);
				break;
			case "If_Statement":
				If_Statement if_stmt = (If_Statement) stmt;
				Boolean if_result = swapChecks_rec(if_stmt.getIfStatements(), q1_po, q2_po);
				if (if_result != null)
					return if_result;
				Boolean else_result = swapChecks_rec(if_stmt.getElseStatements(), q1_po, q2_po);
				if (else_result != null)
					return else_result;
				break;
			default:
				break;
			}
		}
		if (q1_seen_flag) {
			assert (qry2 != null) : "unexpected state: qry2 is not set although qry1 has been found in the current block";
			for (Query q : pot_dep_qries)
				if (qryAreDep(q, qry2))
					return false;
			return true;
		} else
			return null;
	}

	/*
	 * check if two queries are dependent on each other or not
	 */
	private boolean qryAreDep(Query qry1, Query qry2) {
		assert (qry1.getPo() < qry2
				.getPo()) : "unexpected state: algorithm assumes this function is only called for po1<po2 -> po1:"
						+ qry1.getPo() + " po2:" + qry2.getPo();
		if (qry1.getKind() == Kind.SELECT) {
			Select_Query slct_qry = (Select_Query) qry1;
			Variable var1 = slct_qry.getVariable();
			return qry2.getAllRefferencedVars().contains(var1);
			// the only case where true (identifying a dependency) is returned is when the
			// first query is a select and
			// the second query has a reference to the variable created by the first one
		}
		return false;
	}

	/*****************************************************************************************************************/

	public boolean redirectQuery(String txnName, int q_po, String tableName) {
		Transaction txn = this.trasnsactionMap.get(txnName);
		Query query = getQueryByPo(txnName, q_po);
		Table target_table = this.tableMap.get(tableName);
		Table source_table = this.tableMap.get(query.getTableName().getName());
		logger.debug("Redirecting Query: " + query.getId() + " in txn " + txn.getName());
		logger.debug("source table: " + source_table);
		logger.debug("target table: " + target_table);
		if (!redirectIsValid()) {
			logger.debug("Redirect is invalid. Will return false");
			return false;
		}
		logger.debug("Redirect is valid. Will proceed.");
		ArrayList<VC> vcs = getVCsByTables(target_table, source_table);
		logger.debug("VCs related to the requested redirect: " + vcs);

		return true;
	}

	private boolean redirectIsValid() {
		// TODO: make sure that *all* selected fields are in a VC with the target table
		return true;
	}

	/*****************************************************************************************************************/
	public Query getQueryByPo(String txnName, int q_po) {
		Transaction txn = this.trasnsactionMap.get(txnName);
		ArrayList<Query> allQ = txn.getAllQueries();
		assert (allQ.size() >= q_po) : "requested PO out of range";
		for (Query q : txn.getAllQueries())
			if (q.getPo() == q_po)
				return q;
		return null;
	}

	public ArrayList<VC> getVCsByTables(Table T1, Table T2) {
		TableName TN1 = T1.getTableName();
		TableName TN2 = T2.getTableName();
		return (ArrayList<VC>) this.vcMap.values().stream()
				.filter(vc -> (vc.getTableName(1).equals(TN1)) && (vc.getTableName(2).equals(TN2)))
				.collect(Collectors.toList());
	}

}
