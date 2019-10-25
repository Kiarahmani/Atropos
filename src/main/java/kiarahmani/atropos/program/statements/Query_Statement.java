package kiarahmani.atropos.program.statements;

import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.program.Statement;

public class Query_Statement extends Statement {
	private Query query;

	public Query_Statement(Query q) {
		this.query = q;
	}

	@Override
	public void printStatemenet(String indent) {
		System.out.println(indent + this.query.toString());
	}

	@Override
	public void printStatemenet() {
		System.out.println(this.query.toString());
	}
}
