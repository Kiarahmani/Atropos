package kiarahmani.atropos.DML.query;

import java.util.ArrayList;
import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.where_clause.WHC;

public abstract class Query {
	public enum Kind {
		SELECT, INSERT, DELETE, UPDATE
	};

	protected Expression path_condition;
	protected Kind kind;
	protected WHC where_clause;
	protected boolean isAtomic;
	protected int id;
	protected int po;

	public void updatePO(int newPO) {
		this.po = newPO;
	}

	public abstract Expression getUpdateExpressionByFieldName(FieldName fn);

	public abstract void setPathCondition(Expression path_condition);

	public abstract int getPo();

	public abstract Expression getPathCondition();

	public abstract WHC getWHC();

	public abstract Kind getKind();

	public abstract String toString();

	public abstract String getId();

	public abstract TableName getTableName();

	public abstract boolean isWrite();

	public abstract ArrayList<FieldName> getAccessedFieldNames();

	public abstract ArrayList<FieldName> getWrittenFieldNames();

	public abstract ArrayList<FieldName> getReadFieldNames();

	public abstract HashSet<Variable> getAllRefferencedVars();

	public abstract  HashSet<E_Proj> getAllProjExps();

	/*
	 * 
	 * REFACTORING OPERATIONS
	 * 
	 */
	public abstract void redirectProjs(Variable oldVar, FieldName oldFn, Variable newVar, FieldName newFn);

}
