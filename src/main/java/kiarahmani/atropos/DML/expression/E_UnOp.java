package kiarahmani.atropos.DML.expression;

import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.Variable;

public class E_UnOp extends Expression {
	public enum UnOp {
		NOT
	}

	public UnOp un_op;
	public Expression exp;

	public E_UnOp(UnOp un_op, Expression exp) {
		this.un_op = un_op;
		this.exp = exp;
	}

	@Override
	public String toString() {
		return UnOpToString(this.un_op) + "(" + this.exp.toString() + ")";
	};

	private String UnOpToString(UnOp unop) {
		switch (unop) {
		case NOT:
			return "¬";
		default:
			assert false;
		}
		return "";
	}

	@Override
	public HashSet<Variable> getAllRefferencedVars() {
		return exp.getAllRefferencedVars();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * kiarahmani.atropos.DML.expression.Expression#redirectProjs(kiarahmani.atropos
	 * .DML.Variable, kiarahmani.atropos.DML.Variable,
	 * kiarahmani.atropos.DDL.FieldName)
	 */
	@Override
	public void redirectProjs(Variable oldVar, FieldName oldFn, Variable newVar, FieldName newFn) {
		exp.redirectProjs(oldVar, oldFn, newVar, newFn);
	}

	@Override
	public HashSet<E_Proj> getAllProjExps() {
		return this.exp.getAllProjExps();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * kiarahmani.atropos.DML.expression.Expression#isEqual(kiarahmani.atropos.DML.
	 * expression.Expression)
	 */
	@Override
	public boolean isEqual(Expression other) {
		if (other instanceof E_UnOp)
			return exp.isEqual(((E_UnOp) other).exp);
		else
			return false;
	}

	@Override
	public Expression substitute(Expression oldExp, Expression newExp) {
		if (this.isEqual(oldExp))
			return newExp;
		else {
			this.exp = this.exp.substitute(oldExp, newExp);
			return this;
		}
	}
}
