package kiarahmani.atropos.program;

import java.util.ArrayList;
import java.util.List;

import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.E_Arg;
import kiarahmani.atropos.DML.query.Query;

public class Transaction {
	private String TransactionName;
	private ArrayList<Statement> statements;
	private ArrayList<E_Arg> args;

	public ArrayList<E_Arg> getArgs() {
		return this.args;
	}

	public String getName() {
		return this.TransactionName;
	}

	public Transaction(String name) {
		this.TransactionName = name;
		this.statements = new ArrayList<>();
		this.args = new ArrayList<>();
	}

	public ArrayList<Statement> getStatements() {
		return this.statements;
	}

	public void addStatement(Statement statement) {
		this.statements.add(statement);
	}

	public ArrayList<Query> getAllQueries() {
		ArrayList<Query> result = new ArrayList<>();
		for (Statement stmt : getStatements())
			result.addAll(stmt.getAllQueries());
		return result;
	}

	public String[] getAllStmtTypes() {
		List<String> result = new ArrayList<String>();
		int size = 0;
		for (Statement stmt : getStatements())
			for (String s : stmt.getAllQueryIds()) {
				result.add(getName() + "-" + s);
				size++;
			}
		return result.toArray(new String[size]);
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
		System.out.println("}");
	}

}
