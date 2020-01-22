package kiarahmani.atropos.DML.expression.constants;

import java.util.HashSet;

import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.E_Const;

public class E_Const_Bool extends E_Const {
	public  boolean val;

	public E_Const_Bool(boolean b) {
		this.val = b;
	}

	@Override
	public String toString() {
		return "" + this.val;
	}
	
	@Override
	public HashSet<Variable> getAllRefferencedVars() {
		return new HashSet<>();
	}

}
