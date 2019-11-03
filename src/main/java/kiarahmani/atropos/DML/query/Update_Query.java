package kiarahmani.atropos.DML.query;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.query.Query.Kind;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.utils.Tuple;

public class Update_Query extends Query {
	private TableName tableName;
	private ArrayList<Tuple<FieldName, Expression>> update_expressions;

	public Update_Query(int po, int id, boolean isAtomic, TableName tableName, WHC whc) {
		this.kind = Kind.UPDATE;
		this.id = id;
		this.isAtomic = isAtomic;
		this.where_clause = whc;
		this.tableName = tableName;
		this.update_expressions = new ArrayList<>();
		this.po = po;
	}

	public String getId() {
		return this.kind.toString() + "#" + this.id;
	}

	public void addUpdateExp(FieldName fn, Expression exp) {
		this.update_expressions.add(new Tuple<FieldName, Expression>(fn, exp));
	}

	@Override
	public String toString() {
		assert (!update_expressions.isEmpty()) : "No update list specified";
		String isAtomicString = isAtomic ? "ATOMIC " : "";
		String updateTuplesList = "";
		String delim = "";
		for (Tuple<FieldName, Expression> tuple : update_expressions) {
			updateTuplesList += delim + tuple.x + "=" + tuple.y;
			delim = ",";
		}
		return isAtomicString + "UPDATE" + this.id + " " + this.tableName + " SET " + updateTuplesList + " WHERE "
				+ this.where_clause;
	}

	@Override
	public TableName getTableName() {
		return this.tableName;
	}

	@Override
	public boolean isWrite() {
		return this.kind != kind.SELECT;
	}

	@Override
	public Expression getPathCondition() {
		assert (this.path_condition != null) : "cannot return null";
		return this.path_condition;
	}

	@Override
	public ArrayList<FieldName> getAccessedFieldNames() {
		ArrayList<FieldName> result = new ArrayList<>();
		for (Tuple<FieldName, Expression> tuple : update_expressions)
			result.add(tuple.x);
		for (FieldName fn : this.where_clause.getAccessedFieldNames())
			result.add(fn);
		return result;
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
	public int getPo() {
		return this.po;
	}

	@Override
	public ArrayList<FieldName> getWrittenFieldNames() {
		ArrayList<FieldName> result = new ArrayList<>();
		for (Tuple<FieldName, Expression> tuple : update_expressions)
			result.add(tuple.x);
		return result;
	}

	@Override
	public ArrayList<FieldName> getReadFieldNames() {
		ArrayList<FieldName> result = new ArrayList<>();
		for (FieldName fn : this.where_clause.getAccessedFieldNames())
			result.add(fn);
		return result;
	}
}
