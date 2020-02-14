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
public class Optimal_search_engine extends Search_engine {

	@Override
	public Delta[] nextRefactorings(Program_Utils pu) {
		Delta[] result = new Delta[20];
		// introduce new fields
		int index = 0;
		result[index++] = new INTRO_F("accounts", "a_check_bal", F_Type.NUM);
		result[index++] = new INTRO_F("accounts", "a_save_bal", F_Type.NUM);

		// introduce vc between checking and accounts
		INTRO_VC delta_3 = new INTRO_VC(pu, "checking", "accounts", VC_Agg.VC_ID, VC_Type.VC_OTO);
		delta_3.addKeyCorrespondenceToVC("c_custid", "a_custid");
		delta_3.addFieldTupleToVC("c_bal", "a_check_bal");
		result[index++] = delta_3;

		// introduce vc between savings and accounts
		INTRO_VC delta_4 = new INTRO_VC(pu, "savings", "accounts", VC_Agg.VC_ID, VC_Type.VC_OTO);
		delta_4.addKeyCorrespondenceToVC("s_custid", "a_custid");
		delta_4.addFieldTupleToVC("s_bal", "a_save_bal");
		result[index++] = delta_4;

		// introduce a CRDT table for checking balance
		result[index++] = new INTRO_R("checkin_bal_crdt", true);

		// introduce new fields in checkin_bal_crdt
		result[index++] = new INTRO_F("checkin_bal_crdt", "cbc_custid", F_Type.NUM);
		result[index++] = new INTRO_F("checkin_bal_crdt", "cbc_uuids", F_Type.NUM, true, false);
		result[index++] = new INTRO_F("checkin_bal_crdt", "cbc_bal", F_Type.NUM, false, true);

		result[index++] = new ADDPK(pu, "checkin_bal_crdt", "cbc_custid");
		result[index++] = new ADDPK(pu, "checkin_bal_crdt", "cbc_uuids");
		result[index++] = new CHSK(pu, "checkin_bal_crdt", "cbc_custid");

		// introduce vc between accounts and checkin_bal_crdt
		INTRO_VC delta_12 = new INTRO_VC(pu, "accounts", "checkin_bal_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
		delta_12.addKeyCorrespondenceToVC("a_custid", "cbc_custid");
		delta_12.addFieldTupleToVC("a_check_bal", "cbc_bal");
		result[index++] = delta_12;

		 // introduce a CRDT table for savings balance
		 
		result[index++] = new INTRO_R("savings_bal_crdt", true);

		// introduce new fields in checkin_bal_crdt
		result[index++] = new INTRO_F("savings_bal_crdt", "sbc_custid", F_Type.NUM);
		result[index++] = new INTRO_F("savings_bal_crdt", "sbc_uuids", F_Type.NUM, true, false);
		result[index++] = new INTRO_F("savings_bal_crdt", "sbc_bal", F_Type.NUM, false, true);

		result[index++] = new ADDPK(pu, "savings_bal_crdt", "sbc_custid");
		result[index++] = new ADDPK(pu, "savings_bal_crdt", "sbc_uuids");
		result[index++] = new CHSK(pu, "savings_bal_crdt", "sbc_custid");

		// introduce vc between accounts and checkin_bal_crdt
		INTRO_VC delta_212 = new INTRO_VC(pu, "accounts", "savings_bal_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
		delta_212.addKeyCorrespondenceToVC("a_custid", "sbc_custid");
		delta_212.addFieldTupleToVC("a_save_bal", "sbc_bal");
		result[index++] = delta_212;

		return result;
	}

	@Override
	public boolean reset(Program_Utils pu) {
		// TODO Auto-generated method stub
		return false;
	}

}
