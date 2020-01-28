package kiarahmani.atropos.DML.expression;

import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.Variable;

public class E_Proj extends Expression {

	public Variable v;
	public FieldName f;
	public Expression e;

	public Expression getOrderExp() {
		return this.e;
	}

	public E_Proj(Variable v, FieldName f, Expression e) {
		this.f = f;
		this.e = e;
		this.v = v;
	}

	@Override
	public String toString() {
		return "proj(" + f + "," + v + "," + e + ")";
	}

	@Override
	public HashSet<Variable> getAllRefferencedVars() {
		HashSet<Variable> result = new HashSet<>();
		result.add(this.v);
		return result;
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
		if (this.v.equals(oldVar) && this.f.equals(oldFn)) {
			this.f = newFn;
			this.v = newVar;
		}
	}

	@Override
	public HashSet<E_Proj> getAllProjExps() {
		HashSet<E_Proj> result = new HashSet<>();
		result.add(this);
		return result;
	}
}
