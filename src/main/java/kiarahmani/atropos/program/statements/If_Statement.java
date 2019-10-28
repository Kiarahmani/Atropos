package kiarahmani.atropos.program.statements;

import java.util.ArrayList;

import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.program.Statement;

public class If_Statement extends Statement {
	private Expression condition;
	private ArrayList<Statement> if_statements;
	private ArrayList<Statement> else_statements;
	private int id;

	public If_Statement(int id, Expression c, ArrayList<Statement> if_s, ArrayList<Statement> else_s) {
		assert (c != null);
		assert (if_s != null);
		assert (else_s != null);

		this.id = id;
		this.condition = c;
		this.if_statements = if_s;
		this.else_statements = else_s;
	}

	public If_Statement(int id, Expression c, ArrayList<Statement> if_s) {
		assert (c != null);
		assert (if_s != null);

		this.id = id;
		this.condition = c;
		this.if_statements = if_s;
		this.else_statements = new ArrayList<>();
	}

	public void addStatementInIf(Statement stmt) {
		this.if_statements.add(stmt);
	}

	public void addStatementInElse(Statement stmt) {
		this.else_statements.add(stmt);
	}

	@Override
	public void printStatemenet(String indent) {
		System.out.println(indent + "IF" + this.id + " (" + this.condition.toString() + "){");
		for (Statement stmt : if_statements)
			stmt.printStatemenet(indent + "   ");
		System.out.println(indent + "}");
		if (else_statements.size() > 0) {
			System.out.println(indent + "ELSE {");
			for (Statement stmt : else_statements)
				stmt.printStatemenet(indent + "   ");
			System.out.println(indent + "}");
		}
	}

	@Override
	public void printStatemenet() {
		System.out.println("IF" + this.id + " (" + this.condition.toString() + "){");
		for (Statement stmt : if_statements)
			stmt.printStatemenet("   ");
		System.out.println("}");
		if (else_statements.size() > 0) {
			System.out.println("ELSE {");
			for (Statement stmt : else_statements)
				stmt.printStatemenet("   ");
			System.out.println("}");
		}

	}

	public ArrayList<Statement> getIfStatements() {
		return this.if_statements;
	}

	public ArrayList<Statement> getElseStatements() {
		assert (else_statements.size() > 0) : "cannot return empty list";
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
		return "If_Stmt#" + this.id ;
	}
}
