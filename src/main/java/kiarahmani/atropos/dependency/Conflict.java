package kiarahmani.atropos.dependency;

import java.util.ArrayList;
import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.program.Transaction;

public class Conflict {
	private Query q1, q2;
	private Transaction txn1, txn2;
	private ArrayList<FieldName> field_names;
	private TableName table_name;

	public Conflict(Transaction txn1, Query q1, Transaction txn2, Query q2, TableName table_name,
			ArrayList<FieldName> field_names) {
		assert (q1 != null && q2 != null && table_name != null && field_names.size() != 0);
		this.q1 = q1;
		this.q2 = q2;
		this.txn1 = txn1;
		this.txn2 = txn2;

		this.table_name = table_name;
		this.field_names = field_names;
	}

	@Override
	public String toString() {
		return "<" + txn1.getName() + "." + q1.getId() + "," + txn2.getName() + "." + q2.getId() + "," + table_name
				+ "," + this.field_names + ">";
	}

}
