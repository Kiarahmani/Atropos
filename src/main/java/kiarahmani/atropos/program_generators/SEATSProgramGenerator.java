package kiarahmani.atropos.program_generators;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_BinOp;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.expression.constants.E_Const_Text;
import kiarahmani.atropos.DML.query.Delete_Query;
import kiarahmani.atropos.DML.query.Insert_Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHCC;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.ProgramGenerator;
import kiarahmani.atropos.utils.Program_Utils;

public class SEATSProgramGenerator implements ProgramGenerator {

	private Program_Utils pu;

	public SEATSProgramGenerator(Program_Utils pu) {
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
		// country
		String table_name = "country";
		String prefix = "co_";
		FieldName[] fns = new FieldName[] { new FieldName(prefix + "id", true, true, F_Type.NUM),
				new FieldName(prefix + "name", false, false, F_Type.TEXT),
				new FieldName(prefix + "code", false, false, F_Type.TEXT) };
		pu.mkTable(table_name, fns);

		// airport
		table_name = "airport";
		prefix = "ap_";
		fns = new FieldName[] { new FieldName(prefix + "id", true, true, F_Type.NUM),
				new FieldName(prefix + "code", false, false, F_Type.TEXT),
				new FieldName(prefix + "name", false, false, F_Type.TEXT),
				new FieldName(prefix + "city", false, false, F_Type.TEXT),
				new FieldName(prefix + "postal_code", false, false, F_Type.NUM),
				new FieldName(prefix + "co_id", false, false, F_Type.NUM),
				new FieldName(prefix + "longitude", false, false, F_Type.NUM),
				new FieldName(prefix + "latitude", false, false, F_Type.NUM),
				new FieldName(prefix + "gmt_offset", false, false, F_Type.NUM),
				new FieldName(prefix + "wac", false, false, F_Type.NUM),
				new FieldName(prefix + "iattr", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// airport_distance
		table_name = "airport_distance";
		prefix = "d_";
		fns = new FieldName[] { new FieldName(prefix + "ap_id0", true, true, F_Type.NUM),
				new FieldName(prefix + "ap_id1", true, false, F_Type.NUM),
				new FieldName(prefix + "distance", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// airline
		table_name = "airline";
		prefix = "al_";
		fns = new FieldName[] { new FieldName(prefix + "id", true, true, F_Type.NUM),
				new FieldName(prefix + "iata_code", false, false, F_Type.NUM),
				new FieldName(prefix + "icao_code", false, false, F_Type.NUM),
				new FieldName(prefix + "call_sign", false, false, F_Type.TEXT),
				new FieldName(prefix + "name", false, false, F_Type.TEXT),
				new FieldName(prefix + "co_id", false, false, F_Type.NUM),
				new FieldName(prefix + "iattr", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// customer
		table_name = "customer";
		prefix = "c_";
		fns = new FieldName[] { new FieldName(prefix + "id", true, true, F_Type.NUM),
				new FieldName(prefix + "id_str", false, false, F_Type.TEXT),
				new FieldName(prefix + "base_ap_id", false, false, F_Type.NUM),
				new FieldName(prefix + "balance", false, false, F_Type.NUM),
				new FieldName(prefix + "sattr", false, false, F_Type.NUM),
				new FieldName(prefix + "iattr", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// frequent_flyer
		table_name = "frequent_flyer";
		prefix = "ff_";
		fns = new FieldName[] { new FieldName(prefix + "c_id", true, true, F_Type.NUM),
				new FieldName(prefix + "al_id", true, false, F_Type.NUM),
				new FieldName(prefix + "c_id_str", false, false, F_Type.NUM),
				new FieldName(prefix + "sattr", false, false, F_Type.NUM),
				new FieldName(prefix + "iattr", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// flight
		table_name = "flight";
		prefix = "f_";
		fns = new FieldName[] { new FieldName(prefix + "id", true, true, F_Type.NUM),
				new FieldName(prefix + "al_id", false, false, F_Type.NUM),
				new FieldName(prefix + "depart_ap_id", false, false, F_Type.NUM),
				new FieldName(prefix + "depart_time", false, false, F_Type.NUM),
				new FieldName(prefix + "arrive_ap_id", false, false, F_Type.NUM),
				new FieldName(prefix + "arrive_time", false, false, F_Type.NUM),
				new FieldName(prefix + "status", false, false, F_Type.NUM),
				new FieldName(prefix + "base_price", false, false, F_Type.NUM),
				new FieldName(prefix + "seats_total", false, false, F_Type.NUM),
				new FieldName(prefix + "seats_left", false, false, F_Type.NUM),
				new FieldName(prefix + "iattr", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// reservation
		table_name = "reservation";
		prefix = "r_";
		fns = new FieldName[] { new FieldName(prefix + "id", true, true, F_Type.NUM),
				new FieldName(prefix + "c_id", true, false, F_Type.NUM),
				new FieldName(prefix + "f_id", true, false, F_Type.NUM),
				new FieldName(prefix + "seat", false, false, F_Type.NUM),
				new FieldName(prefix + "price", false, false, F_Type.NUM),
				new FieldName(prefix + "iattr", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		/*
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
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
		 *
		 *
		 *
		 *
		 *
		 *
		 *
		 *
		 *
		 *
		 *
		 *
		 *
		 *
		 *
		 *
		 *
		 */
		/*
		 * deleteReservation
		 */
		if (txns.contains("deleteReservation")) {
			txn_name = "deleteReservation";
			prefix = "dr_";
			pu.mkTrnasaction(txn_name, prefix + "f_id:int", prefix + "given_c_id:int", prefix + "ff_c_id_str:string",
					prefix + "ff_al_id:int");
			// get customer's details
			table_name = "customer";
			WHC whc1 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("c_id"), BinOp.EQ, pu.getArg("dr_given_c_id")));
			Select_Query select1 = pu.addSelectQuery(txn_name, table_name, whc1, "c_balance", "c_sattr", "c_iattr");
			pu.addQueryStatement(txn_name, select1);

			// retrieve the flight details
			table_name = "flight";
			WHC whc2 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("f_id"), BinOp.EQ, pu.getArg("dr_f_id")));
			Select_Query select2 = pu.addSelectQuery(txn_name, table_name, whc2, "f_seats_left");
			pu.addQueryStatement(txn_name, select2);

			// retrieve the reservation's details
			table_name = "reservation";
			WHC whc3 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("r_c_id"), BinOp.EQ, pu.getArg("dr_given_c_id")));
			Select_Query select3 = pu.addSelectQuery(txn_name, table_name, whc3, "r_id", "r_seat", "r_price",
					"r_iattr");
			pu.addQueryStatement(txn_name, select3);

			// delete their first reservation
			table_name = "reservation";
			WHC whc4 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_id"), BinOp.EQ,
							pu.mkProjExpr(txn_name, 2, "r_id", 1)),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_c_id"), BinOp.EQ,
							pu.getArg("dr_given_c_id")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_f_id"), BinOp.EQ,
							pu.getArg("dr_f_id")));
			Delete_Query delete4 = pu.addDeleteQuery(txn_name, table_name, whc4);
			pu.addQueryStatement(txn_name, delete4);

			// update the number of seats left in the flight
			table_name = "flight";
			WHC whc5 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("f_id"), BinOp.EQ, pu.getArg("dr_f_id")));
			Update_Query update5 = pu.addUpdateQuery(txn_name, table_name, whc5);
			update5.addUpdateExp(pu.getFieldName("f_seats_left"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 1, "f_seats_left", 1), new E_Const_Num(1)));
			pu.addQueryStatement(txn_name, update5);

			// update customer's balance
			table_name = "customer";
			WHC whc6 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("c_id"), BinOp.EQ, pu.getArg("dr_given_c_id")));
			Update_Query update6 = pu.addUpdateQuery(txn_name, table_name, whc6);
			update6.addUpdateExp(pu.getFieldName("c_balance"), new E_BinOp(BinOp.PLUS,
					pu.mkProjExpr(txn_name, 0, "c_balance", 1), pu.mkProjExpr(txn_name, 2, "r_price", 1)));
			update6.addUpdateExp(pu.getFieldName("c_sattr"),
					new E_BinOp(BinOp.MINUS, pu.mkProjExpr(txn_name, 0, "c_sattr", 1), new E_Const_Num(1)));
			update6.addUpdateExp(pu.getFieldName("c_iattr"),
					new E_BinOp(BinOp.MINUS, pu.mkProjExpr(txn_name, 0, "c_iattr", 1), new E_Const_Num(1)));
			pu.addQueryStatement(txn_name, update6);

		}

		/*
		 * findFlights
		 */
		if (txns.contains("findFlights")) {
			txn_name = "findFlights";
			prefix = "ff_";
			pu.mkTrnasaction(txn_name, prefix + "depart_aid:int", prefix + "arrive_aid:int", prefix + "start_date:int",
					prefix + "end_date:int");

			// retrieve flights' details
			table_name = "flight";
			WHC whc1 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("f_depart_ap_id"), BinOp.EQ,
							pu.getArg("ff_depart_aid")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("f_arrive_ap_id"), BinOp.EQ,
							pu.getArg("ff_arrive_aid")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("f_arrive_time"), BinOp.LT,
							pu.getArg("ff_end_date")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("f_depart_time"), BinOp.GT,
							pu.getArg("ff_start_date")));
			Select_Query select1 = pu.addSelectQuery(txn_name, table_name, whc1, "f_id", "f_al_id", "f_seats_left",
					"f_depart_time", "f_arrive_time", "f_iattr");
			pu.addQueryStatement(txn_name, select1);

			// retrieve airline
			table_name = "airline";
			WHC whc2 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("al_id"), BinOp.EQ, pu.mkProjExpr(txn_name, 0, "f_al_id", 1)));
			Select_Query select2 = pu.addSelectQuery(txn_name, table_name, whc2, "al_name", "al_iattr");
			pu.addQueryStatement(txn_name, select2);

			// retrieve depart airport details
			table_name = "airport";
			WHC whc3 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("ap_id"), BinOp.EQ, pu.getArg("ff_depart_aid")));
			Select_Query select3 = pu.addSelectQuery(txn_name, table_name, whc3, "ap_code", "ap_name", "ap_city",
					"ap_co_id", "ap_longitude", "ap_latitude");
			pu.addQueryStatement(txn_name, select3);
			// retrieve depart country details
			table_name = "country";
			WHC whc4 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("co_id"), BinOp.EQ, pu.mkProjExpr(txn_name, 2, "ap_co_id", 1)));
			Select_Query select4 = pu.addSelectQuery(txn_name, table_name, whc4, "co_name", "co_code");
			pu.addQueryStatement(txn_name, select4);

			// retrieve arrive airport details
			table_name = "airport";
			WHC whc5 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("ap_id"), BinOp.EQ, pu.getArg("ff_arrive_aid")));
			Select_Query select5 = pu.addSelectQuery(txn_name, table_name, whc5, "ap_code", "ap_name", "ap_city",
					"ap_co_id", "ap_longitude", "ap_latitude");
			pu.addQueryStatement(txn_name, select5);

			// retrieve arrive country details
			table_name = "country";
			WHC whc6 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("co_id"), BinOp.EQ, pu.mkProjExpr(txn_name, 4, "ap_co_id", 1)));
			Select_Query select6 = pu.addSelectQuery(txn_name, table_name, whc6, "co_name", "co_code");
			pu.addQueryStatement(txn_name, select6);

		}
		/*
		 * findOpenSeats
		 */
		if (txns.contains("findOpenSeats")) {
			txn_name = "findOpenSeats";
			prefix = "fos_";
			pu.mkTrnasaction(txn_name, prefix + "fid:int");

			// retrieve flight's details
			table_name = "flight";
			WHC whc1 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("f_id"), BinOp.EQ, pu.getArg("fos_fid")));
			Select_Query select1 = pu.addSelectQuery(txn_name, table_name, whc1, "f_status", "f_base_price",
					"f_seats_total", "f_seats_left");
			pu.addQueryStatement(txn_name, select1);

			// get all reservations on this flight
			table_name = "reservation";
			WHC whc2 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("r_f_id"), BinOp.EQ, pu.getArg("fos_fid")));
			Select_Query select2 = pu.addSelectQuery(txn_name, table_name, whc2, "r_id", "r_seat");
			pu.addQueryStatement(txn_name, select2);
		}
		/*
		 * newReservation
		 */
		if (txns.contains("newReservation")) {
			txn_name = "newReservation";
			prefix = "nr_";
			pu.mkTrnasaction(txn_name, prefix + "r_id:int", prefix + "c_id:int", prefix + "f_id:int",
					prefix + "seatnum:int", prefix + "price:int", prefix + "attr:int");

			// retrieve flight's information
			table_name = "flight";
			WHC whc1 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("f_id"), BinOp.EQ, pu.getArg("nr_f_id")));
			Select_Query select1 = pu.addSelectQuery(txn_name, table_name, whc1, "f_base_price", "f_al_id",
					"f_seats_left");
			pu.addQueryStatement(txn_name, select1);

			// retrieve airline information
			table_name = "airline";
			WHC whc2 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("al_id"), BinOp.EQ, pu.mkProjExpr(txn_name, 0, "f_al_id", 1)));
			Select_Query select2 = pu.addSelectQuery(txn_name, table_name, whc2, "al_name", "al_iattr");
			pu.addQueryStatement(txn_name, select2);

			// if seats left > 0
			Expression if1 = new E_BinOp(BinOp.GT, pu.mkProjExpr(txn_name, 0, "f_seats_left", 1), new E_Const_Num(0));
			pu.addIfStmt(txn_name, if1);

			// retrieve reservations on this flight
			table_name = "reservation";
			WHC whc3 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_f_id"), BinOp.EQ,
							pu.getArg("nr_f_id")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_seat"), BinOp.EQ,
							pu.getArg("nr_seatnum")));
			Select_Query select3 = pu.addSelectQuery(txn_name, table_name, whc3, "r_id");
			pu.addQueryStatementInIf(txn_name, 0, select3);

			// retrieve reservations of this customer
			table_name = "reservation";
			WHC whc4 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_f_id"), BinOp.EQ,
							pu.getArg("nr_f_id")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_c_id"), BinOp.EQ,
							pu.getArg("nr_c_id")));
			Select_Query select4 = pu.addSelectQuery(txn_name, table_name, whc4, "r_id");
			pu.addQueryStatementInIf(txn_name, 0, select4);

			// retrieve customer's information
			table_name = "customer";
			WHC whc5 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("c_id"), BinOp.EQ, pu.getArg("nr_c_id")));
			Select_Query select5 = pu.addSelectQuery(txn_name, table_name, whc5, "c_base_ap_id", "c_balance", "c_sattr",
					"c_iattr");
			pu.addQueryStatementInIf(txn_name, 0, select5);

			// insert a new reservation
			table_name = "reservation";
			Insert_Query insert6 = pu.addInsertQuery(txn_name, table_name,
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_id"), BinOp.EQ,
							pu.getArg("nr_r_id")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_c_id"), BinOp.EQ,
							pu.getArg("nr_c_id")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_f_id"), BinOp.EQ,
							pu.getArg("nr_f_id")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_seat"), BinOp.EQ,
							pu.getArg("nr_seatnum")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_price"), BinOp.EQ,
							pu.mkProjExpr(txn_name, 0, "f_base_price", 1)),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_iattr"), BinOp.EQ,
							pu.getArg("nr_attr")));
			insert6.addInsertExp(pu.getFieldName("r_id"), pu.getArg("nr_r_id"));
			insert6.addInsertExp(pu.getFieldName("r_c_id"), pu.getArg("nr_c_id"));
			insert6.addInsertExp(pu.getFieldName("r_f_id"), pu.getArg("nr_f_id"));
			insert6.addInsertExp(pu.getFieldName("r_seat"), pu.getArg("nr_seatnum"));
			insert6.addInsertExp(pu.getFieldName("r_price"), pu.mkProjExpr(txn_name, 0, "f_base_price", 1));
			insert6.addInsertExp(pu.getFieldName("r_iattr"), pu.getArg("nr_attr"));
			pu.addQueryStatementInIf(txn_name, 0, insert6);

			// update customer
			table_name = "customer";
			WHC whc7 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("c_id"), BinOp.EQ, pu.getArg("nr_c_id")));
			Update_Query update7 = pu.addUpdateQuery(txn_name, table_name, whc7);
			update7.addUpdateExp(pu.getFieldName("c_balance"), new E_BinOp(BinOp.MINUS,
					pu.mkProjExpr(txn_name, 4, "c_balance", 1), pu.mkProjExpr(txn_name, 0, "f_base_price", 1)));
			update7.addUpdateExp(pu.getFieldName("c_sattr"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 4, "c_sattr", 1), new E_Const_Num(1)));
			update7.addUpdateExp(pu.getFieldName("c_iattr"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 4, "c_iattr", 1), new E_Const_Num(1)));
			pu.addQueryStatementInIf(txn_name, 0, update7);

			// update flight
			table_name = "flight";
			WHC whc10 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("f_id"), BinOp.EQ, pu.getArg("nr_f_id")));
			Update_Query update10 = pu.addUpdateQuery(txn_name, table_name, whc10);
			update10.addUpdateExp(pu.getFieldName("f_seats_left"),
					new E_BinOp(BinOp.MINUS, pu.mkProjExpr(txn_name, 0, "f_seats_left", 1), new E_Const_Num(1)));
			pu.addQueryStatementInIf(txn_name, 0, update10);

			// select frequent_flyer
			table_name = "frequent_flyer";
			WHC whc8 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("ff_c_id"), BinOp.EQ,
							pu.getArg("nr_c_id")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("ff_al_id"), BinOp.EQ,
							pu.mkProjExpr(txn_name, 0, "f_al_id", 1)));
			Select_Query select8 = pu.addSelectQuery(txn_name, table_name, whc8, "ff_sattr", "ff_iattr");
			pu.addQueryStatementInIf(txn_name, 0, select8);

			// update frequent_flyer
			table_name = "frequent_flyer";
			WHC whc9 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("ff_c_id"), BinOp.EQ,
							pu.getArg("nr_c_id")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("ff_al_id"), BinOp.EQ,
							pu.mkProjExpr(txn_name, 0, "f_al_id", 1)));

			Update_Query update9 = pu.addUpdateQuery(txn_name, table_name, whc9);
			update9.addUpdateExp(pu.getFieldName("ff_sattr"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 5, "ff_sattr", 1), new E_Const_Num(1)));
			update9.addUpdateExp(pu.getFieldName("ff_iattr"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 5, "ff_iattr", 1), new E_Const_Num(1)));
			pu.addQueryStatementInIf(txn_name, 0, update9);

		}
		/*
		 * updateCustomer
		 */
		if (txns.contains("updateCustomer")) {
			txn_name = "updateCustomer";
			prefix = "uc_";
			pu.mkTrnasaction(txn_name, prefix + "c_id:int", prefix + "update_ff:int", prefix + "attr0:int",
					prefix + "attr1:int");

			// retrieve customer's details
			table_name = "customer";
			WHC whc1 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("c_id"), BinOp.EQ, pu.getArg("uc_c_id")));
			Select_Query select1 = pu.addSelectQuery(txn_name, table_name, whc1, "c_base_ap_id", "c_balance", "c_sattr",
					"c_iattr");
			pu.addQueryStatement(txn_name, select1);
			// retrieve their base airport details
			table_name = "airport";
			WHC whc2 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("ap_id"), BinOp.EQ, pu.mkProjExpr(txn_name, 0, "c_base_ap_id", 1)));
			Select_Query select2 = pu.addSelectQuery(txn_name, table_name, whc2, "ap_code", "ap_name", "ap_city",
					"ap_co_id", "ap_longitude", "ap_latitude");
			pu.addQueryStatement(txn_name, select2);

			// retrieve airport's country's details
			table_name = "country";
			WHC whc3 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("co_id"), BinOp.EQ, pu.mkProjExpr(txn_name, 1, "ap_co_id", 1)));
			Select_Query select3 = pu.addSelectQuery(txn_name, table_name, whc3, "co_name", "co_code");
			pu.addQueryStatement(txn_name, select3);
		}

		/*
		 * updateReservation
		 */
		if (txns.contains("updateReservation")) {
			txn_name = "updateReservation";
			prefix = "ur_";
			pu.mkTrnasaction(txn_name, prefix + "r_id:int", prefix + "f_id:int", prefix + "c_id:int",
					prefix + "seatnum:int", prefix + "attr_idx:int", prefix + "attr_val:int");

			// retrieve target reservation's details
			table_name = "reservation";
			WHC whc1 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_f_id"), BinOp.EQ,
							pu.getArg("ur_f_id")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_seat"), BinOp.EQ,
							pu.getArg("ur_seatnum")));
			Select_Query select1 = pu.addSelectQuery(txn_name, table_name, whc1, "r_id");
			pu.addQueryStatement(txn_name, select1);

			// retrieve reservations of the customer
			table_name = "reservation";
			WHC whc2 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_f_id"), BinOp.EQ,
							pu.getArg("ur_f_id")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_c_id"), BinOp.EQ,
							pu.getArg("ur_c_id")));
			Select_Query select2 = pu.addSelectQuery(txn_name, table_name, whc2, "r_id");
			pu.addQueryStatement(txn_name, select2);

			// update seat number
			table_name = "reservation";
			WHC whc3 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_id"), BinOp.EQ,
							pu.mkProjExpr(txn_name, 1, "r_id", 1)),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_f_id"), BinOp.EQ,
							pu.getArg("ur_f_id")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("r_c_id"), BinOp.EQ,
							pu.getArg("ur_c_id")));
			Update_Query update3 = pu.addUpdateQuery(txn_name, table_name, whc3);
			update3.addUpdateExp(pu.getFieldName("r_seat"), pu.getArg("ur_seatnum"));
			pu.addQueryStatement(txn_name, update3);

		}

		return pu.generateProgram();
	}
}
