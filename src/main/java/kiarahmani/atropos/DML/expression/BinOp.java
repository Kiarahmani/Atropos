package kiarahmani.atropos.DML.expression;

public enum BinOp {
	PLUS, MINUS, MULT, DIV, AND, OR, GT, LT, EQ;

	public static String BinOpToString(BinOp binop) {
		switch (binop) {
		case PLUS:
			return "+";
		case MINUS:
			return "-";
		case MULT:
			return "*";
		case DIV:
			return "/";
		case AND:
			return "∧";
		case OR:
			return "∨";
		case GT:
			return ">";
		case LT:
			return "<";
		case EQ:
			return "=";
		default:
			assert false;
		}
		return "";
	}
}
