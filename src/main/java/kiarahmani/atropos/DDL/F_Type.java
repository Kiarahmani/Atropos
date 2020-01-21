package kiarahmani.atropos.DDL;

public enum F_Type {
	NUM, TEXT, BOOL;

	public static F_Type stringTypeToFType(String tp) {
		switch (tp.toLowerCase()) {
		case "int":
			return F_Type.NUM;
		case "string":
			return F_Type.TEXT;
		case "bool":
			return F_Type.BOOL;
		default:
			assert (false) : "unhandled string type: " + tp;
			return null;
		}
	}
}
