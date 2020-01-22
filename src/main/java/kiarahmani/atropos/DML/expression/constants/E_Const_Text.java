package kiarahmani.atropos.DML.expression.constants;

import java.util.HashSet;

import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.E_Const;

public class E_Const_Text extends E_Const {
	public String val;

	public E_Const_Text(String s) {
		this.val = s;
	}

	@Override
	public String toString() {
		return "" + this.val ;
	}
	
	@Override
	public HashSet<Variable> getAllRefferencedVars() {
		return new HashSet<>();
	}


}
