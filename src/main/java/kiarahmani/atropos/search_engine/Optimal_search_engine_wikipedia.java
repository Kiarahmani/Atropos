/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.search_engine;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.vc.VC.VC_Agg;
import kiarahmani.atropos.DDL.vc.VC.VC_Type;
import kiarahmani.atropos.refactoring_engine.deltas.ADDPK;
import kiarahmani.atropos.refactoring_engine.deltas.CHSK;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_F;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_R;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_VC;
import kiarahmani.atropos.utils.Program_Utils;

/**
 * @author Kiarash
 *
 */
public class Optimal_search_engine_wikipedia extends Search_engine {

	private int iter;
	private int max_iter;
	private Delta[] result;

	public Optimal_search_engine_wikipedia() {
		iter = 0;
		max_iter = 8;
	}

	public boolean hasNext() {
		return (++iter) < max_iter;
	}

	@Override
	public Delta nextRefactoring(Program_Utils pu) {
		if (iter == 0) {
			result = new Delta[max_iter];
			// introduce new fields
			int index = 0;
			// introduce a CRDT table for customer balance
			result[index++] = new INTRO_R("useracct_editcount_crdt", true);
			result[index++] = new INTRO_F("useracct_editcount_crdt", "uec_uid", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("useracct_editcount_crdt", "uec_uuid", F_Type.NUM, true, false);
			result[index++] = new INTRO_F("useracct_editcount_crdt", "uec_cnt", F_Type.NUM, false, true);

			result[index++] = new ADDPK(pu, "useracct_editcount_crdt", "uec_uid");
			result[index++] = new ADDPK(pu, "useracct_editcount_crdt", "uec_uuid");
			result[index++] = new CHSK(pu, "useracct_editcount_crdt", "uec_uid");

			// introduce vc between customer and cust_bal_crdt
			INTRO_VC delta_1 = new INTRO_VC(pu, "useracct", "useracct_editcount_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
			delta_1.addKeyCorrespondenceToVC("user_id", "uec_uid");
			delta_1.addFieldTupleToVC("user_editcount", "uec_cnt");
			result[index++] = delta_1;

			/*
			 * *****************************************************************************
			 */

		}
		// return null;
		return result[iter];
	}

	@Override
	public boolean reset(Program_Utils pu) {
		iter = 0;
		return true;
	}

}
