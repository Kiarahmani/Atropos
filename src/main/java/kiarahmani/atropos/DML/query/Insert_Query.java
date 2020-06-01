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
import kiarahmani.atropos.DML.where_clause.WHCC;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.utils.Tuple;

public class Insert_Query extends Query {
	private TableName tableName;
	FieldName is_alive;
	private Table table;
	private ArrayList<Tuple<FieldName, Expression>> insert_expressions;

	public Insert_Query(int po, int id, Table table, FieldName is_alive) {
		super();
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

	@Override
	public Query mkSnapshot() {
		Insert_Query result = new Insert_Query(this.po, this.id, this.table, this.is_alive);
		result.where_clause = this.where_clause.mkSnapshot();
		result.path_condition = this.path_condition.mkSnapshot();
		result.canBeRemoved = this.canBeRemoved;
		result.kind = this.kind;
		for (Tuple<FieldName, Expression> fne : this.insert_expressions)
			result.insert_expressions.add(new Tuple<FieldName, Expression>(fne.x, fne.y.mkSnapshot()));
		result.is_included = this.is_included;
		return result;
	}

	public Expression getInsertExpressionByFieldName(FieldName fn) {
		for (Tuple<FieldName, Expression> tp : this.insert_expressions)
			if (tp.x == fn)
				return tp.y;
		assert (false) : "unexpected field name is requested for this table: " + this.tableName;
		return null;
	}

	public void addPKExp(WHCC... whccs) {
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
		String isAtomicString = isAtomic ? "(" + po + ") ATOMIC " : "(" + po + ") ";
		String updateTuplesList = "";
		String delim = "";

		for (FieldName fn : this.table.getFieldNames()) {
			if (fn.isPK())
				updateTuplesList += delim + this.where_clause.getConstraintByFieldName(fn).getExpression();
			else
				updateTuplesList += delim + /* fn.getName() + "=" + */this.getUpdateExpressionByFieldName(fn);
			delim = ",";
		}
		return isAtomicString + "INSERT" + this.id + " INTO " + String.format("%-10s", this.tableName) + " VALUES ("
				+ updateTuplesList + ")" ;//+ "	("+this.is_included+")";// + " PC=" + this.path_condition;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.DML.query.Query#getAllRefferencedVars()
	 */
	@Override
	public HashSet<Variable> getAllRefferencedVars() {
		HashSet<Variable> result = new HashSet<>();
		for (Tuple<FieldName, Expression> exp : this.insert_expressions)
			result.addAll(exp.y.getAllRefferencedVars());
		return result;
	}

	@Override
	public HashSet<E_Proj> getAllProjExps() {
		HashSet<E_Proj> result = new HashSet<>();
		for (Tuple<FieldName, Expression> exp : this.insert_expressions)
			result.addAll(exp.y.getAllProjExps());
		return result;
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
		ArrayList<Tuple<FieldName, Expression>> new_update_expressions = new ArrayList<>();
		for (Tuple<FieldName, Expression> t : this.insert_expressions) {
			Tuple<FieldName, Expression> newTuple = new Tuple<FieldName, Expression>(t.x,
					t.y.redirectProjs(oldVar, oldFn, newVar, newFn));
			new_update_expressions.add(newTuple);
		}
		this.insert_expressions = new_update_expressions;
		this.where_clause.redirectProjs(oldVar, oldFn, newVar, newFn);
		this.path_condition.redirectProjs(oldVar, oldFn, newVar, newFn);
	}

	@Override
	public void substituteExps(Expression oldExp, Expression newExp) {

		ArrayList<Tuple<FieldName, Expression>> new_update_expressions = new ArrayList<>();
		for (Tuple<FieldName, Expression> fn_exp : insert_expressions) {
			Tuple<FieldName, Expression> newTuple = new Tuple<FieldName, Expression>(fn_exp.x,
					fn_exp.y.substitute(oldExp, newExp));
			new_update_expressions.add(newTuple);
		}
		this.insert_expressions = new_update_expressions;
		this.where_clause.substituteExps(oldExp, newExp);
		this.path_condition.substitute(oldExp, newExp);
	}

}
