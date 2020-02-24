package kiarahmani.atropos.DML.expression;

import java.util.HashSet;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.Variable;

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

	@Override
	public HashSet<Variable> getAllRefferencedVars() {
		return new HashSet<>();
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
		return this;
	}

	@Override
	public HashSet<E_Proj> getAllProjExps() {
		return new HashSet<>();
	}

	@Override
	public boolean isEqual(Expression other) {
		if (other instanceof E_Arg) {
			E_Arg other_e_arg = (E_Arg) other;
			return this.arg_name.equals(other_e_arg.arg_name)
					&& this.transactionName.equals(other_e_arg.transactionName);
		} else
			return false; // exp is of a different sub class
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * kiarahmani.atropos.DML.expression.Expression#substitute(kiarahmani.atropos.
	 * DML.expression.Expression, kiarahmani.atropos.DML.expression.Expression)
	 */
	@Override
	public Expression substitute(Expression oldExp, Expression newExp) {
		if (this.isEqual(oldExp))
			return newExp;
		else
			return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.DML.expression.Expression#mkSnapshot()
	 */
	@Override
	public Expression mkSnapshot() {
		return this;
	}
}
