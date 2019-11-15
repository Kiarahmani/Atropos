package kiarahmani.atropos.DML.query;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.where_clause.WHC;

public class Select_Query extends Query {
	private TableName tableName;
	private Variable variable;
	private ArrayList<FieldName> fieldNames;

	public Select_Query(int po, int id, boolean isAtomic, TableName tableName, ArrayList<FieldName> fieldNames,
			Variable variable, WHC whc) {
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
	}

	public String getId() {
		return this.kind.toString() + "#" + this.id;
	}

	@Override
	public String toString() {
		String fieldNamesString = "";
		String delim = "";
		for (FieldName fn : fieldNames) {
			fieldNamesString += delim + fn.toString();
			delim = ",";
		}
		String isAtomicString = isAtomic ? "ATOMIC " : "";
		return isAtomicString + "SELECT" + this.id + " (" + fieldNamesString + ") FROM " + this.tableName + " AS "
				+ this.variable + " WHERE " + this.where_clause;
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
}
