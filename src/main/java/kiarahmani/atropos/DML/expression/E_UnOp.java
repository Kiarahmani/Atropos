package kiarahmani.atropos.DML.expression;

import java.util.HashSet;

import kiarahmani.atropos.DML.Variable;

public class E_UnOp extends Expression {
	public enum UnOp {
		NOT
	}

	public UnOp un_op;
	public Expression exp;

	public E_UnOp(UnOp un_op, Expression exp) {
		this.un_op = un_op;
		this.exp = exp;
	}

	@Override
	public String toString() {
		return UnOpToString(this.un_op) + "(" + this.exp.toString() + ")";
	};

	private String UnOpToString(UnOp unop) {
		switch (unop) {
		case NOT:
			return "¬";
		default:
			assert false;
		}
		return "";
	}
	
	@Override
	public HashSet<Variable> getAllRefferencedVars() {
		return exp.getAllRefferencedVars();
	};
}
