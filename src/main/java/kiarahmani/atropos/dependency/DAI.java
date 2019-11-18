package kiarahmani.atropos.dependency;

import java.util.ArrayList;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.program.Transaction;

public class DAI {
	private Query q1, q2;
	private Transaction txn;
	private ArrayList<FieldName> field_names1, field_names2;

	public DAI(Transaction txn, Query q1, ArrayList<FieldName> field_names1, Query q2,
			ArrayList<FieldName> field_names2) {
		assert (q1 != null && q2 != null && field_names1.size() != 0 && field_names2.size() != 0);
		this.q1 = q1;
		this.q2 = q2;
		this.txn = txn;

		this.field_names1 = field_names1;
		this.field_names2 = field_names2;
	}

	@Override
	public String toString() {
		return "<" + txn.getName() + "." + q1.getId() + "," + this.field_names1 + ">--<" + txn.getName() + "."
				+ q2.getId() + "," + this.field_names2 + ">";
	}

	public Transaction getTransaction() {
		return this.txn;
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

}
