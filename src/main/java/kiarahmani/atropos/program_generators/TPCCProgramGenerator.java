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
import kiarahmani.atropos.DML.expression.constants.E_Const_Text;
import kiarahmani.atropos.DML.query.Insert_Query;
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
		 * ****************** Tables ******************
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
		 * ************************ Transactions ************************
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
					"no_t_current:int", "no_item_id:int", "no_supplier_wid:int", "no_order_quantity:int");

			// retrieve w_tax name by w_id
			table_name = "warehouse";
			WHC newOrder_whc_1 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("w_id"), BinOp.EQ, pu.getArg("no_wid")));
			Select_Query newOrder1 = pu.addSelectQuery(txn_name, table_name, newOrder_whc_1, "w_tax");
			pu.addQueryStatement(txn_name, newOrder1);

			// retrieve d_tax and d_next_o_id
			table_name = "district";
			WHC newOrder_whc_2 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("d_wid"), BinOp.EQ,
							pu.getArg("no_wid")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("d_id"), BinOp.EQ,
							pu.getArg("no_did")));
			Select_Query newOrder2 = pu.addSelectQuery(txn_name, table_name, newOrder_whc_2, "d_tax", "d_next_o_id");
			pu.addQueryStatement(txn_name, newOrder2);

			// incremenet d_next_o_id
			table_name = "district";
			WHC newOrder_whc_3 = newOrder_whc_2.mkSnapshot();
			Update_Query newOrder3 = pu.addUpdateQuery(txn_name, table_name, newOrder_whc_3);
			newOrder3.addUpdateExp(pu.getFieldName("d_next_o_id"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 1, "d_next_o_id", 1), new E_Const_Num(1)));
			pu.addQueryStatement(txn_name, newOrder3);

			// insert a new order record
			table_name = "oorder";
			Insert_Query newOrder4 = pu.addInsertQuery(txn_name, table_name,
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("o_wid"), BinOp.EQ,
							pu.getArg("no_wid")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("o_did"), BinOp.EQ,
							pu.getArg("no_did")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("o_id"), BinOp.EQ,
							pu.mkProjExpr(txn_name, 1, "d_next_o_id", 1)));

			newOrder4.addInsertExp(pu.getFieldName("o_cid"), pu.getArg("no_cid"));
			newOrder4.addInsertExp(pu.getFieldName("o_carrier_id"), new E_Const_Num(-1));
			newOrder4.addInsertExp(pu.getFieldName("o_ol_cnt"), new E_Const_Num(1));
			newOrder4.addInsertExp(pu.getFieldName("o_all_local"), pu.getArg("no_o_all_local"));
			newOrder4.addInsertExp(pu.getFieldName("o_entry_d"), pu.getArg("no_t_current"));
			pu.addQueryStatement(txn_name, newOrder4);

			// insert a new new_order record
			table_name = "new_order";
			Insert_Query newOrder5 = pu.addInsertQuery(txn_name, table_name,
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("no_wid"), BinOp.EQ,
							pu.getArg("no_wid")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("no_did"), BinOp.EQ,
							pu.getArg("no_did")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("no_oid"), BinOp.EQ,
							pu.mkProjExpr(txn_name, 1, "d_next_o_id", 1)));
			pu.addQueryStatement(txn_name, newOrder5);

			// retrieve customer' information
			table_name = "customer";
			WHC newOrder_whc_6 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("c_wid"), BinOp.EQ,
							pu.getArg("no_wid")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("c_did"), BinOp.EQ,
							pu.getArg("no_did")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("c_id"), BinOp.EQ,
							pu.getArg("no_cid")));
			Select_Query newOrder6 = pu.addSelectQuery(txn_name, table_name, newOrder_whc_6, "c_discount", "c_credit",
					"c_last");
			pu.addQueryStatement(txn_name, newOrder6);

			// retrieve item' information
			table_name = "item";
			WHC newOrder_whc_7 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("i_id"), BinOp.EQ, pu.getArg("no_item_id")));
			Select_Query newOrder7 = pu.addSelectQuery(txn_name, table_name, newOrder_whc_7, "i_price", "i_name",
					"i_data");
			pu.addQueryStatement(txn_name, newOrder7);

			// retrieve stock' information
			table_name = "stock";
			WHC newOrder_whc_8 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("s_wid"), BinOp.EQ,
							pu.getArg("no_wid")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("s_iid"), BinOp.EQ,
							pu.getArg("no_item_id")));
			Select_Query newOrder8 = pu.addSelectQuery(txn_name, table_name, newOrder_whc_8, "s_quantitiy", "s_ytd",
					"s_order_cnt", "s_remote_cnt", "s_data", "s_dist_1");
			pu.addQueryStatement(txn_name, newOrder8);

			// update stock's information
			table_name = "stock";
			WHC newOrder_whc_9 = newOrder_whc_8.mkSnapshot();
			Update_Query newOrder9 = pu.addUpdateQuery(txn_name, table_name, newOrder_whc_9);
			newOrder9.addUpdateExp(pu.getFieldName("s_quantitiy"), new E_BinOp(BinOp.MINUS,
					pu.mkProjExpr(txn_name, 4, "s_quantitiy", 1), pu.getArg("no_order_quantity")));
			newOrder9.addUpdateExp(pu.getFieldName("s_ytd"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 4, "s_ytd", 1), pu.getArg("no_order_quantity")));
			newOrder9.addUpdateExp(pu.getFieldName("s_order_cnt"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 4, "s_order_cnt", 1), new E_Const_Num(1)));
			pu.addQueryStatement(txn_name, newOrder9);

			// insert a new order_line record
			table_name = "order_line";
			Insert_Query newOrder10 = pu.addInsertQuery(txn_name, table_name,
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("ol_wid"), BinOp.EQ,
							pu.getArg("no_wid")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("ol_did"), BinOp.EQ,
							pu.getArg("no_did")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("ol_oid"), BinOp.EQ,
							pu.mkProjExpr(txn_name, 1, "d_next_o_id", 1)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("ol_number"), BinOp.EQ,
							new E_Const_Num(1)));
			newOrder10.addInsertExp(pu.getFieldName("ol_iid"), pu.getArg("no_item_id"));
			newOrder10.addInsertExp(pu.getFieldName("ol_delivery_d"), new E_Const_Num(-1));
			newOrder10.addInsertExp(pu.getFieldName("ol_quantity"), pu.getArg("no_order_quantity"));
			newOrder10.addInsertExp(pu.getFieldName("ol_supply_wid"), new E_Const_Num(-1));
			newOrder10.addInsertExp(pu.getFieldName("ol_amount"),
					new E_BinOp(BinOp.MULT, pu.mkProjExpr(txn_name, 3, "i_price", 1), pu.getArg("no_order_quantity")));
			newOrder10.addInsertExp(pu.getFieldName("ol_dist_info"), new E_Const_Text("info"));

			pu.addQueryStatement(txn_name, newOrder10);

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
