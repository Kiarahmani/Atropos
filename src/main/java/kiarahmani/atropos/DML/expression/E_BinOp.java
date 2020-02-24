package kiarahmani.atropos.DML.expression;

import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.constants.E_Const_Text;

public class E_BinOp extends Expression {

	public Expression oper1, oper2;
	public BinOp op;
	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public E_BinOp(BinOp o, Expression e1, Expression e2) {
		this.oper1 = e1;
		this.oper2 = e2;
		this.op = o;
	}

	@Override
	public Expression mkSnapshot() {
		return new E_BinOp(op, oper1.mkSnapshot(), oper2.mkSnapshot());
	}

	@Override
	public String toString() {
		assert (this.op != null) : " null operation";
		assert (this.oper1 != null) : "oper1 is null (op:" + this.op + ")";
		assert (this.oper2 != null) : "oper2 is null (op:" + this.op + ")";
		return "(" + this.oper1.toString() + BinOp.BinOpToString(this.op) + this.oper2.toString() + ")";
	}

	@Override
	public Expression substitute(Expression oldExp, Expression newExp) {
		if (this.isEqual(oldExp))
			return newExp;
		else {
			this.oper1 = this.oper1.substitute(oldExp, newExp);
			this.oper2 = this.oper2.substitute(oldExp, newExp);
			return this;
		}
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
	public Expression redirectProjs(Variable oldVar, FieldName oldFn, Variable newVar, FieldName newFn) {
		oper1.redirectProjs(oldVar, oldFn, newVar, newFn);
		oper2.redirectProjs(oldVar, oldFn, newVar, newFn);
		return this;

	}

	@Override
	public boolean isEqual(Expression other) {
		if (other instanceof E_BinOp) {
			E_BinOp other_e_bin = (E_BinOp) other;
			if (other_e_bin.op == this.op) {
				logger.debug("operations are the same");
				boolean ops_same = this.oper1.isEqual(other_e_bin.oper1) && this.oper2.isEqual(other_e_bin.oper2);
				boolean ops_oppose = this.oper1.isEqual(other_e_bin.oper2) && this.oper2.isEqual(other_e_bin.oper1);
				logger.debug("exps_same: " + ops_same);
				logger.debug("exps_opps: " + ops_oppose);
				if (BinOp.isCommutative(this.op))
					return ops_same || ops_oppose;
				else
					return ops_same;
			} else {
				logger.debug(" the other exp instance has a different operation: " + other_e_bin.op);
				return false; // ops are not the same
			}
		} else {
			logger.debug(" the other exp instance has a different sub class: " + other);
			return false; // exp is of a different sub class
		}
	}

}
