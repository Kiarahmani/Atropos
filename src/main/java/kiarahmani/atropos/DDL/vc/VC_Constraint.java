package kiarahmani.atropos.DDL.vc;

import kiarahmani.atropos.DDL.FieldName;

public class VC_Constraint {

	private FieldName F_1, F_2;

	public FieldName getF_1() {
		return this.F_1;
	}

	public FieldName getF_2() {
		return this.F_2;
	}

	/* constructor */
	public VC_Constraint(FieldName F_1, FieldName F_2) {
		this.F_1 = F_1;
		this.F_2 = F_2;
	}

	public String toString() {
		return F_1 + "=" + F_2;
	}
}