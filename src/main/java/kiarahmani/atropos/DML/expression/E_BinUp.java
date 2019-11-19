package kiarahmani.atropos.DML.expression;

public class E_BinUp extends Expression {

	public Expression oper1, oper2;
	public BinOp op;

	public E_BinUp(BinOp o, Expression e1, Expression e2) {
		this.oper1 = e1;
		this.oper2 = e2;
		this.op = o;
	}

	@Override
	public String toString() {
		assert (this.op != null) : " null operation";
		assert (this.oper1 != null) : "oper1 is null (op:" + this.op + ")";
		assert (this.oper2 != null) : "oper2 is null (op:" + this.op + ")";
		return "(" + this.oper1.toString() + BinOp.BinOpToString(this.op) + this.oper2.toString() + ")";
	};

}
