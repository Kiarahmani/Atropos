package kiarahmani.atropos.program;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;

public class Table {
	private ArrayList<FieldName> fieldNames;
	private TableName name;
	private boolean crdt;
	private boolean canBeRemoved;
	public boolean isNew;
	private boolean isAllPk;

	public void setIsAllPK(boolean b) {
		this.isAllPk = b;
	}

	public boolean isAllPK() {
		return this.isAllPk;
	}

	public boolean canBeRemoved() {
		return canBeRemoved;
	}

	public void setCanBeRemoved(boolean canBeRemoved) {
		this.canBeRemoved = canBeRemoved;
	}

	public boolean isCrdt() {
		return crdt;
	}

	public void setCrdt(boolean crdt) {
		this.crdt = crdt;
	}

	public List<FieldName> getPKFields() {
		return this.fieldNames.stream().filter(fn -> fn.isPK()).collect(Collectors.toList());
	}

	public FieldName getDeltaField() {
		assert (this.crdt) : "delta field is not defined on non-crdt tables";
		for (FieldName fn : fieldNames)
			if (fn.isDelta())
				return fn;
		assert (false) : "unexpected state";
		return null;
	}

	public FieldName getUUIDField() {
		assert (this.crdt) : "delta field is not defined on non-crdt tables";
		for (FieldName fn : fieldNames)
			if (fn.isUUID())
				return fn;
		assert (false) : "unexpected state";
		return null;
	}

	public Table(TableName tn, FieldName is_alive, FieldName... fns) {
		this.isNew = false;
		this.crdt = false; // must be set explicitly
		fieldNames = new ArrayList<>();
		name = tn;
		for (FieldName fn : fns)
			this.fieldNames.add(fn);
		this.fieldNames.add(is_alive);
		canBeRemoved = true;
	}

	public void addFieldName(FieldName fn) {
		this.fieldNames.add(fn);
	}

	public void removeFieldName(FieldName fn) {
		this.fieldNames.remove(fn);
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

	public FieldName getIsAliveFN() {
		for (FieldName fn : fieldNames)
			if (fn.getName().contains("alive"))
				return fn;
		assert (false) : "unexpected state: table does not include an is_alive field";
		return null;
	}

	public FieldName getShardKey() {
		for (FieldName fn : fieldNames)
			if (fn.isSK())
				return fn;
		return null;
	}

	@Override
	public String toString() {
		boolean print_table_size = false;
		String result = "", delim = "";
		String show_size = (print_table_size)? "(" + this.fieldNames.size() + ")" : "";
		result += String.format("%-15s", this.name) + show_size + "(";
		for (FieldName fn : fieldNames) {
			result += delim + fn.toStringWithType();
			delim = ",";
		}
		result += ")";
		return result;
	}

	public Table mkSnapshot() {
		Table result = new Table(this.name, this.getIsAliveFN());
		for (FieldName fn : this.fieldNames)
			if (!fn.getName().contains("alive"))
				result.addFieldName(fn);
		result.setIsAllPK(this.isAllPk);
		result.canBeRemoved = this.canBeRemoved;
		result.isNew = this.isNew;
		return result;
	}

	public boolean is_equal(Table other) {
		return this.getTableName().equalsWith(other.getTableName());
	}

}
