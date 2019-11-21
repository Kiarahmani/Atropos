package kiarahmani.atropos.DML.query;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Bool;
import kiarahmani.atropos.DML.query.Query.Kind;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.utils.Tuple;

public class Insert_Query extends Query {
	private TableName tableName;
	FieldName is_alive;
	private Table table;
	private ArrayList<Tuple<FieldName, Expression>> insert_expressions;

	public Insert_Query(int po, int id, Table table, FieldName is_alive) {
		this.kind = Kind.INSERT;
		this.po = po;
		this.table = table;
		this.is_alive = is_alive;
		this.id = id;
		this.isAtomic = true;
		this.where_clause = null;
		this.tableName = table.getTableName();
		this.insert_expressions = new ArrayList<>();
		this.insert_expressions.add(new Tuple<FieldName, Expression>(is_alive, new E_Const_Bool(true)));
	}

	public Expression getInsertExpressionByFieldName(FieldName fn) {
		for (Tuple<FieldName, Expression> tp : this.insert_expressions)
			if (tp.x == fn)
				return tp.y;
		assert (false) : "unexpected field name is requested for this table: " + this.tableName;
		return null;
	}

	public void addPKExp(WHC_Constraint... whccs) {
		where_clause = new WHC(whccs);
	}

	public void addInsertExp(FieldName fn, Expression exp) {
		this.insert_expressions.add(new Tuple<FieldName, Expression>(fn, exp));
	}

	public String getId() {
		return this.kind.toString() + "#" + this.id;
	}

	@Override
	public String toString() {
		assert (!insert_expressions.isEmpty()) : "No update list specified";
		String isAtomicString = isAtomic ? "(" + po + ") ATOMIC " : "(" + po + ")        ";
		String updateTuplesList = "";
		String delim = "";

		for (FieldName fn : this.table.getFieldNames()) {
			if (fn.isPK())
				updateTuplesList += delim + this.where_clause.getConstraintByFieldName(fn);
			else
				updateTuplesList += delim + fn.getName() + "=" + this.getUpdateExpressionByFieldName(fn);
			delim = ",";
		}
		return isAtomicString + "INSERT" + this.id + " INTO " + String.format("%-6s", this.tableName) + " VALUES "
				+ updateTuplesList;
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
		for (Tuple<FieldName, Expression> tuple : insert_expressions)
			result.add(tuple.x);
		// for (FieldName fn : this.where_clause.getAccessedFieldNames())
		// result.add(fn);
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
		for (Tuple<FieldName, Expression> tuple : insert_expressions)
			result.add(tuple.x);
		return result;
	}

	@Override
	public ArrayList<FieldName> getReadFieldNames() {
		ArrayList<FieldName> result = new ArrayList<>();
		// for (FieldName fn : this.where_clause.getAccessedFieldNames())
		// result.add(fn);
		return result;
	}

	@Override
	public Expression getUpdateExpressionByFieldName(FieldName fn) {
		for (Tuple<FieldName, Expression> tp : this.insert_expressions)
			if (tp.x == fn)
				return tp.y;
		return null;
	}
}