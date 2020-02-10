package kiarahmani.atropos.DML.query;

import java.util.ArrayList;
import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.where_clause.WHC;

public class Select_Query extends Query {
	private TableName tableName;
	private Variable variable;
	private ArrayList<FieldName> fieldNames;
	private HashSet<FieldName> implicitlyUsed;

	public HashSet<FieldName> getImplicitlyUsed() {
		return implicitlyUsed;
	}

	public void setImplicitlyUsed(FieldName... implicitlyUsed) {
		for (FieldName fn : implicitlyUsed)
			this.implicitlyUsed.add(fn);
	}

	public void setImplicitlyUsed(HashSet<FieldName> implicitlyUsed) {
		for (FieldName fn : implicitlyUsed)
			this.implicitlyUsed.add(fn);
	}

	public Select_Query(int po, int id, boolean isAtomic, TableName tableName, ArrayList<FieldName> fieldNames,
			Variable variable, WHC whc) {
		super();
		assert (!(tableName == null));
		assert (!(fieldNames == null));
		this.id = id;
		this.kind = Kind.SELECT;
		this.tableName = tableName;
		this.fieldNames = fieldNames;
		this.variable = variable;
		this.isAtomic = isAtomic;
		this.where_clause = whc;
		this.po = po;
		this.implicitlyUsed = new HashSet<>();
	}

	public String getId() {
		return this.kind.toString() + "#" + this.id;
	}

	public boolean isAtomic() {
		return this.isAtomic;
	}

	@Override
	public String toString() {
		String fieldNamesString = "";
		String delim = "";
		for (FieldName fn : fieldNames) {
			fieldNamesString += delim + fn.toString();
			delim = ",";
		}
		fieldNamesString += ")";
		String isAtomicString = isAtomic ? "(" + po + ") ATOMIC " : "(" + po + ") ";
		return isAtomicString + "SELECT" + this.id + " (" + String.format("%-10s", fieldNamesString) + " FROM "
				+ String.format("%-10s", this.tableName) + " AS " + this.variable + " WHERE " + this.where_clause
				+ String.format("				", "PC=" + this.path_condition);

	}

	@Override
	public TableName getTableName() {
		return this.tableName;
	}

	@Override
	public boolean isWrite() {
		return this.kind != kind.SELECT;
	}

	public Variable getVariable() {
		return this.variable;
	}

	public ArrayList<FieldName> getSelectedFieldNames() {
		return this.fieldNames;
	}

	@Override
	public ArrayList<FieldName> getAccessedFieldNames() {
		ArrayList<FieldName> result = new ArrayList<>();
		for (FieldName fn : this.fieldNames)
			result.add(fn);
		for (FieldName fn : this.where_clause.getAccessedFieldNames())
			result.add(fn);
		return result;
	}

	@Override
	public ArrayList<FieldName> getWrittenFieldNames() {
		ArrayList<FieldName> result = new ArrayList<>();
		return result;
	}

	@Override
	public ArrayList<FieldName> getReadFieldNames() {
		return getAccessedFieldNames();
	}

	@Override
	public Kind getKind() {
		return this.kind;
	}

	@Override
	public WHC getWHC() {
		return this.where_clause;
	}

	@Override
	public void setPathCondition(Expression path_condition) {
		this.path_condition = path_condition;
	}

	@Override
	public Expression getPathCondition() {
		assert (this.path_condition != null) : "cannot return null";
		return this.path_condition;
	}

	@Override
	public int getPo() {
		return this.po;
	}

	@Override
	public Expression getUpdateExpressionByFieldName(FieldName fn) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.DML.query.Query#getAllRefferencedVars()
	 */
	@Override
	public HashSet<Variable> getAllRefferencedVars() {
		return where_clause.getAllRefferencedVars();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.DML.query.Query#redirectProjs(kiarahmani.atropos.DML.
	 * Variable, kiarahmani.atropos.DDL.FieldName, kiarahmani.atropos.DML.Variable,
	 * kiarahmani.atropos.DDL.FieldName)
	 */
	@Override
	public void redirectProjs(Variable oldVar, FieldName oldFn, Variable newVar, FieldName newFn) {
		this.where_clause.redirectProjs(oldVar, oldFn, newVar, newFn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.DML.query.Query#getAllProjExps()
	 */
	@Override
	public HashSet<E_Proj> getAllProjExps() {
		return this.where_clause.getAllProjExps();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * kiarahmani.atropos.DML.query.Query#substituteExps(kiarahmani.atropos.DML.
	 * expression.Expression, kiarahmani.atropos.DML.expression.Expression)
	 */
	@Override
	public void substituteExps(Expression oldExp, Expression newExp) {
		this.where_clause.substituteExps(oldExp, newExp);
	}

}
