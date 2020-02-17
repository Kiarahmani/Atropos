package kiarahmani.atropos.program_generators;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_BinOp;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.E_UnOp;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.E_UnOp.UnOp;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.ProgramGenerator;
import kiarahmani.atropos.utils.Program_Utils;
import kiarahmani.atropos.utils.Program_Utils;

public class TPCCProgramGenerator implements ProgramGenerator {

	private Program_Utils pu;

	public TPCCProgramGenerator(Program_Utils pu) {
		this.pu = pu;
	}

	public Program generate(String... args) {
		String txn_name = "";
		ArrayList<String> txns = new ArrayList<>();
		for (String txn : args)
			txns.add(txn);

		/*
		 * ****** Tables ******
		 */
		// warehouse
		String table_name = "warehouse";
		String prefix = "w_";
		FieldName[] fns = new FieldName[] { new FieldName(prefix + "id", true, true, F_Type.NUM),
				new FieldName(prefix + "ytd", false, false, F_Type.NUM),
				new FieldName(prefix + "tax", false, false, F_Type.NUM),
				new FieldName(prefix + "name", false, false, F_Type.TEXT),
				new FieldName(prefix + "street", false, false, F_Type.TEXT),
				new FieldName(prefix + "city", false, false, F_Type.TEXT),
				new FieldName(prefix + "state", false, false, F_Type.NUM),
				new FieldName(prefix + "zip", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// district
		table_name = "district";
		prefix = "d_";
		fns = new FieldName[] { new FieldName(prefix + "wid", true, true, F_Type.NUM),
				new FieldName(prefix + "id", true, false, F_Type.NUM),
				new FieldName(prefix + "ytd", false, false, F_Type.NUM),
				new FieldName(prefix + "tax", false, false, F_Type.NUM),
				new FieldName(prefix + "next_o_id", false, false, F_Type.NUM),
				new FieldName(prefix + "name", false, false, F_Type.TEXT),
				new FieldName(prefix + "street", false, false, F_Type.TEXT),
				new FieldName(prefix + "city", false, false, F_Type.TEXT),
				new FieldName(prefix + "state", false, false, F_Type.NUM),
				new FieldName(prefix + "zip", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// customer
		table_name = "customer";
		prefix = "c_";
		fns = new FieldName[] { new FieldName(prefix + "wid", true, true, F_Type.NUM),
				new FieldName(prefix + "did", true, false, F_Type.NUM),
				new FieldName(prefix + "id", true, false, F_Type.NUM),
				new FieldName(prefix + "discount", false, false, F_Type.NUM),
				new FieldName(prefix + "credit", false, false, F_Type.NUM),
				new FieldName(prefix + "last", false, false, F_Type.TEXT),
				new FieldName(prefix + "middle", false, false, F_Type.TEXT),
				new FieldName(prefix + "first", false, false, F_Type.TEXT),
				new FieldName(prefix + "credit_lim", false, false, F_Type.NUM),
				new FieldName(prefix + "balance", false, false, F_Type.NUM),
				new FieldName(prefix + "ytd_payment", false, false, F_Type.NUM),
				new FieldName(prefix + "payment_cnt", false, false, F_Type.NUM),
				new FieldName(prefix + "delivery_cnt", false, false, F_Type.NUM),
				new FieldName(prefix + "street", false, false, F_Type.TEXT),
				new FieldName(prefix + "city", false, false, F_Type.TEXT),
				new FieldName(prefix + "state", false, false, F_Type.NUM),
				new FieldName(prefix + "zip", false, false, F_Type.NUM),
				new FieldName(prefix + "phone", false, false, F_Type.TEXT),
				new FieldName(prefix + "since", false, false, F_Type.NUM),
				new FieldName(prefix + "data", false, false, F_Type.TEXT) };
		pu.mkTable(table_name, fns);

		// oorder
		table_name = "oorder";
		prefix = "o_";
		fns = new FieldName[] { new FieldName(prefix + "wid", true, true, F_Type.NUM),
				new FieldName(prefix + "did", true, false, F_Type.NUM),
				new FieldName(prefix + "id", true, false, F_Type.NUM),
				new FieldName(prefix + "cid", false, false, F_Type.NUM),
				new FieldName(prefix + "carrier_id", false, false, F_Type.NUM),
				new FieldName(prefix + "ol_cnt", false, false, F_Type.NUM),
				new FieldName(prefix + "all_local", false, false, F_Type.NUM),
				new FieldName(prefix + "entry_d", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// new order
		table_name = "new_order";
		prefix = "no_";
		fns = new FieldName[] { new FieldName(prefix + "wid", true, true, F_Type.NUM),
				new FieldName(prefix + "did", true, false, F_Type.NUM),
				new FieldName(prefix + "oid", true, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// history
		table_name = "history";
		prefix = "h_";
		fns = new FieldName[] { new FieldName(prefix + "cid", true, true, F_Type.NUM),
				new FieldName(prefix + "c_did", false, false, F_Type.NUM),
				new FieldName(prefix + "c_wid", false, false, F_Type.NUM),
				new FieldName(prefix + "did", false, false, F_Type.NUM),
				new FieldName(prefix + "wid", false, true, F_Type.NUM),
				new FieldName(prefix + "date", false, false, F_Type.NUM),
				new FieldName(prefix + "amount", false, false, F_Type.NUM),
				new FieldName(prefix + "data", false, false, F_Type.TEXT) };
		pu.mkTable(table_name, fns);

		// item
		table_name = "item";
		prefix = "i_";
		fns = new FieldName[] { new FieldName(prefix + "id", true, true, F_Type.NUM),
				new FieldName(prefix + "name", false, false, F_Type.TEXT),
				new FieldName(prefix + "price", false, false, F_Type.NUM),
				new FieldName(prefix + "data", false, false, F_Type.TEXT),
				new FieldName(prefix + "im_id", false, true, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// stock
		table_name = "stock";
		prefix = "s_";
		fns = new FieldName[] { new FieldName(prefix + "wid", true, true, F_Type.NUM),
				new FieldName(prefix + "iid", true, false, F_Type.NUM),
				new FieldName(prefix + "quantitiy", false, false, F_Type.NUM),
				new FieldName(prefix + "ytd", false, false, F_Type.NUM),
				new FieldName(prefix + "order_cnt", false, false, F_Type.NUM),
				new FieldName(prefix + "remote_cnt", false, false, F_Type.NUM),
				new FieldName(prefix + "data", false, false, F_Type.TEXT),
				new FieldName(prefix + "dist_10", false, false, F_Type.TEXT),
				new FieldName(prefix + "dist_1", false, false, F_Type.TEXT),
				new FieldName(prefix + "dist_2", false, false, F_Type.TEXT),
				new FieldName(prefix + "dist_3", false, false, F_Type.TEXT),
				new FieldName(prefix + "dist_4", false, false, F_Type.TEXT),
				new FieldName(prefix + "dist_5", false, false, F_Type.TEXT),
				new FieldName(prefix + "dist_6", false, false, F_Type.TEXT),
				new FieldName(prefix + "dist_7", false, false, F_Type.TEXT),
				new FieldName(prefix + "dist_8", false, false, F_Type.TEXT),
				new FieldName(prefix + "dist_9", false, false, F_Type.TEXT) };
		pu.mkTable(table_name, fns);

		// order_line
		table_name = "order_line";
		prefix = "ol_";
		fns = new FieldName[] { new FieldName(prefix + "wid", true, true, F_Type.NUM),
				new FieldName(prefix + "did", true, false, F_Type.NUM),
				new FieldName(prefix + "oid", true, false, F_Type.NUM),
				new FieldName(prefix + "number", true, false, F_Type.NUM),
				new FieldName(prefix + "iid", false, false, F_Type.NUM),
				new FieldName(prefix + "delivery_d", false, false, F_Type.NUM),
				new FieldName(prefix + "amount", false, false, F_Type.NUM),
				new FieldName(prefix + "supply_wid", false, false, F_Type.NUM),
				new FieldName(prefix + "quantity", false, false, F_Type.NUM),
				new FieldName(prefix + "dist_info", false, false, F_Type.TEXT) };
		pu.mkTable(table_name, fns);

		/*
		 * 
		 * 
		 * 
		 * 
		 * 
		 * ************ Transactions ************
		 *
		 *
		 *
		 *
		 */
		/*
		 * newOrder
		 */
		if (txns.contains("newOrder")) {
			txn_name = "newOrder";
			pu.mkTrnasaction(txn_name, "no_wid:int", "no_did:int", "no_cid:int", "no_o_all_local:int",
					"no_o_ol_cnt:int", "no_t_current:int", "no_item_id:int", "no_supplier_wid:int",
					"no_order_quantity:int");

		}

		/*
		 * payment
		 */
		if (txns.contains("payment")) {
			txn_name = "payment";
		}
		/*
		 * stockLevel
		 */
		if (txns.contains("stockLevel")) {
			txn_name = "stockLevel";
		}
		/*
		 * orderStatus
		 */
		if (txns.contains("orderStatus")) {
			txn_name = "orderStatus";
		}
		/*
		 * delivery
		 */
		if (txns.contains("delivery")) {
			txn_name = "delivery";
		}
		return pu.generateProgram();
	}
}
