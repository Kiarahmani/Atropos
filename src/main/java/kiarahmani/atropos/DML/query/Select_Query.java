package kiarahmani.atropos.DML.query;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.where_clause.WHC;

public class Select_Query extends Query {
	private TableName tableName;
	private Variable variable;
	private ArrayList<FieldName> fieldNames;

	public Select_Query(int id, boolean isAtomic, TableName tableName, ArrayList<FieldName> fieldNames,
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
	}

	public String getId() {
		return this.kind.toString() + this.id;
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
		return isAtomicString + "SELECT (" + fieldNamesString + ") FROM " + this.tableName + " AS " + this.variable
				+ " WHERE " + this.where_clause;
	}
}
