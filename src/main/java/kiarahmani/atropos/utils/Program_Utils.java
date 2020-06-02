package kiarahmani.atropos.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DB.statementBuilder;
import kiarahmani.atropos.DB.tableBuilder;
import kiarahmani.atropos.DB.transactionBuilder;
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
import kiarahmani.atropos.DML.expression.E_Const;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.E_Size;
import kiarahmani.atropos.DML.expression.E_UnOp;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.E_UnOp.UnOp;
import kiarahmani.atropos.DML.expression.constants.E_Const_Bool;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.expression.constants.E_Const_Text;
import kiarahmani.atropos.DML.query.Delete_Query;
import kiarahmani.atropos.DML.query.Insert_Query;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHCC;
import kiarahmani.atropos.program.Block;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.program.Block.BlockType;
import kiarahmani.atropos.program.statements.If_Statement;
import kiarahmani.atropos.program.statements.Query_Statement;

/*****************************************************************************************************************/
// DSL front-end
/*****************************************************************************************************************/

public class Program_Utils {
	private static final Logger logger = LogManager.getLogger(Atropos.class);

	/*****************************************************************************************************************/
	// DSL front-end
	/*****************************************************************************************************************/

	public tableBuilder Table(String tname) {
		return new tableBuilder(this, tname);
	}

	public transactionBuilder Transaction(String txnName) {
		return new transactionBuilder(this, txnName);
	}

	public statementBuilder addStmt(String txnName) {
		return new statementBuilder(this, txnName);
	}

	public statementBuilder addInIf(String txnName, int ifId) {
		return new statementBuilder(this, txnName, ifId, true);
	}

	public statementBuilder addInElse(String txnName, int ifId) {
		return new statementBuilder(this, txnName, ifId, false);
	}

	/*****************************************************************************************************************/
	// meta-data and bookkeeping data structures
	/*****************************************************************************************************************/
	/*
	 * basic program meta data
	 */
	private String program_name;
	private int version;
	private String comments;
	private boolean lock;

	/*
	 * string to object mappings
	 */
	// schema components
	private HashMap<String, TableName> tableNameMap;
	private HashMap<String, Table> tableMap;
	private HashMap<String, FieldName> fieldNameMap;
	// program components
	private HashMap<String, Transaction> trasnsactionMap;
	private HashMap<String, E_Arg> argsMap;
	private HashMap<String, Variable> variableMap; // used after locking from other objects which call mkVariable
	private HashMap<String, If_Statement> ifStatementMap; // never used after locking
	// value Corresponces
	private HashMap<String, VC> vcMap; // only used AFTER locking

	/*
	 * Meta data maps from transaction instances the number of their components
	 */
	private HashMap<String, Integer> transactionToSelectCount; // used after locking (when a new select is generated)
	private HashMap<String, Integer> transactionToUpdateCount; // used after locking (when a new update is generated)
	private HashMap<String, Integer> transactionToStmtCount; // never used after locking
	private HashMap<String, Integer> transactionToIfCount; // never used after locking
	private HashMap<String, Integer> transactionToPoCount; // never used after locking
	private HashMap<String, Integer> transactionToVarCount; // used after locking (when a new variable is generated)

	/*****************************************************************************************************************/
	// Constructor Function
	/*****************************************************************************************************************/
	public Program_Utils(String pn) {
		// allocate new objects for all data structures
		this.program_name = pn;
		this.vcMap = new HashMap<>();
		tableMap = new HashMap<>();
		tableNameMap = new HashMap<>();
		fieldNameMap = new HashMap<>();
		argsMap = new HashMap<>();
		trasnsactionMap = new HashMap<>();
		transactionToVarCount = new HashMap<>();
		transactionToSelectCount = new HashMap<>();
		transactionToStmtCount = new HashMap<>();
		transactionToUpdateCount = new HashMap<>();
		transactionToIfCount = new HashMap<>();
		ifStatementMap = new HashMap<>();
		transactionToPoCount = new HashMap<>();
		variableMap = new HashMap<>();
		lock = false;
		this.comments = "";
	}

	/*****************************************************************************************************************/
	// Lock this object so base version generating functions cannot be called again
	/*****************************************************************************************************************/
	public void lock() {
		this.lock = true;
	}

	/*****************************************************************************************************************/
	// f=Functions to generate the base version (these functions cannot be called
	// again once lock() is called)
	/*****************************************************************************************************************/
	/*
	 * Create a new transaction, store it locally and return it
	 */
	public Transaction mkTrnasaction(String txn_name, String... args) {
		assert (!lock) : "cannot call this function after locking";
		Transaction txn = new Transaction(txn_name);
		this.getTrasnsactionMap().put(txn_name, txn);
		transactionToVarCount.put(txn_name, 0);
		for (String arg : args) {
			String[] parts = arg.split(":");
			E_Arg current_arg = new E_Arg(txn_name, parts[0], F_Type.stringTypeToFType(parts[1]));
			argsMap.put(parts[0], current_arg);
			txn.addArg(current_arg);
		}
		return txn;
	}

	public Transaction mkTrnasaction(String txn_name, ArrayList<String> args) {
		assert (!lock) : "cannot call this function after locking";
		Transaction txn = new Transaction(txn_name);
		this.getTrasnsactionMap().put(txn_name, txn);
		transactionToVarCount.put(txn_name, 0);
		for (String arg : args) {
			String[] parts = arg.split(":");
			E_Arg current_arg = new E_Arg(txn_name, parts[0], F_Type.stringTypeToFType(parts[1]));
			argsMap.put(parts[0], current_arg);
			txn.addArg(current_arg);
		}
		return txn;
	}

	/*
	 * create a new assertions about args in a transaction
	 */
	public Expression mkAssertion(String txn, Expression exp) {
		assert (!lock) : "cannot call this function after locking";
		this.getTrasnsactionMap().get(txn).addAssertion(exp);
		return exp;
	}

	/*
	 * create, add and return queries
	 */

	public Select_Query addSelectQuery(String txn, String tableName, WHC whc, String... fieldNames) {
		assert (!lock) : "cannot call this function after locking";
		boolean isAtomic = whc.isAtomic(getTable(tableName).getShardKey());
		int po = transactionToPoCount.containsKey(txn) ? transactionToPoCount.get(txn) : 0;
		transactionToPoCount.put(txn, po + 1);
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

	public Select_Query addSelectQuery(String txn, String tableName, String varName, WHC whc, String... fieldNames) {
		assert (!lock) : "cannot call this function after locking";
		boolean isAtomic = whc.isAtomic(getTable(tableName).getShardKey());
		int po = transactionToPoCount.containsKey(txn) ? transactionToPoCount.get(txn) : 0;
		transactionToPoCount.put(txn, po + 1);
		Variable fresh_variable = mkVariable(tableName, txn, varName);
		int select_counts = (transactionToSelectCount.containsKey(txn)) ? transactionToSelectCount.get(txn) : 0;
		transactionToSelectCount.put(txn, select_counts + 1);
		ArrayList<FieldName> fresh_field_names = new ArrayList<>();
		for (String fn : fieldNames)
			fresh_field_names.add(fieldNameMap.get(fn));
		Select_Query result = new Select_Query(po, select_counts, isAtomic, tableNameMap.get(tableName),
				fresh_field_names, fresh_variable, whc);
		return result;
	}

	public Update_Query addUpdateQuery(String txn, String tableName, WHC whc) {
		assert (!lock) : "cannot call this function after locking";
		boolean isAtomic = whc.isAtomic(getTable(tableName).getShardKey());
		int po = transactionToPoCount.containsKey(txn) ? transactionToPoCount.get(txn) : 0;
		transactionToPoCount.put(txn, po + 1);
		int update_counts = (transactionToUpdateCount.containsKey(txn)) ? transactionToUpdateCount.get(txn) : 0;
		transactionToUpdateCount.put(txn, update_counts + 1);
		Update_Query result = new Update_Query(po, update_counts, isAtomic, tableNameMap.get(tableName), whc);
		return result;
	}

	public Insert_Query addInsertQuery(String txn, String tableName, WHCC... pks) {
		assert (!lock) : "cannot call this function after locking";
		int po = transactionToPoCount.containsKey(txn) ? transactionToPoCount.get(txn) : 0;
		transactionToPoCount.put(txn, po + 1);
		int update_counts = (transactionToUpdateCount.containsKey(txn)) ? transactionToUpdateCount.get(txn) : 0;
		transactionToUpdateCount.put(txn, update_counts + 1);
		Insert_Query result = new Insert_Query(po, update_counts, tableMap.get(tableName),
				this.getIsAliveFieldName(tableName));
		result.addPKExp(pks);

		for (WHCC pk : pks)
			result.addInsertExp(pk.getFieldName(), pk.getExpression());
		return result;
	}

	public Delete_Query addDeleteQuery(String txn, String tableName, WHC whc) {
		assert (!lock) : "cannot call this function after locking";
		boolean isAtomic = whc.isAtomic(getTable(tableName).getShardKey());
		int po = transactionToPoCount.containsKey(txn) ? transactionToPoCount.get(txn) : 0;
		transactionToPoCount.put(txn, po + 1);
		int update_counts = (transactionToUpdateCount.containsKey(txn)) ? transactionToUpdateCount.get(txn) : 0;
		transactionToUpdateCount.put(txn, update_counts + 1);
		Delete_Query result = new Delete_Query(po, update_counts, isAtomic, tableNameMap.get(tableName),
				this.getIsAliveFieldName(tableName), whc);
		return result;
	}

	/*
	 * create, add (either in the main body or in an existing if) and return
	 * statements
	 */
	public Query_Statement addQueryStatement(String txn, Query q) {
		assert (!lock) : "cannot call this function after locking";
		int stmt_counts = (transactionToStmtCount.containsKey(txn)) ? transactionToStmtCount.get(txn) : 0;
		transactionToStmtCount.put(txn, stmt_counts + 1);
		Query_Statement result = new Query_Statement(stmt_counts, q);
		getTrasnsactionMap().get(txn).addStatement(result);
		result.setPathCondition(new E_Const_Bool(true));
		return result;
	}

	public Query_Statement addQueryStatementInIf(String txn, int if_id, Query q) {
		assert (!lock) : "cannot call this function after locking";
		int stmt_counts = (transactionToStmtCount.containsKey(txn)) ? transactionToStmtCount.get(txn) : 0;
		transactionToStmtCount.put(txn, stmt_counts + 1);
		Query_Statement result = new Query_Statement(stmt_counts, q);
		If_Statement if_stmt = ifStatementMap.get(txn + "-if-" + if_id);
		Expression new_path_condition = new E_BinOp(BinOp.AND, if_stmt.getPathCondition(), if_stmt.getCondition());
		result.setPathCondition(new_path_condition);
		if_stmt.addStatementInIf(result);
		return result;
	}

	public Query_Statement addQueryStatementInElse(String txn, int if_id, Query q) {
		assert (!lock) : "cannot call this function after locking";
		int stmt_counts = (transactionToStmtCount.containsKey(txn)) ? transactionToStmtCount.get(txn) : 0;
		transactionToStmtCount.put(txn, stmt_counts + 1);
		Query_Statement result = new Query_Statement(stmt_counts, q);
		If_Statement if_stmt = ifStatementMap.get(txn + "-if-" + if_id);
		Expression new_path_condition = new E_BinOp(BinOp.AND, if_stmt.getPathCondition(),
				new E_UnOp(UnOp.NOT, if_stmt.getCondition()));
		result.setPathCondition(new_path_condition);
		if_stmt.addStatementInElse(result);
		return result;
	}

	public If_Statement addIfStmt(String txn, Expression c) {
		assert (!lock) : "cannot call this function after locking";
		int if_stmt_counts = (transactionToIfCount.containsKey(txn)) ? transactionToIfCount.get(txn) : 0;
		transactionToIfCount.put(txn, if_stmt_counts + 1);
		If_Statement result = new If_Statement(if_stmt_counts, c);
		result.setPathCondition(new E_Const_Bool(true));
		ifStatementMap.put(txn + "-if-" + if_stmt_counts, result);
		getTrasnsactionMap().get(txn).addStatement(result);// the enclosed statements will be added later
		return result;
	}

	public If_Statement addIfStatementInIf(String txn, int if_id, Expression c) {
		assert (!lock) : "cannot call this function after locking";
		int if_stmt_counts = (transactionToIfCount.containsKey(txn)) ? transactionToIfCount.get(txn) : 0;
		transactionToIfCount.put(txn, if_stmt_counts + 1);
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
		int if_stmt_counts = (transactionToIfCount.containsKey(txn)) ? transactionToIfCount.get(txn) : 0;
		transactionToIfCount.put(txn, if_stmt_counts + 1);
		If_Statement result = new If_Statement(if_stmt_counts, c);
		Expression old_path_condition = ifStatementMap.get(txn + "-if-" + if_id).getPathCondition();
		Expression old_condition = ifStatementMap.get(txn + "-if-" + if_id).getCondition();
		result.setPathCondition(new E_BinOp(BinOp.AND, old_path_condition, new E_UnOp(UnOp.NOT, old_condition)));
		ifStatementMap.put(txn + "-if-" + if_stmt_counts, result);
		ifStatementMap.get(txn + "-if-" + if_id).addStatementInElse(result);
		return result;
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
		return exp;
	}

	/*****************************************************************************************************************/
	// Book keeping functions regarding VC
	/*****************************************************************************************************************/
	public void mkVC(VC vc) {
		this.vcMap.put(vc.getName(), vc);
	}

	public void rmVC(VC vc) {
		this.vcMap.remove(vc.getName());
	}

	public VC getVC(String key) {
		return this.vcMap.get(key);
	}

	public int getVCCnt() {
		return this.vcMap.size();
	}

	public VC getVCByTables(TableName TN1, TableName TN2) {
		logger.debug("current pu: " + this);
		logger.debug("finding a VC between " + TN1 + " and " + TN2);
		logger.debug("current list of VCs: " + this.vcMap.values());
		List<VC> result = this.vcMap.values().stream()
				.filter(vc -> ((vc.getTableName(this, 1).equalsWith(TN1)) && (vc.getTableName(this, 2).equalsWith(TN2))
						|| ((vc.getTableName(this, 1).equalsWith(TN2)) && (vc.getTableName(this, 2).equalsWith(TN1)))))
				.collect(Collectors.toList());
		if (result.size() == 0) {
			logger.debug("no such VC was found. returning null");
			return null; // returns null if no VC is found

		} else {
			logger.debug("desired VC is found. returning " + result.get(0));
			return result.get(0);
		}
	}

	public VC getVCByOrderedTables(TableName TN1, TableName TN2) {
		List<VC> result = this.vcMap.values().stream().filter(
				vc -> ((vc.getTableName(this, 1).equalsWith(TN1)) && (vc.getTableName(this, 2).equalsWith(TN2))))
				.collect(Collectors.toList());
		if (result.size() == 0)
			return null; // returns null if no VC is found
		else
			return result.get(0);
	}

	public void addFieldTupleToVC(String vcName, String F_1, String F_2) {
		assert (this.vcMap.get(vcName) != null) : "cannot add tuple to a non-existing VC";
		this.vcMap.get(vcName).addFieldTuple(F_1, F_2);
	}

	public void addFieldTupleToVC(String vcName, FieldName F_1, FieldName F_2) {
		assert (this.vcMap.get(vcName) != null) : "cannot add tuple to a non-existing VC";
		this.vcMap.get(vcName).addFieldTuple(F_1.getName(), F_2.getName());
	}

	public void addKeyCorrespondenceToVC(String vcName, String F_1, String F_2) {
		assert (this.vcMap.get(vcName) != null) : "cannot add tuple to a non-existing VC";
		VC_Constraint vcc = new VC_Constraint(F_1, F_2);
		this.vcMap.get(vcName).addConstraint(vcc);
	}

	public void rmTransaction(String txn_name) {
		this.trasnsactionMap.remove(txn_name);
	}

	/*****************************************************************************************************************/
	// Basic Getters
	/*****************************************************************************************************************/

	/*
	 * Tables and TableNames
	 */
	public Table getTable(String tableName) {
		return this.tableMap.get(tableName);
	}

	public Table getTable(TableName tableName) {
		return this.tableMap.get(tableName.getName());
	}

	public TableName getTableName(String tn) {
		// assert (this.tableNameMap.get(tn) != null) : "table " + tn + " does not exist
		// in "+this.tableNameMap ;
		if (this.tableNameMap.get(tn) == null)
			return new TableName("NULL");

		return this.tableNameMap.get(tn);
	}

	public HashMap<String, Table> getTables() {
		return this.tableMap;
	}

	/*
	 * FieldNames
	 */
	public FieldName getFieldName(String fn) {
		// assert (this.fieldNameMap.get(fn) != null) : "something unholy happened on ("
		// + fn + ")";
		return this.fieldNameMap.get(fn);
	}

	public FieldName getIsAliveFieldName(String table_name) {
		assert (this.fieldNameMap.get(
				table_name + "_is_alive") != null) : "something went wrong! the table does not contain is_alive field";
		return this.fieldNameMap.get(table_name + "_is_alive");
	}

	/*
	 * Transaction
	 */
	public HashMap<String, Transaction> getTrasnsactionMap() {
		return trasnsactionMap;
	}

	public List<Transaction> getIncludedTrasnsactionMap() {
		return trasnsactionMap.values().stream().filter(txn -> txn.is_included).collect(Collectors.toList());
	}

	/*
	 * Variables
	 */
	public Variable getVariable(String txn, int id) {
		return variableMap.get(txn + "_v" + id);
	}

	public Variable getVariable(String key) {
		return variableMap.get(key);
	}

	/*
	 * Queries
	 */
	public Query getQueryByPo(String txnName, int po) {
		Transaction txn = getTrasnsactionMap().get(txnName);
		for (Query q : txn.getAllQueries())
			if (q.getPo() == po)
				return q;
		logger.debug("no query was found in txn: " + txnName + " at po: " + po);
		return null;
	}

	/*
	 * Args
	 */
	public E_Arg getArg(String arg) {
		return this.argsMap.get(arg);
	}

	public E_Arg arg(String arg) {
		return this.argsMap.get(arg);
	}

	public E_Proj at(String fn, String varName, int order) {
		Variable v = variableMap.get(varName);
		assert (v != null);
		assert (getFieldName(fn) != null);
		return new E_Proj(v, getFieldName(fn), new E_Const_Num(order));
	}

	public Expression plus(Expression e1, Expression e2) {
		return new E_BinOp(BinOp.PLUS, e1, e2);
	}

	public Expression gt(Expression e1, Expression e2) {
		return new E_BinOp(BinOp.GT, e1, e2);
	}

	public Expression lt(Expression e1, Expression e2) {
		return new E_BinOp(BinOp.LT, e1, e2);
	}

	public Expression eq(Expression e1, Expression e2) {
		return new E_BinOp(BinOp.EQ, e1, e2);
	}

	public Expression mult(Expression e1, Expression e2) {
		return new E_BinOp(BinOp.MULT, e1, e2);
	}

	public Expression minus(Expression e1, Expression e2) {
		return new E_BinOp(BinOp.MINUS, e1, e2);
	}

	public Expression div(Expression e1, Expression e2) {
		return new E_BinOp(BinOp.DIV, e1, e2);
	}

	public E_Const_Num cons(int i) {
		return new E_Const_Num(i);
	}

	public E_Const_Bool cons(boolean b) {
		return new E_Const_Bool(b);
	}

	public E_Const_Text cons(String s) {
		return new E_Const_Text(s);
	}

	/*
	 * Blocks
	 */
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

	/*
	 * Fresh IDs
	 */
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

	public HashMap<TableName, Integer> getTableWieght() {
		HashMap<TableName, Integer> table_weight_map = new HashMap<>();
		for (Table t : this.tableMap.values())
			table_weight_map.put(t.getTableName(), 0);
		for (Transaction txn : this.trasnsactionMap.values())
			for (Query q : txn.getAllQueries())
				if (q.isWrite()) {
					int old_weight = table_weight_map.get(q.getTableName());
					table_weight_map.put(q.getTableName(), old_weight + 1);
				}

		return table_weight_map;
	}

	/*****************************************************************************************************************/
	// Meta Data interface
	/*****************************************************************************************************************/
	public void incVersion() {
		this.version++;
	}

	public HashMap<String, VC> getVCMap() {
		return this.vcMap;
	}

	public int getVersion() {
		return this.version;
	}

	public void decVersion() {
		this.version--;
	}

	public String getProgramName() {
		return this.program_name;
	}

	public String getComments() {
		return this.comments;
	}

	public void resetComments() {
		this.comments = "";
	}

	public void addComment(String comment) {
		this.comments += comment;
	}

	/*****************************************************************************************************************/
	// Make, store and return new components (and remove, for some dynamically
	// created/deleted components)
	/*****************************************************************************************************************/
	/*
	 * new table
	 */
	public Table mkTable(String tn_name, FieldName... fns) {
		TableName tn = new TableName(tn_name);
		FieldName is_alive = new FieldName("is_alive", false, false, F_Type.BOOL);
		this.tableNameMap.put(tn.getName(), tn);
		Table newTable = new Table(tn, is_alive, fns);
		newTable.setIsAllPK(false);
		this.tableMap.put(tn.getName(), newTable);
		for (FieldName fn : fns)
			this.fieldNameMap.put(fn.getName(), fn);
		this.fieldNameMap.put(tn_name + "_is_alive", is_alive);
		return newTable;
	}

	/*
	 * new table
	 */
	public Table mkTable(String tn_name, ArrayList<FieldName> fns) {
		TableName tn = new TableName(tn_name);
		FieldName is_alive = new FieldName("is_alive", false, false, F_Type.BOOL);
		this.tableNameMap.put(tn.getName(), tn);
		Table newTable = new Table(tn, is_alive, fns);
		newTable.setIsAllPK(false);
		this.tableMap.put(tn.getName(), newTable);
		for (FieldName fn : fns)
			this.fieldNameMap.put(fn.getName(), fn);
		this.fieldNameMap.put(tn_name + "_is_alive", is_alive);
		return newTable;
	}

	public Table mkAllPKTable(String tn_name, FieldName... fns) {
		TableName tn = new TableName(tn_name);
		FieldName is_alive = new FieldName("is_alive", false, false, F_Type.BOOL);
		this.tableNameMap.put(tn.getName(), tn);
		Table newTable = new Table(tn, is_alive, fns);
		newTable.setIsAllPK(true);
		this.tableMap.put(tn.getName(), newTable);
		for (FieldName fn : fns)
			this.fieldNameMap.put(fn.getName(), fn);
		this.fieldNameMap.put(tn_name + "_is_alive", is_alive);
		logger.debug("adding table " + tn_name + " whose allPK is set to: " + newTable.isAllPK());
		return newTable;
	}

	public void rmTable(String tn_name) {
		Table toBeRemovedTable = this.tableMap.get(tn_name);
		this.tableNameMap.remove(tn_name);
		this.tableMap.remove(tn_name);
		for (FieldName fn : toBeRemovedTable.getFieldNames())
			this.fieldNameMap.remove(fn.getName());
		this.fieldNameMap.remove(tn_name + "_is_alive");
	}

	/*
	 * new CRDT table
	 */
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

	/*
	 * variable
	 */
	public Variable mkVariable(String tn, String txn) {
		int var_cnt = transactionToVarCount.get(txn);
		String fresh_variable_name = txn + "_v" + var_cnt;
		Variable fresh_variable = new Variable(tn, fresh_variable_name);
		transactionToVarCount.put(txn, ++var_cnt);
		variableMap.put(fresh_variable_name, fresh_variable);
		return fresh_variable;
	}

	public Variable mkVariable(String tn, String txn, String name) {
		int var_cnt = transactionToVarCount.get(txn);
		String fresh_variable_name = name;
		Variable fresh_variable = new Variable(tn, fresh_variable_name);
		transactionToVarCount.put(txn, ++var_cnt);
		variableMap.put(fresh_variable_name, fresh_variable);
		return fresh_variable;
	}

	public void rmVariable(String txnName, Variable var) {
		int var_cnt = transactionToVarCount.get(txnName);
		transactionToVarCount.put(txnName, --var_cnt);
		variableMap.remove(var.getName());
	}

	/*
	 * size expr
	 */
	public E_Size mkSizeExpr(String txn, int id) {
		return new E_Size(getVariable(txn, id));
	}

	/*****************************************************************************************************************/
	// Update the schema
	/*****************************************************************************************************************/
	public void addFieldNameToTable(String tableName, FieldName fn) {
		Table table = this.tableMap.get(tableName);
		table.addFieldName(fn);
		this.fieldNameMap.put(fn.getName(), fn);
	}

	public void removeFieldNameFromTable(String tableName, FieldName fn) {
		Table table = this.tableMap.get(tableName);
		table.removeFieldName(fn);
		this.fieldNameMap.remove(fn.getName());
	}

	/*****************************************************************************************************************/
	// Analyze properties of Schema
	/*****************************************************************************************************************/

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
						List<FieldName> constrained_fns = vc.getVCC().stream().map(vcc -> vcc.getF_1(this))
								.collect(Collectors.toList());
						logger.debug(
								"constrained fields in table " + other_t.getTableName() + " are: " + constrained_fns);
						List<FieldName> corresponding_constrained_fns = constrained_fns.stream()
								.map(mfn -> vc.getCorrespondingFN(this, mfn)).collect(Collectors.toList());
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
	// Generate program instances
	/*****************************************************************************************************************/
	/*
	 * return a program based on current state
	 */
	public Program generateProgram() {
		Program program = new Program(program_name, version, comments);
		for (Table t : tableMap.values())
			program.addTable(t);
		for (Transaction t : getTrasnsactionMap().values())
			program.addTransaction(t);
		for (VC vc : vcMap.values())
			program.addVC(vc);
		program.setMaxQueryCount();
		comments = "";
		return program;
	}

	/*****************************************************************************************************************/
	// Testing functions (only for developement)
	/*****************************************************************************************************************/
	/*
	 * generate a sample Query_Statement
	 */
	public Query_Statement mkTestQryStmt(String txnName) {
		assert (!lock) : "cannot call this function after locking";
		Variable v = new Variable("accounts", "v_test_" + 999);
		WHC GetAccount0_WHC = new WHC(getIsAliveFieldName("accounts"),
				new WHCC(getTableName("accounts"), getFieldName("a_custid"), BinOp.EQ, new E_Const_Num(999)));
		ArrayList<FieldName> fns = new ArrayList<>();
		fns.add(getFieldName("a_custid"));
		Select_Query q = new Select_Query(9, 999, true, getTableName("accounts"), fns, v, GetAccount0_WHC);
		return new Query_Statement(-1, q);
	}

	/*
	 * generate a sanoke Table where only the first field is both PK and SK This is
	 * kind of redundant; it simply meant to offer a simpler interface when a table
	 * must be create quicklyS
	 */
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
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	public Program_Utils mkSnapShot() {
		Program_Utils snapshot = new Program_Utils(program_name);
		snapshot.version = this.version;
		snapshot.comments = this.comments;
		snapshot.lock = this.lock;
		snapshot.argsMap = this.argsMap;
		snapshot.ifStatementMap = this.ifStatementMap;
		snapshot.transactionToSelectCount = this.transactionToSelectCount;
		snapshot.transactionToUpdateCount = this.transactionToUpdateCount;
		snapshot.transactionToStmtCount = this.transactionToStmtCount;
		snapshot.transactionToIfCount = this.transactionToIfCount;
		snapshot.transactionToPoCount = this.transactionToPoCount;
		snapshot.transactionToVarCount = this.transactionToVarCount;
		// tableNameMap
		for (String key : this.tableNameMap.keySet()) {
			TableName val = this.tableNameMap.get(key);
			snapshot.tableNameMap.put(key, val);
		}

		// tableMap
		for (String key : this.tableMap.keySet()) {
			Table val = this.tableMap.get(key);
			snapshot.tableMap.put(key, val.mkSnapshot());
		}

		// fieldNameMap
		for (String key : this.fieldNameMap.keySet()) {
			FieldName val = this.fieldNameMap.get(key);
			snapshot.fieldNameMap.put(key, val);
		}

		// transactionMap
		for (String key : this.trasnsactionMap.keySet()) {
			Transaction val = this.trasnsactionMap.get(key);
			snapshot.trasnsactionMap.put(key, val.mkSnapshot());
		}

		// variableMap
		for (String key : this.variableMap.keySet()) {
			Variable val = this.variableMap.get(key);
			snapshot.variableMap.put(key, val);
		}

		// VCMap
		for (String key : this.vcMap.keySet()) {
			VC val = this.vcMap.get(key);
			snapshot.vcMap.put(key, val.mkSnapshot());
		}

		return snapshot;
	}

}
