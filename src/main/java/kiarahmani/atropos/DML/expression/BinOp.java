package kiarahmani.atropos.DML.expression;

public enum BinOp {
	PLUS, MINUS, MULT, DIV, AND, OR, GT, LT, EQ;

	public static boolean isCommutative(BinOp binop) {
		switch (binop) {
		case PLUS:
			return true;
		case MINUS:
			return false;
		case MULT:
			return true;
		case DIV:
			return false;
		case AND:
			return true;
		case OR:
			return true;
		case GT:
			return false;
		case LT:
			return false;
		case EQ:
			return true;
		default:
			assert false;
		}
		return false;
	}

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
