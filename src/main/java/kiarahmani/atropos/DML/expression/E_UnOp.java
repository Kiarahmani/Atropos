package kiarahmani.atropos.DML.expression;

public class E_UnOp extends Expression {
	public enum UnOp {
		NOT
	}

	private UnOp un_op;
	private Expression exp;

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
}
