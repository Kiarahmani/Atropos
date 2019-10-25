package kiarahmani.atropos.DML.expression.constants;

import kiarahmani.atropos.DML.expression.E_Const;

public class E_Const_Bool extends E_Const {
	private boolean val;

	public E_Const_Bool(boolean b) {
		this.val = b;
	}

	@Override
	public String toString() {
		return "" + this.val;
	}
}
