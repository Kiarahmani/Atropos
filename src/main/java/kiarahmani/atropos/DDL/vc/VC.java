/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.DDL.vc;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.utils.Program_Utils;
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

	public VC mkSnapshot() {
		VC result = new VC(this.name, this.T_1, this.T_2, this.vc_agg, this.vc_type);
		for (Tuple<String, String> ff : this.fieldTuples)
			result.fieldTuples.add(ff);
		for (VC_Constraint vcc : this.vc_constraints)
			result.vc_constraints.add(vcc.mkSnapshot());
		return result;
	}

	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private VC_Type vc_type;
	private VC_Agg vc_agg;
	private String T_1, T_2; /* T_1 must be bound to a single record */
	private HashSet<Tuple<String, String>> fieldTuples;
	private ArrayList<VC_Constraint> vc_constraints;
	private String name;

	/*
	 * public TableName getOtherTableName(TableName tn) { if (T_1.equalsWith(tn))
	 * return T_2; else if (T_2.equalsWith(tn)) return T_1; return null; }
	 */
	public String getName() {
		return this.name;
	}

	public ArrayList<VC_Constraint> getVCC() {
		return this.vc_constraints;
	}

	public FieldName getCorrespondingFN(Program_Utils pu, FieldName input_fn) {
		for (Tuple<String, String> fnt : this.fieldTuples)
			if (fnt.x.equals(input_fn.getName()))
				return pu.getFieldName(fnt.y);
			else if (fnt.y.equals(input_fn.getName()))
				return pu.getFieldName(fnt.x);
		return null;
	}

	public FieldName getCorrespondingKey(Program_Utils pu, FieldName input_key) {
		for (VC_Constraint vcc : this.vc_constraints)
			if (vcc.getF_1(pu).equals(input_key))
				return vcc.getF_2(pu);
			else if (vcc.getF_2(pu).equals(input_key))
				return vcc.getF_1(pu);
		return null;
	}

	public TableName getTableName(Program_Utils pu, int i) {
		switch (i) {
		case 1:
			return pu.getTableName(T_1);
		case 2:
			return pu.getTableName(T_2);
		default:
			break;
		}
		assert (false) : "input must be either 1 or 2";
		return new TableName("NULL");
	}

	public VC_Type getType() {
		return this.vc_type;
	}

	public VC_Agg get_agg() {
		return this.vc_agg;
	}

	public VC(String name, String T_1, String T_2, VC_Agg vc_agg, VC_Type vc_type) {
		this.vc_constraints = new ArrayList<VC_Constraint>();
		this.name = name;
		this.T_1 = T_1;
		this.T_2 = T_2;
		this.vc_agg = vc_agg;
		this.vc_type = vc_type;
		fieldTuples = new HashSet<>();
	}

	public void addConstraint(VC_Constraint vcc) {
		// fieldTuples.add(new Tuple<FieldName, FieldName>(vcc.getF_1(), vcc.getF_2()));
		this.vc_constraints.add(vcc);
	}

	public void addFieldTuple(String F1, String F2) {
		this.fieldTuples.add(new Tuple<String, String>(F1, F2));
	}

	public boolean containsWHC(Program_Utils pu, WHC input_whc) {
		for (WHC_Constraint whcc : input_whc.getConstraints())
			if (!whcc.isAliveConstraint() && !containsWHCC(pu, whcc))
				return false;
		logger.debug("whc (" + input_whc + ") is contained in " + this);
		return true;
	}

	private boolean containsWHCC(Program_Utils pu, WHC_Constraint input_whcc) {
		for (VC_Constraint vcc : vc_constraints)
			if (vcc.getF_1(pu).equals(input_whcc.getFieldName()) || vcc.getF_2(pu).equals(input_whcc.getFieldName()))
				return true;
		logger.debug("whcc (" + input_whcc + ") is NOT contained in " + this);
		return false;
	}

	public boolean correspondsAllFns(Program_Utils pu, TableName tn, ArrayList<FieldName> fns) {
		boolean result = true;
		for (FieldName fn : fns) {
			result = result && corresponsSingleFn(pu, tn, fn);
			if (result == false) {
				logger.debug("there is no correspondence for " + fn + " in " + this);
				break;
			}
		}
		logger.debug("result is: " + result);
		return result;
	}

	public boolean corresponsSingleFn(Program_Utils pu, TableName tn, FieldName fn) {
		boolean result_x = false;
		boolean result_y = false;
		for (Tuple<String, String> tfn : this.fieldTuples) {
			logger.debug("tfn: " + tfn + "   fn: " + fn + "  tfn.x.equals(fn)=" + tfn.x.equals(fn)
					+ "   tfn.y.equals(fn)=" + pu.getFieldName(tfn.y).equals(fn));
			result_x = result_x || pu.getFieldName(tfn.x).equals(fn);
			result_y = result_y || pu.getFieldName(tfn.y).equals(fn);
		}
		if (tn.equals(pu.getTableName(T_1))) {
			logger.debug("The result of correspondence in T1(" + T_1 + ") is: " + result_x);
			return result_x;
		} else if (tn.equals(pu.getTableName(T_2))) {
			logger.debug("The result of correspondence in T2(" + T_2 + ") is: " + result_y);
			return result_y;
		}
		assert (false) : "unexpected state";
		return false;
	}

	@Override
	public String toString() {
		String constraintsList = "", delim = "";
		String F_1 = "";
		String F_2 = "";
		for (Tuple<String, String> ff : fieldTuples) {
			F_1 += delim + ff.x;
			F_2 += delim + ff.y;
			delim = ",";
		}
		delim = "";
		for (VC_Constraint vcc : vc_constraints) {
			constraintsList += delim + vcc.toString();
			delim = " ∧ ";
		}
		String init_string = name + ": " + T_1 + ".{" + F_1 + "}=" + this.vc_agg + "(" + T_2 + ".{" + F_2 + "})";
		String final_string = " (" + this.vc_type + ")";
		return init_string + " <<" + constraintsList + ">>" + final_string;
	}
}
