package kiarahmani.atropos.DML.expression;

import java.util.HashSet;

import kiarahmani.atropos.DML.Variable;

public class E_UUID extends Expression {

	@Override
	public String toString() {
		return "uuid";
	}
	
	@Override
	public HashSet<Variable> getAllRefferencedVars() {
		return new HashSet<>();
	}

}
