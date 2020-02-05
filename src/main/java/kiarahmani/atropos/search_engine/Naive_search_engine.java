/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.search_engine;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.vc.VC.VC_Agg;
import kiarahmani.atropos.DDL.vc.VC.VC_Type;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_F;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_R;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_VC;
import kiarahmani.atropos.utils.Program_Utils;

/**
 * @author Kiarash
 *
 */
public class Naive_search_engine extends Search_engine {
	private int current_depth;
	private int max_depth;
	Program_Utils pu;
	private NameGenerator ng;

	public Naive_search_engine() {
		ng = new NameGenerator();
	}

	@Override
	public INTRO_VC nextIntroVC() {
		current_depth++;
		return new INTRO_VC(pu, "car", "makers", VC_Agg.VC_ID, VC_Type.VC_OTO);
	}

	@Override
	public INTRO_F nextIntroF() {
		return new INTRO_F("car", ng.newFieldName(), F_Type.NUM);
	}

	@Override
	public INTRO_R nextIntroR() {
		return new INTRO_R(ng.newRelationName());
	}

	@Override
	public boolean hasNext() {
		return (current_depth < max_depth);
	}

	@Override
	public void set(Program_Utils pu, int k) {
		current_depth = 0;
		max_depth = k;
		this.pu = pu;
	}

}
