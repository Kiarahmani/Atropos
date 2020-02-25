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
		max_iter = 83;
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

			/********************************************************************************/
			// introduce a CRDT table for stock quantity
			result[index++] = new INTRO_R("stock_quantitiy_crdt", true);

			result[index++] = new INTRO_F("stock_quantitiy_crdt", "sqc_wid", F_Type.NUM);
			result[index++] = new INTRO_F("stock_quantitiy_crdt", "sqc_iid", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("stock_quantitiy_crdt", "cqcc_uuid", F_Type.NUM, true, false);
			result[index++] = new INTRO_F("stock_quantitiy_crdt", "sqc_quantity", F_Type.NUM, false, true);

			result[index++] = new ADDPK(pu, "stock_quantitiy_crdt", "sqc_wid");
			result[index++] = new ADDPK(pu, "stock_quantitiy_crdt", "sqc_iid");
			result[index++] = new CHSK(pu, "stock_quantitiy_crdt", "sqc_wid");

			// introduce vc between customer and cust_bal_crdt
			INTRO_VC delta_4 = new INTRO_VC(pu, "stock", "stock_quantitiy_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
			delta_4.addKeyCorrespondenceToVC("s_wid", "sqc_wid");
			delta_4.addKeyCorrespondenceToVC("s_iid", "sqc_iid");
			delta_4.addFieldTupleToVC("s_quantitiy", "sqc_quantity");
			result[index++] = delta_4;

			/********************************************************************************/
			// introduce a CRDT table for stock quantity
			result[index++] = new INTRO_R("stock_ord_cnt_crdt", true);

			result[index++] = new INTRO_F("stock_ord_cnt_crdt", "socc_wid", F_Type.NUM);
			result[index++] = new INTRO_F("stock_ord_cnt_crdt", "socc_iid", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("stock_ord_cnt_crdt", "cocc_uuid", F_Type.NUM, true, false);
			result[index++] = new INTRO_F("stock_ord_cnt_crdt", "socc_ord_cnt", F_Type.NUM, false, true);

			result[index++] = new ADDPK(pu, "stock_ord_cnt_crdt", "socc_wid");
			result[index++] = new ADDPK(pu, "stock_ord_cnt_crdt", "socc_iid");
			result[index++] = new CHSK(pu, "stock_ord_cnt_crdt", "socc_wid");

			// introduce vc between customer and cust_bal_crdt
			INTRO_VC delta_5 = new INTRO_VC(pu, "stock", "stock_ord_cnt_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
			delta_5.addKeyCorrespondenceToVC("s_wid", "socc_wid");
			delta_5.addKeyCorrespondenceToVC("s_iid", "socc_iid");
			delta_5.addFieldTupleToVC("s_order_cnt", "socc_ord_cnt");
			result[index++] = delta_5;

			/********************************************************************************/
			// introduce a CRDT table for c_ytd_payment
			result[index++] = new INTRO_R("cust_ytd_crdt", true);

			result[index++] = new INTRO_F("cust_ytd_crdt", "cyc_wid", F_Type.NUM);
			result[index++] = new INTRO_F("cust_ytd_crdt", "cyc_did", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("cust_ytd_crdt", "cyc_cid", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("cust_ytd_crdt", "cyc_uuid", F_Type.NUM, true, false);
			result[index++] = new INTRO_F("cust_ytd_crdt", "cyc_ytd_payment", F_Type.NUM, false, true);

			result[index++] = new ADDPK(pu, "cust_ytd_crdt", "cyc_wid");
			result[index++] = new ADDPK(pu, "cust_ytd_crdt", "cyc_did");
			result[index++] = new ADDPK(pu, "cust_ytd_crdt", "cyc_cid");
			result[index++] = new ADDPK(pu, "cust_ytd_crdt", "cyc_uuid");
			result[index++] = new CHSK(pu, "cust_ytd_crdt", "cyc_wid");

			// introduce vc between customer and cust_bal_crdt
			INTRO_VC delta_6 = new INTRO_VC(pu, "customer", "cust_ytd_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
			delta_6.addKeyCorrespondenceToVC("c_wid", "cyc_wid");
			delta_6.addKeyCorrespondenceToVC("c_did", "cyc_did");
			delta_6.addKeyCorrespondenceToVC("c_id", "cyc_cid");
			delta_6.addFieldTupleToVC("c_ytd_payment", "cyc_ytd_payment");
			result[index++] = delta_6;

			/********************************************************************************/
			// introduce a CRDT table for c_payment_cnt
			result[index++] = new INTRO_R("cust_pay_cnt_crdt", true);

			result[index++] = new INTRO_F("cust_pay_cnt_crdt", "cpcc_wid", F_Type.NUM);
			result[index++] = new INTRO_F("cust_pay_cnt_crdt", "cpcc_did", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("cust_pay_cnt_crdt", "cpcc_cid", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("cust_pay_cnt_crdt", "cpcc_uuid", F_Type.NUM, true, false);
			result[index++] = new INTRO_F("cust_pay_cnt_crdt", "cpcc_ytd_payment", F_Type.NUM, false, true);

			result[index++] = new ADDPK(pu, "cust_pay_cnt_crdt", "cpcc_wid");
			result[index++] = new ADDPK(pu, "cust_pay_cnt_crdt", "cpcc_did");
			result[index++] = new ADDPK(pu, "cust_pay_cnt_crdt", "cpcc_cid");
			result[index++] = new ADDPK(pu, "cust_pay_cnt_crdt", "cpcc_uuid");
			result[index++] = new CHSK(pu, "cust_pay_cnt_crdt", "cpcc_wid");

			// introduce vc between customer and c_payment_cnt
			INTRO_VC delta_7 = new INTRO_VC(pu, "customer", "cust_pay_cnt_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
			delta_7.addKeyCorrespondenceToVC("c_wid", "cpcc_wid");
			delta_7.addKeyCorrespondenceToVC("c_did", "cpcc_did");
			delta_7.addKeyCorrespondenceToVC("c_id", "cpcc_cid");
			delta_7.addFieldTupleToVC("c_payment_cnt", "cpcc_ytd_payment");
			result[index++] = delta_7;

			/********************************************************************************/
			// introduce a CRDT table for w_ytd
			result[index++] = new INTRO_R("warehouse_ytd_crdt", true);

			result[index++] = new INTRO_F("warehouse_ytd_crdt", "wyc_wid", F_Type.NUM);
			result[index++] = new INTRO_F("warehouse_ytd_crdt", "wyc_uuid", F_Type.NUM, true, false);
			result[index++] = new INTRO_F("warehouse_ytd_crdt", "wyc_ytd", F_Type.NUM, false, true);

			result[index++] = new ADDPK(pu, "warehouse_ytd_crdt", "wyc_wid");
			result[index++] = new ADDPK(pu, "warehouse_ytd_crdt", "wyc_uuid");
			result[index++] = new CHSK(pu, "warehouse_ytd_crdt", "wyc_wid");

			// introduce vc between customer and w_ytd
			INTRO_VC delta_8 = new INTRO_VC(pu, "warehouse", "warehouse_ytd_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
			delta_8.addKeyCorrespondenceToVC("w_id", "wyc_wid");
			delta_8.addFieldTupleToVC("w_ytd", "wyc_ytd");
			result[index++] = delta_8;

			/********************************************************************************/
			// introduce a CRDT table for d_ytd
			/*result[index++] = new INTRO_R("district_ytd_crdt", true);

			result[index++] = new INTRO_F("district_ytd_crdt", "dyc_wid", F_Type.NUM);
			result[index++] = new INTRO_F("district_ytd_crdt", "dyc_did", F_Type.NUM);
			result[index++] = new INTRO_F("district_ytd_crdt", "dyc_uuid", F_Type.NUM, true, false);
			result[index++] = new INTRO_F("district_ytd_crdt", "dyc_ytd", F_Type.NUM, false, true);

			result[index++] = new ADDPK(pu, "district_ytd_crdt", "dyc_wid");
			result[index++] = new ADDPK(pu, "district_ytd_crdt", "dyc_did");
			result[index++] = new ADDPK(pu, "district_ytd_crdt", "dyc_uuid");
			result[index++] = new CHSK(pu, "district_ytd_crdt", "dyc_wid");

			// introduce vc between customer and w_ytd
			INTRO_VC delta_9 = new INTRO_VC(pu, "district", "district_ytd_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
			delta_9.addKeyCorrespondenceToVC("d_wid", "dyc_wid");
			delta_9.addKeyCorrespondenceToVC("d_id", "dycdyc_did_wid");
			delta_9.addFieldTupleToVC("w_ytd", "wyc_wid");
			result[index++] = delta_9;
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
