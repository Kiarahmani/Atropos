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

	
	@Override
	public Expression substitute(Expression oldExp, Expression newExp) {
		if (this.isEqual(oldExp))
			return newExp;
		else {
			this.e = this.e.substitute(oldExp, newExp);
			return this;
		}
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

	@Override
	public boolean isEqual(Expression other) {
		if (other instanceof E_Proj) {
			E_Proj other_e_proj = (E_Proj) other;
			return (v.equals(other_e_proj.v)) && (f.equals(other_e_proj.f)) && (e.isEqual(other_e_proj.e));
		} else
			return false;
	}
	

}
