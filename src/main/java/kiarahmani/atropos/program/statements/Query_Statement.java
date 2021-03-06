package kiarahmani.atropos.program.statements;

import java.util.ArrayList;

import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.program.Statement;

public class Query_Statement extends Statement {
	private Query query;
	private int id;

	public int getIdInt() {
		return this.id;
	}

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

	public void updatePO(int newPO) {
		this.query.updatePO(newPO);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.program.Statement#getSimpleName()
	 */
	@Override
	public String getSimpleName() {
		return this.query.getId() + "(po:" + this.query.getPo() + ")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.program.Statement#mkSnapshot()
	 */
	@Override
	public Statement mkSnapshot() {
		Query_Statement result = new Query_Statement(this.id, this.query.mkSnapshot());
		return result;
	}

}
