package kiarahmani.atropos.program.statements;

import java.util.ArrayList;

import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.program.Statement;

public class If_Statement extends Statement {
	private Expression condition;
	private ArrayList<Statement> if_statements;
	private ArrayList<Statement> else_statements;

	public If_Statement(Expression c, ArrayList<Statement> if_s, ArrayList<Statement> else_s) {
		assert (c != null);
		assert (if_s != null);
		assert (else_s != null);

		this.condition = c;
		this.if_statements = if_s;
		this.else_statements = else_s;
	}

	public If_Statement(Expression c, ArrayList<Statement> if_s) {
		assert (c != null);
		assert (if_s != null);

		this.condition = c;
		this.if_statements = if_s;
		this.else_statements = new ArrayList<>();
	}

	@Override
	public void printStatemenet(String indent) {
		System.out.println(indent + "if(" + this.condition.toString() + ")");
		for (Statement stmt : if_statements)
			stmt.printStatemenet(indent + "   ");
		System.out.println(indent + "}");
		if (else_statements.size() > 0) {
			System.out.println(indent + "else {");
			for (Statement stmt : else_statements)
				stmt.printStatemenet(indent + "   ");
			System.out.println(indent + "}");
		}
	}

	@Override
	public void printStatemenet() {
		System.out.println("if(" + this.condition.toString() + "){");
		for (Statement stmt : if_statements)
			stmt.printStatemenet("   ");
		System.out.println("}");
		if (else_statements.size() > 0) {
			System.out.println("else {");
			for (Statement stmt : else_statements)
				stmt.printStatemenet("   ");
			System.out.println("}");
		}

	}
}
