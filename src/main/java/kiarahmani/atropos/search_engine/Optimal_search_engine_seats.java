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
public class Optimal_search_engine_seats extends Search_engine {

	private int iter;
	private int max_iter;
	private Delta[] result;

	public Optimal_search_engine_seats() {
		iter = 0;
		max_iter = 44;
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
			result[index++] = new INTRO_F("cust_bal_crdt", "cbc_cid", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("cust_bal_crdt", "cbc_uuid", F_Type.NUM, true, false);
			result[index++] = new INTRO_F("cust_bal_crdt", "cbc_bal", F_Type.NUM, false, true);

			result[index++] = new ADDPK(pu, "cust_bal_crdt", "cbc_cid");
			result[index++] = new ADDPK(pu, "cust_bal_crdt", "cbc_uuid");
			result[index++] = new CHSK(pu, "cust_bal_crdt", "cbc_cid");

			// introduce vc between customer and cust_bal_crdt
			INTRO_VC delta_1 = new INTRO_VC(pu, "customer", "cust_bal_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
			delta_1.addKeyCorrespondenceToVC("c_id", "cbc_cid");
			delta_1.addFieldTupleToVC("c_balance", "cbc_bal");
			result[index++] = delta_1;

			/*
			 * *****************************************************************************
			 */
			// introduce a CRDT table for customer c_iattr
			result[index++] = new INTRO_R("cust_iattr_crdt", true);
			result[index++] = new INTRO_F("cust_iattr_crdt", "cic_cid", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("cust_iattr_crdt", "cic_uuid", F_Type.NUM, true, false);
			result[index++] = new INTRO_F("cust_iattr_crdt", "cic_iattr", F_Type.NUM, false, true);

			result[index++] = new ADDPK(pu, "cust_iattr_crdt", "cic_cid");
			result[index++] = new ADDPK(pu, "cust_iattr_crdt", "cic_uuid");
			result[index++] = new CHSK(pu, "cust_iattr_crdt", "cic_iattr");

			// introduce vc between customer and cust_bal_crdt
			INTRO_VC delta_2 = new INTRO_VC(pu, "customer", "cust_iattr_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
			delta_2.addKeyCorrespondenceToVC("c_id", "cic_cid");
			delta_2.addFieldTupleToVC("c_iattr", "cic_iattr");
			result[index++] = delta_2;

			/*
			 * *****************************************************************************
			 */
			// introduce a CRDT table for customer c_sattr
			result[index++] = new INTRO_R("cust_sattr_crdt", true);
			result[index++] = new INTRO_F("cust_sattr_crdt", "csc_cid", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("cust_sattr_crdt", "csc_uuid", F_Type.NUM, true, false);
			result[index++] = new INTRO_F("cust_sattr_crdt", "csc_sattr", F_Type.NUM, false, true);

			result[index++] = new ADDPK(pu, "cust_sattr_crdt", "csc_cid");
			result[index++] = new ADDPK(pu, "cust_sattr_crdt", "csc_uuid");
			result[index++] = new CHSK(pu, "cust_sattr_crdt", "csc_sattr");

			// introduce vc between customer and cust_bal_crdt
			INTRO_VC delta_3 = new INTRO_VC(pu, "customer", "cust_sattr_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
			delta_3.addKeyCorrespondenceToVC("c_id", "csc_cid");
			delta_3.addFieldTupleToVC("c_sattr", "csc_sattr");
			result[index++] = delta_3;

			/*
			 * *****************************************************************************
			 */
			// introduce a CRDT table for frequent_flyer ff_iattr
			result[index++] = new INTRO_R("frequent_flyer_iattr_crdt", true);
			result[index++] = new INTRO_F("frequent_flyer_iattr_crdt", "ffic_cid", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("frequent_flyer_iattr_crdt", "ffic_alid", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("frequent_flyer_iattr_crdt", "ffic_uuid", F_Type.NUM, true, false);
			result[index++] = new INTRO_F("frequent_flyer_iattr_crdt", "ffic_iattr", F_Type.NUM, false, true);

			result[index++] = new ADDPK(pu, "frequent_flyer_iattr_crdt", "ffic_cid");
			result[index++] = new ADDPK(pu, "frequent_flyer_iattr_crdt", "ffic_alid");
			result[index++] = new ADDPK(pu, "frequent_flyer_iattr_crdt", "ffic_uuid");
			result[index++] = new CHSK(pu, "frequent_flyer_iattr_crdt", "ffic_iattr");

			// introduce vc between customer and cust_bal_crdt
			INTRO_VC delta_4 = new INTRO_VC(pu, "frequent_flyer", "frequent_flyer_iattr_crdt", VC_Agg.VC_SUM,
					VC_Type.VC_OTM);
			delta_4.addKeyCorrespondenceToVC("ff_c_id", "ffic_cid");
			delta_4.addKeyCorrespondenceToVC("ff_al_id", "ffic_alid");
			delta_4.addFieldTupleToVC("ff_iattr", "ffic_iattr");
			result[index++] = delta_4;

			/*
			 * *****************************************************************************
			 */
			// introduce a CRDT table for frequent_flyer ff_sattr
			result[index++] = new INTRO_R("frequent_flyer_sattr_crdt", true);
			result[index++] = new INTRO_F("frequent_flyer_sattr_crdt", "ffsc_cid", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("frequent_flyer_sattr_crdt", "ffsc_alid", F_Type.NUM, false, false);
			result[index++] = new INTRO_F("frequent_flyer_sattr_crdt", "ffsc_uuid", F_Type.NUM, true, false);
			result[index++] = new INTRO_F("frequent_flyer_sattr_crdt", "ffsc_sattr", F_Type.NUM, false, true);

			result[index++] = new ADDPK(pu, "frequent_flyer_sattr_crdt", "ffsc_cid");
			result[index++] = new ADDPK(pu, "frequent_flyer_sattr_crdt", "ffsc_alid");
			result[index++] = new ADDPK(pu, "frequent_flyer_sattr_crdt", "ffsc_uuid");
			result[index++] = new CHSK(pu, "frequent_flyer_sattr_crdt", "ffsc_sattr");

			// introduce vc between customer and cust_bal_crdt
			INTRO_VC delta_5 = new INTRO_VC(pu, "frequent_flyer", "frequent_flyer_sattr_crdt", VC_Agg.VC_SUM,
					VC_Type.VC_OTM);
			delta_5.addKeyCorrespondenceToVC("ff_c_id", "ffsc_cid");
			delta_5.addKeyCorrespondenceToVC("ff_al_id", "ffsc_alid");
			delta_5.addFieldTupleToVC("ff_sattr", "ffsc_sattr");
			result[index++] = delta_5;

			/*
			 * *****************************************************************************
			 */
//			// introduce a CRDT table for flight's f_seats_left
//			result[index++] = new INTRO_R("flight_seats_left_crdt", true);
//			result[index++] = new INTRO_F("flight_seats_left_crdt", "fslc_fid", F_Type.NUM, false, false);
//			result[index++] = new INTRO_F("flight_seats_left_crdt", "fslc_uuid", F_Type.NUM, true, false);
//			result[index++] = new INTRO_F("flight_seats_left_crdt", "fslc_seats_left", F_Type.NUM, false, true);
//
//			result[index++] = new ADDPK(pu, "flight_seats_left_crdt", "fslc_fid");
//			result[index++] = new ADDPK(pu, "flight_seats_left_crdt", "fslc_uuid");
//			result[index++] = new CHSK(pu, "flight_seats_left_crdt", "fslc_fid");
//
//			// introduce vc between customer and cust_bal_crdt
//			INTRO_VC delta_6 = new INTRO_VC(pu, "flight", "flight_seats_left_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
//			delta_6.addKeyCorrespondenceToVC("f_id", "fslc_fid");
//			delta_6.addFieldTupleToVC("f_seats_left", "fslc_seats_left");
//			result[index++] = delta_6;

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
