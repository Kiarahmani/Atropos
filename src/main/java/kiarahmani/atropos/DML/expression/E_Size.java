package kiarahmani.atropos.DML.expression;

import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.Variable;

public class E_Size extends Expression {

	public Variable v;

	public E_Size(Variable v) {
		this.v = v;
	}

	@Override
	public Expression mkSnapshot() {
		return new E_Size(this.v);
	}

	@Override
	public String toString() {
		return "size(" + v + ")";
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
		if (this.v.equals(oldVar)) {
			this.v = newVar;
		}
	};

	@Override
	public HashSet<E_Proj> getAllProjExps() {
		return new HashSet<>();
	}

	@Override
	public boolean isEqual(Expression other) {
		if (other instanceof E_Size)
			return v.equals(((E_Size) other).v);
		else
			return false;
	}

	@Override
	public Expression substitute(Expression oldExp, Expression newExp) {
		if (this.isEqual(oldExp))
			return newExp;
		else
			return this;
	}
}
