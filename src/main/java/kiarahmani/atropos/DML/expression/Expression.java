package kiarahmani.atropos.DML.expression;

import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.Variable;

public abstract class Expression {
	public abstract String toString();

	public abstract HashSet<Variable> getAllRefferencedVars();

	public abstract void redirectProjs(Variable oldVar, FieldName oldFn, Variable newVar, FieldName newFn);

	public abstract Expression substitute(Expression oldExp, Expression newExp);

	/**
	 * @return
	 */
	public abstract HashSet<E_Proj> getAllProjExps();

	public abstract boolean isEqual(Expression other);

	public abstract Expression mkSnapshot();

}
