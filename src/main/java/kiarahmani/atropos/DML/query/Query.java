package kiarahmani.atropos.DML.query;

import java.util.ArrayList;
import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.dependency.DAI;

public abstract class Query {
	public enum Kind {
		SELECT, INSERT, DELETE, UPDATE
	};

	protected Expression path_condition;
	protected Kind kind;
	protected WHC where_clause;
	protected boolean isAtomic;
	protected boolean canBeRemoved;
	protected int id;
	protected int po;
	protected boolean is_included;
	protected ArrayList<Anml> anomalies;

	public ArrayList<FieldName> toBeExcluded;

	public void addAnml(ArrayList<FieldName> fns, DAI originalDAI) {
		if (this.anomalies == null)
			this.anomalies = new ArrayList<>();
		this.anomalies.add(new Anml(fns, originalDAI));
	}

	public ArrayList<Anml> getAnmls() {
		if (this.anomalies == null)
			return new ArrayList<>();
		return this.anomalies;
	}

	public class Anml {
		public DAI originalDAI;
		private ArrayList<FieldName> fns;

		public Anml(ArrayList<FieldName> fns, DAI originalDAI) {
			this.fns = fns;
			this.originalDAI = originalDAI;
		}

		public ArrayList<FieldName> getFns() {
			return this.fns;
		}

		public String toString() {
			return "ANML" + this.fns.toString();
		}
	}

	public boolean equals_ids(Query other) {
		return this.getId().equalsIgnoreCase(other.getId());
	}

	public boolean getIsIncluded() {
		return is_included;
	}

	public void updateWHC(WHC new_whc) {
		this.where_clause = new_whc;
	}

	public void setIsIncluded(boolean is_included) {
		this.is_included = is_included;
		this.canBeRemoved = !is_included;
	}

	public boolean canBeRemoved() {
		return this.canBeRemoved;
	}

	public Query() {
		this.canBeRemoved = true;
		this.is_included = false;
	}

	public void setcanBeRemoved(boolean r) {
		this.canBeRemoved = r;
	}

	public boolean isAtomic() {
		return this.isAtomic;
	}

	public void setAtomic(boolean atomic) {
		this.isAtomic = atomic;
	}

	public void updatePO(int newPO) {
		// System.out.println("updating po of "+this.getId() + " from "+ po + " to
		// "+newPO);
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

	public abstract HashSet<E_Proj> getAllProjExps();

	/*
	 * 
	 * REFACTORING OPERATIONS
	 * 
	 */
	public abstract void redirectProjs(Variable oldVar, FieldName oldFn, Variable newVar, FieldName newFn);

	public abstract void substituteExps(Expression oldExp, Expression newExp);

	public abstract Query mkSnapshot();

}
