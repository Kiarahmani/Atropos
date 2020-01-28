package kiarahmani.atropos.DML.expression;

import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.Variable;

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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.DML.expression.Expression#getAllRefferencedVars()
	 */
	@Override
	public HashSet<Variable> getAllRefferencedVars() {
		HashSet<Variable> result = new HashSet<>();
		result.addAll(oper1.getAllRefferencedVars());
		result.addAll(oper2.getAllRefferencedVars());
		return result;
	}

	@Override
	public HashSet<E_Proj> getAllProjExps() {
		HashSet<E_Proj> result = new HashSet<>();
		result.addAll(oper1.getAllProjExps());
		result.addAll(oper2.getAllProjExps());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * kiarahmani.atropos.DML.expression.Expression#redirectProjs(kiarahmani.atropos
	 * .DML.Variable, kiarahmani.atropos.DML.Variable,
	 * kiarahmani.atropos.DDL.FieldName)
	 */
	@Override
	public void redirectProjs(Variable oldVar, FieldName oldFn, Variable newVar, FieldName newFn) {
		oper1.redirectProjs(oldVar, oldFn, newVar, newFn);
		oper2.redirectProjs(oldVar, oldFn, newVar, newFn);

	}

}
