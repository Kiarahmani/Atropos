package kiarahmani.atropos.DML.expression;

import kiarahmani.atropos.DDL.F_Type;

public class E_Arg extends Expression {
	private String arg_name;
	private F_Type arg_type;
	private String transactionName;

	public E_Arg(String transactionName, String name, F_Type arg_type) {
		this.arg_name = name;
		this.arg_type = arg_type;
		this.transactionName = transactionName;
	}
	
	public String getTransactionName() {
		return this.transactionName;
	}

	public F_Type getType() {
		return this.arg_type;
	}

	public String getName() {
		return this.arg_name;
	}

	@Override
	public String toString() {
		return "" + this.arg_name;
	}

}
