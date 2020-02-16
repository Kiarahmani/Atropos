/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.deltas;

import java.util.ArrayList;
import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.vc.VC;
import kiarahmani.atropos.DDL.vc.VC.VC_Agg;
import kiarahmani.atropos.DDL.vc.VC.VC_Type;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.UPDATE_Duplicator;
import kiarahmani.atropos.utils.Program_Utils;

public class INTRO_VC extends Delta {
	private VC vc;
	private Program_Utils pu;
	private String name;
	private ArrayList<UPDATE_Duplicator> applied_update_dups;

	public VC getVC() {
		return this.vc;
	}

	public void addAppliedUpDup(UPDATE_Duplicator ud) {
		this.applied_update_dups.add(ud);
	}

	public ArrayList<UPDATE_Duplicator> getAppliedUpDup() {
		return this.applied_update_dups;
	}

	public INTRO_VC(Program_Utils pu, String T_1, String T_2, VC_Agg vc_agg, VC_Type vc_type) {
		name = "vc_" + pu.getVCCnt();
		vc = new VC(name, T_1, T_2, vc_agg, vc_type);
		pu.mkVC(vc);
		this.pu = pu;
		applied_update_dups = new ArrayList<>();
	}

	/**
	 * @param string
	 * @param string2
	 */
	public void addKeyCorrespondenceToVC(String string, String string2) {
		pu.addKeyCorrespondenceToVC(this.vc.getName(), string, string2);
	}

	/**
	 * @param string
	 * @param string2
	 */
	public void addFieldTupleToVC(String string, String string2) {
		pu.addFieldTupleToVC(this.vc.getName(), string, string2);
	}

	public void addFieldTupleToVC(FieldName fn1, FieldName fn2) {
		pu.addFieldTupleToVC(this.vc.getName(), fn1, fn2);
	}

	
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.refactoring_engine.deltas.Delta#getDesc()
	 */
	@Override
	public String getDesc() {
		return this.name + " (between " + vc.getTableName(pu, 1).getName() + " and " + vc.getTableName(pu, 2).getName()
				+ ") added to the schema";
	}

}
