package kiarahmani.atropos.program;

import java.util.ArrayList;

import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.query.Query;

public abstract class Statement {
	public abstract void printStatemenet(String indent);

	public abstract void printStatemenet();

	public abstract String getId();

	public abstract String getSimpleName();

	public abstract String[] getAllQueryIds();

	public abstract ArrayList<Query> getAllQueries();

	protected Expression path_condition;

	public abstract void setPathCondition(Expression path_condition);

	public abstract Expression getPathCondition();

	public abstract Statement mkSnapshot();

}
