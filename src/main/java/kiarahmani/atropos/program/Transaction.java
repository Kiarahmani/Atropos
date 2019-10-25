package kiarahmani.atropos.program;

import java.util.ArrayList;

public class Transaction {
	private String TransactionName;

	private ArrayList<Statement> statements;

	public Transaction(String name) {
		this.TransactionName = name;
		this.statements = new ArrayList<>();
	}

	public void addStatement(Statement statement) {
		this.statements.add(statement);
	}

	public void printTransaction() {
		System.out.println("transaction " + TransactionName);
		for (Statement stmt : this.statements)
			stmt.printStatemenet();
	}

}
