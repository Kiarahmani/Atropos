package kiarahmani.atropos.program.statements;

import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.program.Statement;

public class Query_Statement extends Statement {
	private Query query;
	private int id;

	public Query_Statement(int id, Query q) {
		this.query = q;
		this.id = id;
	}

	@Override
	public void printStatemenet(String indent) {
		System.out.println(indent + this.query.toString());
	}

	@Override
	public void printStatemenet() {
		System.out.println(this.query.toString());
	}

	@Override
	public String getId() {
		return "Q_Stmt#" + this.id;
	}

	public Query getQuery() {
		return this.query;
	}

}
