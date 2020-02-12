package kiarahmani.atropos.DDL.vc;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.utils.Program_Utils;

public class VC_Constraint {

	private String F_1, F_2;

	public FieldName getF_1(Program_Utils pu) {
		return pu.getFieldName(F_1);
	}

	public FieldName getF_2(Program_Utils pu) {
		return pu.getFieldName(F_2);
	}

	/* constructor */
	public VC_Constraint(String F_1, String F_2) {
		this.F_1 = F_1;
		this.F_2 = F_2;
	}

	public String toString() {
		return F_1 + "=" + F_2;
	}

	public VC_Constraint mkSnapshot() {
		return new VC_Constraint(this.F_1, this.F_2);
	}

}