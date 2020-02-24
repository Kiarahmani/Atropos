package kiarahmani.atropos.DML.expression.constants;

import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.E_Arg;
import kiarahmani.atropos.DML.expression.E_Const;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.Expression;

public class E_Const_Bool extends E_Const {
	public boolean val;

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.DML.expression.Expression#mkSnapshot()
	 */
	@Override
	public Expression mkSnapshot() {
		return new E_Const_Bool(this.val);
	}

	public E_Const_Bool(boolean b) {
		this.val = b;
	}

	@Override
	public String toString() {
		return "" + this.val;
	}

	@Override
	public HashSet<Variable> getAllRefferencedVars() {
		return new HashSet<>();
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
	public Expression redirectProjs(Variable oldVar, FieldName oldFn, Variable newVar, FieldName newFn) {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.DML.expression.Expression#getAllProjExps()
	 */
	@Override
	public HashSet<E_Proj> getAllProjExps() {
		return new HashSet<>();
	}

	public boolean equals(E_Const_Bool other) {
		return (this.val == other.val);
	}

	@Override
	public boolean isEqual(Expression other) {
		if (other instanceof E_Const_Bool) {
			E_Const_Bool other_e_arg = (E_Const_Bool) other;
			return this.val == other_e_arg.val;
		} else
			return false; // exp is of a different sub class
	}

	@Override
	public Expression substitute(Expression oldExp, Expression newExp) {
		if (this.isEqual(oldExp))
			return newExp;
		else
			return this;
	}

}
