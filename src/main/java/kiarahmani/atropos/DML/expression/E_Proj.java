package kiarahmani.atropos.DML.expression;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.Variable;

public class E_Proj extends Expression {

	public Variable v;
	public FieldName f;
	public Expression e;

	public E_Proj(Variable v, FieldName f, Expression e) {
		this.f = f;
		this.e = e;
		this.v = v;
	}

	@Override
	public String toString() {
		return "proj(" + f + "," + v + "," + e + ")";
	}

}
