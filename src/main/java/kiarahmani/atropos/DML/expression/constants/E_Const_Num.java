package kiarahmani.atropos.DML.expression.constants;

import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.E_Const;
import kiarahmani.atropos.DML.expression.E_Proj;

public class E_Const_Num extends E_Const {
	public int val;

	public E_Const_Num(int i) {
		this.val = i;
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
	
	public boolean equals(E_Const_Num other) {
		return (this.val == other.val);
	}
}
