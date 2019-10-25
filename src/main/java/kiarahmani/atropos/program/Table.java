package kiarahmani.atropos.program;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;

public class Table {
	private ArrayList<FieldName> fieldNames;
	private TableName name;

	public Table(TableName tn, FieldName... fns) {
		fieldNames = new ArrayList<>();
		name = tn;
		for (FieldName fn : fns)
			this.fieldNames.add(fn);
	}

	@Override
	public String toString() {
		String result = "", delim = "";
		result += this.name + "(";
		for (FieldName fn : fieldNames) {
			result += delim + fn.toStringWithType();
			delim = ",";
		}
		result += ")";
		return result;
	}
}
