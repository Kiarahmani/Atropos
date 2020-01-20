/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.vc;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;

/**
 * @author Kiarash Value correpondse between data items
 */
public class VC {
	public enum VC_Type {
		VC_OTO /* one to one */, VC_OTM /* one to many */;
	}

	public enum VC_Agg {
		VC_SUM /* sum */, VC_ID /* id */;
		public String toString() {
			return (this.equals(VC_ID)) ? "id" : "sum";
		}
	}

	private VC_Type vc_type;
	private VC_Agg vc_agg;
	private TableName T_1, T_2; /* T_1 must be bound to a single record */
	private FieldName F_1, F_2;
	private ArrayList<VC_Constraint> vc_constraints;

	public VC(TableName T_1, FieldName F_1, TableName T_2, FieldName F_2, VC_Agg vc_agg, VC_Type vc_type) {
		this.vc_constraints = new ArrayList<VC_Constraint>();
		this.F_1 = F_1;
		this.F_2 = F_2;
		this.T_1 = T_1;
		this.T_2 = T_2;
		this.vc_agg = vc_agg;
		this.vc_type = vc_type;
	}

	public void addConstraint(VC_Constraint vcc) {
		this.vc_constraints.add(vcc);
	}

	@Override
	public String toString() {
		String constraintsList = "", delim = "";
		for (VC_Constraint vcc : vc_constraints) {
			constraintsList += delim + vcc.toString();
			delim = " ∧ ";
		}
		String init_string = T_1.getName() + "." + F_1.getName() + "=" + this.vc_agg + "(" + T_2.getName() + "."
				+ F_2.getName() + ")";
		return init_string + " <<" + constraintsList + ">>";
	}
}
