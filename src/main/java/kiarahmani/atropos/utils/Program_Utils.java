package kiarahmani.atropos.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.E_Arg;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.program.statements.If_Statement;
import kiarahmani.atropos.program.statements.Query_Statement;

public class Program_Utils {
	private Program program;
	private String program_name;
	private HashMap<String, Transaction> trasnsactionMap;
	private HashMap<String, Table> tableMap;
	private HashMap<String, TableName> tableNameMap;
	private HashMap<String, FieldName> fieldNameMap;
	private HashMap<String, ArrayList<Variable>> transactionToVariableSetMap;
	private HashMap<String, E_Arg> argsMap;
	private HashMap<String, ArrayList<E_Arg>> transactionToArgsSetMap;
	private HashMap<String, Integer> transactionToSelectCount;
	private HashMap<String, Integer> transactionToUpdateCount;
	private HashMap<String, Integer> transactionToStatement;
	private HashMap<String, Integer> transactionToIf;
	private HashMap<String, If_Statement> ifStatementMap;
	private HashMap<String, Variable> variableMap;

	public Program getProgram() {
		if (this.program == null) {
			this.program = new Program(program_name);
			for (Table t : tableMap.values())
				program.addTable(t);
			for (Transaction t : trasnsactionMap.values())
				program.addTransaction(t);
			program.setMaxQueryCount();
		}
		return program;
	}

	public Program_Utils(String pn) {
		this.program_name = pn;
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
		variableMap = new HashMap<>();
	}

	public Variable getVariable(String txn, int id) {
		return variableMap.get(txn + "_v" + id);
	}

	public Variable getFreshVariable(String txn) {
		String fresh_variable_name = txn + "_v" + transactionToVariableSetMap.get(txn).size();
		Variable fresh_variable = new Variable(fresh_variable_name);
		transactionToVariableSetMap.get(txn).add(fresh_variable);
		variableMap.put(fresh_variable_name, fresh_variable);
		return fresh_variable;
	}

	public E_Proj getProjExpr(String txn, int id, String fn, int order) {
		return new E_Proj(getVariable(txn, id), getFieldName(fn), new E_Const_Num(order));
	}

	public FieldName getFieldName(String fn) {
		return this.fieldNameMap.get(fn);
	}

	public E_Arg getArg(String arg) {
		return this.argsMap.get(arg);
	}

	public Query_Statement addQueryStatement(String txn, Query q) {
		int stmt_counts = (transactionToStatement.containsKey(txn)) ? transactionToStatement.get(txn) : 0;
		transactionToStatement.put(txn, stmt_counts + 1);
		Query_Statement result = new Query_Statement(stmt_counts, q);
		trasnsactionMap.get(txn).addStatement(result);
		return result;
	}

	public Query_Statement addQueryStatementInIf(String txn, int if_id, Query q) {
		int stmt_counts = (transactionToStatement.containsKey(txn)) ? transactionToStatement.get(txn) : 0;
		transactionToStatement.put(txn, stmt_counts + 1);
		Query_Statement result = new Query_Statement(stmt_counts, q);
		ifStatementMap.get(txn + "-if-" + if_id).addStatementInIf(result);
		return result;
	}

	public Query_Statement addQueryStatementInElse(String txn, int if_id, Query q) {
		int stmt_counts = (transactionToStatement.containsKey(txn)) ? transactionToStatement.get(txn) : 0;
		transactionToStatement.put(txn, stmt_counts + 1);
		Query_Statement result = new Query_Statement(stmt_counts, q);
		ifStatementMap.get(txn + "-if-" + if_id).addStatementInElse(result);
		return result;
	}

	public If_Statement addIfStatementInIf(String txn, int if_id, Expression c) {
		int if_stmt_counts = (transactionToIf.containsKey(txn)) ? transactionToIf.get(txn) : 0;
		transactionToIf.put(txn, if_stmt_counts + 1);
		If_Statement result = new If_Statement(if_stmt_counts, c, new ArrayList<Statement>());
		ifStatementMap.put(txn + "-if-" + if_stmt_counts, result);
		ifStatementMap.get(txn + "-if-" + if_id).addStatementInIf(result);
		return result;
	}

	public If_Statement addIfStatementInElse(String txn, int if_id, Expression c) {
		int if_stmt_counts = (transactionToIf.containsKey(txn)) ? transactionToIf.get(txn) : 0;
		transactionToIf.put(txn, if_stmt_counts + 1);
		If_Statement result = new If_Statement(if_stmt_counts, c, new ArrayList<Statement>());
		ifStatementMap.put(txn + "-if-" + if_stmt_counts, result);
		ifStatementMap.get(txn + "-if-" + if_id).addStatementInElse(result);
		return result;
	}

	// create and add an empty if statement --> the enclosed statements will be
	// added later
	public If_Statement addIfStatement(String txn, Expression c) {
		int if_stmt_counts = (transactionToIf.containsKey(txn)) ? transactionToIf.get(txn) : 0;
		transactionToIf.put(txn, if_stmt_counts + 1);
		If_Statement result = new If_Statement(if_stmt_counts, c, new ArrayList<Statement>());
		ifStatementMap.put(txn + "-if-" + if_stmt_counts, result);
		trasnsactionMap.get(txn).addStatement(result);
		return result;
	}

	public Select_Query addSelectQuery(String txn, String tableName, boolean isAtomic, WHC whc, String... fieldNames) {
		Variable fresh_variable = getFreshVariable(txn);
		int select_counts = (transactionToSelectCount.containsKey(txn)) ? transactionToSelectCount.get(txn) : 0;
		transactionToSelectCount.put(txn, select_counts + 1);
		ArrayList<FieldName> fresh_field_names = new ArrayList<>();
		for (String fn : fieldNames)
			fresh_field_names.add(fieldNameMap.get(fn));
		Select_Query result = new Select_Query(select_counts, isAtomic, tableNameMap.get(tableName), fresh_field_names,
				fresh_variable, whc);
		return result;
	}

	public Update_Query addUpdateQuery(String txn, String tableName, boolean isAtomic, WHC whc) {
		int update_counts = (transactionToUpdateCount.containsKey(txn)) ? transactionToSelectCount.get(txn) : 0;
		transactionToUpdateCount.put(txn, update_counts + 1);
		Update_Query result = new Update_Query(update_counts, isAtomic, tableNameMap.get(tableName), whc);
		return result;
	}

	public Transaction addTrnasaction(String txn_name, String... args) {
		Transaction txn = new Transaction(txn_name);
		String transaction_name = txn.getName();
		this.trasnsactionMap.put(transaction_name, txn);
		transactionToArgsSetMap.put(transaction_name, new ArrayList<>());
		transactionToVariableSetMap.put(transaction_name, new ArrayList<>());
		for (String arg : args) {
			E_Arg current_arg = new E_Arg(arg);
			argsMap.put(arg, current_arg);
			txn.addArg(current_arg);
			transactionToArgsSetMap.get(transaction_name).add(current_arg);
		}
		return txn;
	}

	public Table addTable(String tn_name, FieldName... fns) {
		TableName tn = new TableName(tn_name);
		this.tableNameMap.put(tn.getName(), tn);
		Table newTable = new Table(tn, fns);
		this.tableMap.put(tn.getName(), newTable);
		for (FieldName fn : fns)
			this.fieldNameMap.put(fn.getName(), fn);
		return newTable;
	}

	/*
	 * add a table with default values for SK, PK and types
	 */
	public Table addBasicTable(String tn_name, String... fns) {
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

}
