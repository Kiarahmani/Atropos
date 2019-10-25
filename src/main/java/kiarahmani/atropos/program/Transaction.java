package kiarahmani.atropos.program;

import java.util.ArrayList;

import kiarahmani.atropos.DML.expression.E_Arg;

public class Transaction {
	private String TransactionName;
	private ArrayList<Statement> statements;
	private ArrayList<E_Arg> args;

	public Transaction(String name) {
		this.TransactionName = name;
		this.statements = new ArrayList<>();
		this.args = new ArrayList<>();
	}

	public void addStatement(Statement statement) {
		this.statements.add(statement);
	}

	public void addArg(E_Arg a) {
		this.args.add(a);
	}

	public void printTransaction() {
		System.out.print(TransactionName + "(");
		String delim = "";
		for (E_Arg a : this.args) {
			System.out.print(delim + a.toString());
			delim = ", ";
		}

		System.out.println("){");
		for (Statement stmt : this.statements)
			stmt.printStatemenet("  ");
		System.out.println("}\n\n");
	}

}
