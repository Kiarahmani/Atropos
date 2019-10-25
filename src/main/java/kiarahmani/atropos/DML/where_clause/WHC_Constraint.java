package kiarahmani.atropos.DML.where_clause;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.Expression;

public class WHC_Constraint {
	private FieldName f;
	private BinOp op;
	private Expression exp;

	public WHC_Constraint(FieldName f, BinOp op, Expression exp) {
		this.f = f;
		this.op = op;
		this.exp = exp;
	}

	@Override
	public String toString() {
		return this.f + BinOp.BinOpToString(op) + exp.toString();
	}

}