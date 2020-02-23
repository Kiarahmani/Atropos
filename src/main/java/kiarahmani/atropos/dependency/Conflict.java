package kiarahmani.atropos.dependency;

import java.util.ArrayList;
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

	public TableName getTableName() {
		return this.table_name;
	}

	public ArrayList<FieldName> getFieldNames() {
		return this.field_names;
	}

	public Transaction getTransaction(int id) {
		if (id == 1)
			return txn1;
		else if (id == 2)
			return txn2;
		else
			assert (false);
		return null;

	}

	public Query getQuery(int id) {
		if (id == 1)
			return q1;
		else if (id == 2)
			return q2;
		else
			assert (false);
		return null;
	}

	public void setQuery1( Query q) {
		this.q1 = q;
	}
	
	public void setQuery2( Query q) {
		this.q2 = q;
	}
	
	@Override
	public String toString() {
		return "<" + txn1.getName() + "." + q1.getId() + "," + txn2.getName() + "." + q2.getId() + "," + table_name
				+ "," + this.field_names + ">";
	}

}
