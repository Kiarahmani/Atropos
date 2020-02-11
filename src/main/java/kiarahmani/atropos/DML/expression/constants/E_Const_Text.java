package kiarahmani.atropos.DML.expression.constants;

import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.E_Const;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.Expression;

public class E_Const_Text extends E_Const {
	public String val;

	public E_Const_Text(String s) {
		this.val = s;
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
	public void redirectProjs(Variable oldVar, FieldName oldFn, Variable newVar, FieldName newFn) {

	}

	@Override
	public HashSet<E_Proj> getAllProjExps() {
		return new HashSet<>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.DML.expression.E_Const#equals(kiarahmani.atropos.DML.
	 * expression.E_Const)
	 */
	public boolean equals(E_Const_Text other) {
		return this.val.equals(other.val);
	}

	@Override
	public boolean isEqual(Expression other) {
		if (other instanceof E_Const_Text) {
			E_Const_Text other_e_arg = (E_Const_Text) other;
			return this.val.equals(other_e_arg.val);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.DML.expression.Expression#mkSnapshot()
	 */
	@Override
	public Expression mkSnapshot() {
		return new E_Const_Text(val);
	}
}
