package kiarahmani.atropos.DML.expression;

import java.util.HashSet;

import kiarahmani.atropos.DML.Variable;

public abstract class Expression {
	public abstract String toString();

	public abstract HashSet<Variable> getAllRefferencedVars();

	public abstract void substituteVar(Variable oldVar, Variable newVar);

}
