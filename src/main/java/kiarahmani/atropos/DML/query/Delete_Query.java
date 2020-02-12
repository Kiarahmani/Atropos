package kiarahmani.atropos.DML.query;

import java.util.ArrayList;
import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Bool;
import kiarahmani.atropos.DML.query.Query.Kind;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.utils.Tuple;

public class Delete_Query extends Query {
	private TableName tableName;
	private ArrayList<Tuple<FieldName, Expression>> update_expressions;
	FieldName is_alive;

	public Delete_Query(int po, int id, boolean isAtomic, TableName tableName, FieldName is_alive, WHC whc) {
		super();
		this.kind = Kind.DELETE;
		this.po = po;
		this.id = id;
		this.isAtomic = isAtomic;
		this.where_clause = whc;
		this.tableName = tableName;
		this.update_expressions = new ArrayList<>();
		this.update_expressions.add(new Tuple<FieldName, Expression>(is_alive, new E_Const_Bool(false)));
		this.is_alive = is_alive;
	}

	@Override
	public Query mkSnapshot() {
		Query result = new Delete_Query(this.po, this.id, this.isAtomic, this.tableName, this.is_alive,
				this.where_clause.mkSnapshot());
		result.path_condition = this.path_condition.mkSnapshot();
		result.canBeRemoved = this.canBeRemoved;
		result.kind = this.kind;
		return result;
	}

	public Expression getUpdateExpressionByFieldName(FieldName fn) {
		return update_expressions.get(0).y;
	}

	public String getId() {
		return this.kind.toString() + "#" + this.id;
	}

	public void addUpdateExp(FieldName fn, Expression exp) {
		assert (false) : "must not add any expression to a delete query";
	}

	@Override
	public String toString() {
		assert (!update_expressions.isEmpty()) : "No update list specified";
		String isAtomicString = isAtomic ? "(" + po + ") ATOMIC " : "(" + po + ") ";
		return isAtomicString + "DELETE" + this.id + " FROM " + String.format("%-10s", this.tableName) + " WHERE "
				+ this.where_clause; // + " PC=" + this.path_condition;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.DML.query.Query#getAllRefferencedVars()
	 */
	@Override
	public HashSet<Variable> getAllRefferencedVars() {
		return this.where_clause.getAllRefferencedVars();
	}

	@Override
	public HashSet<E_Proj> getAllProjExps() {
		return this.where_clause.getAllProjExps();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.DML.query.Query#redirectProjs(kiarahmani.atropos.DML.
	 * Variable, kiarahmani.atropos.DDL.FieldName, kiarahmani.atropos.DML.Variable,
	 * kiarahmani.atropos.DDL.FieldName)
	 */
	@Override
	public void redirectProjs(Variable oldVar, FieldName oldFn, Variable newVar, FieldName newFn) {
		this.where_clause.redirectProjs(oldVar, oldFn, newVar, newFn);
		this.path_condition.redirectProjs(oldVar, oldFn, newVar, newFn);
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
		this.where_clause.substituteExps(oldExp, newExp);
		this.path_condition.substitute(oldExp, newExp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.DML.query.Query#mkSnapshot()
	 */

}
