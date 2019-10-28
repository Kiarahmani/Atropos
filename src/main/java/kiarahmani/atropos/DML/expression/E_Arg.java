package kiarahmani.atropos.DML.expression;

public class E_Arg extends Expression {
	private String arg_name;

	public E_Arg(String name) {
		this.arg_name = name;
	}

	@Override
	public String toString() {
		return "" + this.arg_name;
	}

}
