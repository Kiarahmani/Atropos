package kiarahmani.atropos.program;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;

public class Table {
	private ArrayList<FieldName> fieldNames;
	private TableName name;

	public List<FieldName> getPKFields() {
		return this.fieldNames.stream().filter(fn -> fn.isPK()).collect(Collectors.toList());
	}

	public Table(TableName tn, FieldName is_alive, FieldName... fns) {
		fieldNames = new ArrayList<>();
		name = tn;
		for (FieldName fn : fns)
			this.fieldNames.add(fn);
		this.fieldNames.add(is_alive);

	}

	public ArrayList<FieldName> getFieldNames() {
		return this.fieldNames;
	}

	public TableName getTableName() {
		return this.name;
	}

	public Table(TableName tn, ArrayList<FieldName> fns) {
		fieldNames = new ArrayList<>();
		name = tn;
		for (FieldName fn : fns)
			this.fieldNames.add(fn);
	}

	@Override
	public String toString() {
		String result = "", delim = "";
		result += String.format("%-9s", this.name) + "(";
		for (FieldName fn : fieldNames) {
			result += delim + fn.toStringWithType();
			delim = ",";
		}
		result += ")";
		return result;
	}
}
