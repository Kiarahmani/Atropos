package kiarahmani.atropos.DML.query;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.where_clause.WHC;

public abstract class Query {
	public enum Kind {
		SELECT, INSERT, DELETE, UPDATE
	};

	protected Kind kind;
	protected WHC where_clause;
	protected boolean isAtomic;
	protected int id;

	public abstract WHC getWHC();

	public abstract Kind getKind();

	public abstract String toString();

	public abstract String getId();

	public abstract TableName getTableName();

	public abstract boolean isWrite();

	public abstract ArrayList<FieldName> getAccessedFieldNames();

}
