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
public class Optimal_search_engine_tpcc extends Search_engine {

	private int iter;
	private int max_iter;
	private Delta[] result;

	public Optimal_search_engine_tpcc() {
		iter = 0;
		max_iter = 33;
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
			result[index++] = new INTRO_R("cust_bal_crdt", true);

			result[index++] = new INTRO_F("cust_bal_crdt", "cbc_wid", F_Type.NUM);
			result[index++] = new INTRO_F("cust_bal_crdt", "cbc_did", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("cust_bal_crdt", "cbc_cid", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("cust_bal_crdt", "cbc_uuid", F_Type.NUM, true, false);
			result[index++] = new INTRO_F("cust_bal_crdt", "cbc_bal", F_Type.NUM, false, true);

			result[index++] = new ADDPK(pu, "cust_bal_crdt", "cbc_wid");
			result[index++] = new ADDPK(pu, "cust_bal_crdt", "cbc_did");
			result[index++] = new ADDPK(pu, "cust_bal_crdt", "cbc_cid");
			result[index++] = new ADDPK(pu, "cust_bal_crdt", "cbc_uuid");
			result[index++] = new CHSK(pu, "cust_bal_crdt", "cbc_wid");

			// introduce vc between customer and cust_bal_crdt
			INTRO_VC delta_1 = new INTRO_VC(pu, "customer", "cust_bal_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
			delta_1.addKeyCorrespondenceToVC("c_wid", "cbc_wid");
			delta_1.addKeyCorrespondenceToVC("c_did", "cbc_did");
			delta_1.addKeyCorrespondenceToVC("c_id", "cbc_cid");
			delta_1.addFieldTupleToVC("c_balance", "cbc_bal");
			result[index++] = delta_1;

			/********************************************************************************/
			// introduce a CRDT table for customer delivery count
			result[index++] = new INTRO_R("cust_del_cnt_crdt", true);

			result[index++] = new INTRO_F("cust_del_cnt_crdt", "cdcc_wid", F_Type.NUM);
			result[index++] = new INTRO_F("cust_del_cnt_crdt", "cdcc_did", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("cust_del_cnt_crdt", "cdcc_cid", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("cust_del_cnt_crdt", "cdcc_uuid", F_Type.NUM, true, false);
			result[index++] = new INTRO_F("cust_del_cnt_crdt", "cdcc_del_cnt", F_Type.NUM, false, true);

			result[index++] = new ADDPK(pu, "cust_del_cnt_crdt", "cdcc_wid");
			result[index++] = new ADDPK(pu, "cust_del_cnt_crdt", "cdcc_did");
			result[index++] = new ADDPK(pu, "cust_del_cnt_crdt", "cdcc_cid");
			result[index++] = new ADDPK(pu, "cust_del_cnt_crdt", "cdcc_uuid");
			result[index++] = new CHSK(pu, "cust_del_cnt_crdt", "cdcc_wid");

			// introduce vc between customer and cust_bal_crdt
			INTRO_VC delta_2 = new INTRO_VC(pu, "customer", "cust_del_cnt_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
			delta_2.addKeyCorrespondenceToVC("c_wid", "cdcc_wid");
			delta_2.addKeyCorrespondenceToVC("c_did", "cdcc_did");
			delta_2.addKeyCorrespondenceToVC("c_id", "cdcc_cid");
			delta_2.addFieldTupleToVC("c_delivery_cnt", "cdcc_del_cnt");
			result[index++] = delta_2;

			/********************************************************************************/
			// introduce a CRDT table for stock ytd
			result[index++] = new INTRO_R("stock_ytd_crdt", true);

			result[index++] = new INTRO_F("stock_ytd_crdt", "syc_wid", F_Type.NUM);
			result[index++] = new INTRO_F("stock_ytd_crdt", "syc_iid", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("stock_ytd_crdt", "cdcc_uuid", F_Type.NUM, true, false);
			result[index++] = new INTRO_F("stock_ytd_crdt", "syc_ytd", F_Type.NUM, false, true);

			result[index++] = new ADDPK(pu, "stock_ytd_crdt", "syc_wid");
			result[index++] = new ADDPK(pu, "stock_ytd_crdt", "syc_iid");
			result[index++] = new CHSK(pu, "stock_ytd_crdt", "syc_wid");

			// introduce vc between customer and cust_bal_crdt
			INTRO_VC delta_3 = new INTRO_VC(pu, "stock", "stock_ytd_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
			delta_3.addKeyCorrespondenceToVC("s_wid", "syc_wid");
			delta_3.addKeyCorrespondenceToVC("s_iid", "syc_iid");
			delta_3.addFieldTupleToVC("s_ytd", "syc_ytd");
			result[index++] = delta_3;

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
