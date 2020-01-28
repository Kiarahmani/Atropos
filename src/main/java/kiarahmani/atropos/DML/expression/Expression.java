package kiarahmani.atropos.DML.expression;

import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.Variable;

public abstract class Expression {
	public abstract String toString();

	public abstract HashSet<Variable> getAllRefferencedVars();

	public abstract void redirectProjs(Variable oldVar, FieldName oldFn, Variable newVar, FieldName newFn);

	/**
	 * @return
	 */
	public abstract HashSet<E_Proj> getAllProjExps();

}
