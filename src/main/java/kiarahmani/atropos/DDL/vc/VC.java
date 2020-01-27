/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.DDL.vc;

import java.util.ArrayList;
import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.utils.Tuple;

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
	private HashSet<Tuple<FieldName, FieldName>> fieldTuples;
	private ArrayList<VC_Constraint> vc_constraints;
	private String name;

	public FieldName getCorrespondingFN(FieldName input_fn) {
		for (Tuple<FieldName, FieldName> fnt : this.fieldTuples)
			if (fnt.x.equals(input_fn))
				return fnt.y;
			else if (fnt.y.equals(input_fn))
				return fnt.x;
		return null;
	}

	public FieldName getCorrespondingKey(FieldName input_key) {
		for (VC_Constraint vcc : this.vc_constraints)
			if (vcc.getF_1().equals(input_key))
				return vcc.getF_2();
			else if (vcc.getF_2().equals(input_key))
				return vcc.getF_1();
		return null;
	}

	public TableName getTableName(int i) {
		switch (i) {
		case 1:
			return this.T_1;
		case 2:
			return this.T_2;
		default:
			break;
		}
		assert (false) : "input must be either 1 or 2";
		return null;
	}

	public VC_Type getType() {
		return this.vc_type;
	}

	public VC_Agg get_agg() {
		return this.vc_agg;
	}

	public VC(String name, TableName T_1, TableName T_2, VC_Agg vc_agg, VC_Type vc_type) {
		this.vc_constraints = new ArrayList<VC_Constraint>();
		this.name = name;
		this.T_1 = T_1;
		this.T_2 = T_2;
		this.vc_agg = vc_agg;
		this.vc_type = vc_type;
		fieldTuples = new HashSet<>();
	}

	public void addConstraint(VC_Constraint vcc) {
		this.vc_constraints.add(vcc);
	}

	public void addFieldTuple(FieldName F1, FieldName F2) {
		this.fieldTuples.add(new Tuple<FieldName, FieldName>(F1, F2));
	}

	@Override
	public String toString() {
		String constraintsList = "", delim = "";
		String F_1 = "";
		String F_2 = "";
		for (Tuple<FieldName, FieldName> ff : fieldTuples) {
			F_1 += delim + ff.x;
			F_2 += delim + ff.y;
			delim = ",";
		}
		delim = "";
		for (VC_Constraint vcc : vc_constraints) {
			constraintsList += delim + vcc.toString();
			delim = " ∧ ";
		}
		String init_string = name + ": " + T_1.getName() + ".{" + F_1 + "}=" + this.vc_agg + "(" + T_2.getName() + ".{"
				+ F_2 + "})";
		String final_string = " (" + this.vc_type + ")";
		return init_string + " <<" + constraintsList + ">>" + final_string;
	}
}
