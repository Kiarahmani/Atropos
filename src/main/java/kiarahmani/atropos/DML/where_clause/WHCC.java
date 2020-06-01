package kiarahmani.atropos.DML.where_clause;

import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.Expression;

public class WHCC {
	private TableName tn;
	private FieldName f;
	private BinOp op;
	private Expression exp;
	private boolean alive_constraint;
	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public WHCC(TableName tn, FieldName f, BinOp op, Expression exp) {
		this.tn = tn;
		this.f = f;
		this.op = op;
		this.exp = exp;
		this.alive_constraint = this.f.getName().contains("alive");
	}

	public WHCC mkSnapshot() {
		return new WHCC(this.tn, this.f, this.op, this.exp.mkSnapshot());
	}

	public boolean isAliveConstraint() {
		return alive_constraint;
	}

	public TableName getTableName() {
		return this.tn;
	}

	public Expression getExpression() {
		return this.exp;
	}

	public BinOp getOp() {
		return this.op;
	}

	public FieldName getFieldName() {
		return this.f;
	}

	public boolean isEqual(WHCC other) {
		// logger.debug(this.f + " =? " + other.f + " (" + this.f.equals(other.f) +
		// ")");
		// logger.debug(this.op + " =? " + other.op + " (" + (this.op == other.op) +
		// ")");
		// logger.debug(this.exp + " =? " + other.exp + " (" +
		// this.exp.isEqual(other.exp) + ")");
		return this.f.equals(other.f) && this.op == other.op && this.exp.isEqual(other.exp);

	}

	@Override
	public String toString() {
		return this.f + BinOp.BinOpToString(op) + exp.toString();
	}

	public HashSet<Variable> getAllRefferencedVars() {
		return this.exp.getAllRefferencedVars();
	}

	public void redirectProjs(Variable oldVar, FieldName oldFn, Variable newVar, FieldName newFn) {
		this.exp.redirectProjs(oldVar, oldFn, newVar, newFn);
	}

	public void substituteExps(Expression oldExp, Expression newExp) {
		this.exp = this.exp.substitute(oldExp, newExp);
	}

	/**
	 * @return
	 */
	public HashSet<E_Proj> getAllProjExps() {
		return this.exp.getAllProjExps();
	}

}