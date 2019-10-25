package kiarahmani.atropos.DML.query;

import kiarahmani.atropos.DML.where_clause.WHC;

public abstract class Query {
	public enum Kind {
		SELECT, INSERT, DELETE, UPDATE
	};

	protected Kind kind;
	protected WHC where_clause;
	protected boolean isAtomic;

	public abstract String toString();
}
