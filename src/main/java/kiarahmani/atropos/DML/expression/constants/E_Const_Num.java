package kiarahmani.atropos.DML.expression.constants;

import kiarahmani.atropos.DML.expression.E_Const;

public class E_Const_Num extends E_Const {
	public int val;

	public E_Const_Num(int i) {
		this.val = i;
	}

	@Override
	public String toString() {
		return "" + this.val ;
	}
}
