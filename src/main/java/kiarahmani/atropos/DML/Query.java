package kiarahmani.atropos.DML;

import kiarahmani.atropos.DML.expression.E_WHC;

public class Query {
	public enum Kind {
		SELECT, INSERT, DELETE, UPDATE
	};
	
	private E_WHC where_clause;
	private boolean isAtomic;
}
