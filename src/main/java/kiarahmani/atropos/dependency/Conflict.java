package kiarahmani.atropos.dependency;

import java.util.ArrayList;
import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.query.Query;

public class Conflict {
	private Query q1, q2;
	private ArrayList<FieldName> field_names;
	private TableName table_name;

	public Conflict(Query q1, Query q2, TableName table_name, ArrayList<FieldName> field_names) {
		assert (q1 != null && q2 != null && table_name != null && field_names.size() != 0);
		this.q1 = q1;
		this.q2 = q2;
		this.table_name = table_name;
		this.field_names = field_names;
	}

	@Override
	public String toString() {
		return "<" + q1.getId() + "," + q2.getId() + "," + table_name + "," + this.field_names + ">";
	}

}
