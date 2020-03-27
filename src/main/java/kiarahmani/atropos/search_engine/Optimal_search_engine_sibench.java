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
public class Optimal_search_engine_sibench extends Search_engine {

	private int iter;
	private int max_iter;
	private Delta[] result;

	public Optimal_search_engine_sibench() {
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
			result[index++] = new INTRO_R("sitest_value_crdt", true);
			result[index++] = new INTRO_F("sitest_value_crdt", "svc_id", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("sitest_value_crdt", "svc_uuid", F_Type.NUM, true, false);
			result[index++] = new INTRO_F("sitest_value_crdt", "svc_val", F_Type.NUM, false, true);

			result[index++] = new ADDPK(pu, "sitest_value_crdt", "svc_id");
			result[index++] = new ADDPK(pu, "sitest_value_crdt", "svc_uuid");
			result[index++] = new CHSK(pu, "sitest_value_crdt", "svc_id");

			// introduce vc between customer and cust_bal_crdt
			INTRO_VC delta_1 = new INTRO_VC(pu, "sitest", "sitest_value_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
			delta_1.addKeyCorrespondenceToVC("id", "svc_id");
			delta_1.addFieldTupleToVC("value", "svc_val");
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
