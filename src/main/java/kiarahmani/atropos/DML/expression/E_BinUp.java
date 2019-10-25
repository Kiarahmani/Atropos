package kiarahmani.atropos.DML.expression;

public class E_BinUp extends Expression {


	private Expression oper1, oper2;
	private BinOp op;

	public E_BinUp(BinOp o, Expression e1, Expression e2) {
		this.oper1 = e1;
		this.oper2 = e2;
		this.op = o;
	}

	@Override
	public String toString() {
		return "(" + this.oper1.toString() + BinOp.BinOpToString(this.op) + this.oper2.toString() + ")";
	};

	
}
