package kiarahmani.atropos.DML.expression;

import java.util.HashSet;

import kiarahmani.atropos.DML.Variable;

public class E_Size extends Expression {

	public Variable v;

	public E_Size(Variable v) {
		this.v = v;
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
	};
}
