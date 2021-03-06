package kiarahmani.atropos.program.statements;

import java.util.ArrayList;
import java.util.List;

import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Bool;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.Transaction;

public class If_Statement extends Statement {
	private Expression condition;
	private ArrayList<Statement> if_statements;
	private ArrayList<Statement> else_statements;
	private int id;
	private boolean isLoop;
	private Expression iter_cnt;

	public If_Statement(int id, Expression c, ArrayList<Statement> if_s, ArrayList<Statement> else_s) {
		assert (c != null);
		assert (if_s != null);
		assert (else_s != null);

		this.id = id;
		this.condition = c;
		this.if_statements = if_s;
		this.else_statements = else_s;
		this.isLoop = false;
	}

	public If_Statement(int id, Expression c) {
		assert (c != null);
		// assert (if_s != null);

		this.id = id;
		this.condition = c;
		// this.if_statements = if_s;
		this.if_statements = new ArrayList<>();
		this.else_statements = new ArrayList<>();
		this.isLoop = false;
	}

	// will be called when loop is constructed
	public If_Statement(int id, Expression iter_cnt, boolean isLoop) {
		assert (iter_cnt != null);
		this.id = id;
		this.condition = new E_Const_Bool(true);
		this.if_statements = new ArrayList<>();
		this.else_statements = new ArrayList<>();
		this.isLoop = isLoop;
		this.iter_cnt = iter_cnt;
	}

	public void addStatementInIf(Statement stmt) {
		this.if_statements.add(stmt);
	}

	public void addStatementInElse(Statement stmt) {
		this.else_statements.add(stmt);
	}

	@Override
	public void printStatemenet(String indent) {
		if (!isLoop) {
			System.out.println(indent + "    IF" + this.id + " (" + this.condition.toString() + "){");
			for (Statement stmt : if_statements)
				stmt.printStatemenet(indent + "       ");
			System.out.println(indent + "    }");
			if (else_statements.size() > 0) {
				System.out.println(indent + "    ELSE {");
				for (Statement stmt : else_statements)
					stmt.printStatemenet(indent + "       ");
				System.out.println(indent + "    }");
			}
		} else {
			System.out.println(indent + "    ITERATE" + this.id + " (0<iter≤" + this.iter_cnt.toString() + "){");
			for (Statement stmt : if_statements)
				stmt.printStatemenet(indent + "       ");
			System.out.println(indent + "    }");
		}
	}

	@Override
	public void printStatemenet() {
		if (!isLoop) {
			System.out.println("    IF" + this.id + " (" + this.condition.toString() + "){");
			for (Statement stmt : if_statements)
				stmt.printStatemenet("       ");
			System.out.println("    }");
			if (else_statements.size() > 0) {
				System.out.println("    ELSE {");
				for (Statement stmt : else_statements)
					stmt.printStatemenet("       ");
				System.out.println("    }");
			}
		} else {
			System.out.println("    ITERATE" + this.id + " (0<iter≤" + this.iter_cnt.toString() + "){");
			for (Statement stmt : if_statements)
				stmt.printStatemenet("       ");
			System.out.println("    }");
		}

	}

	public ArrayList<Statement> getIfStatements() {
		return this.if_statements;
	}

	public ArrayList<Statement> getElseStatements() {
		// assert (else_statements.size() > 0) : "cannot return empty list";
		return this.else_statements;
	}

	public ArrayList<Statement> getAllStatements() {
		ArrayList<Statement> result = new ArrayList<>();
		result.addAll(this.if_statements);
		result.addAll(this.else_statements);
		return result;
	}

	public boolean hasElse() {
		return else_statements.size() > 0;
	}

	@Override
	public String getId() {
		return "If_Stmt#" + this.id;
	}

	public int getIntId() {
		return this.id;
	}

	@Override
	public String[] getAllQueryIds() {
		List<String> result = new ArrayList<String>();
		int size = 0;
		for (Statement if_stmt : if_statements)
			for (String s : if_stmt.getAllQueryIds()) {
				result.add(s);
				size++;
			}
		for (Statement else_stmt : else_statements)
			for (String s : else_stmt.getAllQueryIds()) {
				result.add(s);
				size++;
			}
		return result.toArray(new String[size]);
	}

	@Override
	public ArrayList<Query> getAllQueries() {
		ArrayList<Query> result = new ArrayList<>();
		for (Statement if_stmt : this.if_statements)
			result.addAll(if_stmt.getAllQueries());
		for (Statement else_stmt : this.else_statements)
			result.addAll(else_stmt.getAllQueries());
		return result;
	}

	@Override
	public void setPathCondition(Expression path_condition) {
		this.path_condition = path_condition;
	}

	@Override
	public Expression getPathCondition() {
		assert (this.path_condition != null) : "cannot return null";
		return this.path_condition;
	}

	public Expression getCondition() {
		return this.condition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.program.Statement#getSimpleName()
	 */
	@Override
	public String getSimpleName() {
		return "IF-" + this.getIntId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.program.Statement#mkSnapshot()
	 */
	@Override
	public Statement mkSnapshot() {
		if (!isLoop) {
			If_Statement result = new If_Statement(this.id, this.condition.mkSnapshot());
			for (Statement stmt : this.if_statements)
				result.addStatementInIf(stmt.mkSnapshot());
			for (Statement stmt : this.else_statements)
				result.addStatementInElse(stmt.mkSnapshot());
			return result;
		} else {
			If_Statement result = new If_Statement(this.id, this.iter_cnt.mkSnapshot(), true);
			for (Statement stmt : this.if_statements)
				result.addStatementInIf(stmt.mkSnapshot());
			return result;
		}
	}

}
