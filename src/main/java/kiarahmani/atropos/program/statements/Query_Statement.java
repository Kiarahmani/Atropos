package kiarahmani.atropos.program.statements;

import java.util.ArrayList;

import kiarahmani.atropos.DML.expression.Expression;
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

	@Override
	public String[] getAllQueryIds() {
		return new String[] { this.query.getId() };
	}

	@Override
	public ArrayList<Query> getAllQueries() {
		ArrayList<Query> result = new ArrayList<>();
		result.add(this.query);
		return result;
	}

	@Override
	public void setPathCondition(Expression path_condition) {
		this.path_condition = path_condition;
		this.query.setPathCondition(path_condition);
	}

	@Override
	public Expression getPathCondition() {
		assert (this.path_condition != null) : "cannot return null";
		return this.path_condition;
	}

}
