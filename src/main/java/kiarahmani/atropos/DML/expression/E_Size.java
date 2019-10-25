package kiarahmani.atropos.DML.expression;

import kiarahmani.atropos.DML.Variable;

public class E_Size extends Expression {

	private Variable v;

	public E_Size(Variable v) {
		this.v = v;
	}

	@Override
	public String toString() {
		return "size(" + v + ")";
	}
}
