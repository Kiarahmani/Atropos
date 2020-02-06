package kiarahmani.atropos.DML.query;

import java.util.ArrayList;
import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.E_Proj;
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

	public Expression getUpdateExpressionByFieldName(FieldName fn) {
		for (Tuple<FieldName, Expression> tp : this.update_expressions)
			if (tp.x == fn)
				return tp.y;
		return null;
	}

	public boolean isAtomic() {
		return this.isAtomic;
	}

	public ArrayList<Tuple<FieldName, Expression>> getUpdateExps() {
		return this.update_expressions;
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
		String isAtomicString = isAtomic ? "(" + po + ") ATOMIC " : "(" + po + ") ";
		String updateTuplesList = "";
		String delim = "";
		for (Tuple<FieldName, Expression> tuple : update_expressions) {
			updateTuplesList += delim + tuple.x + "=" + tuple.y;
			delim = ",";
		}
		return isAtomicString + "UPDATE" + this.id + " " + String.format("%-10s", this.tableName) + " SET "
				+ updateTuplesList + " WHERE " + this.where_clause;
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
		assert (this.path_condition != null) : "cannot return null"+this;
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

	@Override
	public HashSet<Variable> getAllRefferencedVars() {
		HashSet<Variable> result = new HashSet<>();
		result.addAll(this.where_clause.getAllRefferencedVars());
		for (Tuple<FieldName, Expression> exp : this.update_expressions)
			result.addAll(exp.y.getAllRefferencedVars());
		return result;
	}

	@Override
	public HashSet<E_Proj> getAllProjExps() {
		HashSet<E_Proj> result = new HashSet<>();
		result.addAll(this.where_clause.getAllProjExps());
		for (Tuple<FieldName, Expression> exp : this.update_expressions)
			result.addAll(exp.y.getAllProjExps());
		return result;
	}

	@Override
	public void redirectProjs(Variable oldVar, FieldName oldFn, Variable newVar, FieldName newFn) {
		for (Tuple<FieldName, Expression> fn_exp : update_expressions)
			fn_exp.y.redirectProjs(oldVar, oldFn, newVar, newFn);
		this.where_clause.redirectProjs(oldVar, oldFn, newVar, newFn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * kiarahmani.atropos.DML.query.Query#substituteExps(kiarahmani.atropos.DML.
	 * expression.Expression, kiarahmani.atropos.DML.expression.Expression)
	 */
	@Override
	public void substituteExps(Expression oldExp, Expression newExp) {
		ArrayList<Tuple<FieldName, Expression>> new_update_expressions = new ArrayList<>();
		for (Tuple<FieldName, Expression> fn_exp : update_expressions) {
			Tuple<FieldName, Expression> newTuple = new Tuple<FieldName, Expression>(fn_exp.x,
					fn_exp.y.substitute(oldExp, newExp));
			new_update_expressions.add(newTuple);
		}
		this.update_expressions = new_update_expressions;
		this.where_clause.substituteExps(oldExp, newExp);
	}
}
