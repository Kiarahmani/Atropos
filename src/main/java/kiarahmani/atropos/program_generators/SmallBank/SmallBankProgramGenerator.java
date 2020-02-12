package kiarahmani.atropos.program_generators.SmallBank;

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

public class SmallBankProgramGenerator implements ProgramGenerator {

	/*
	 * 
	 * SMALLBANK APPLICATION (FROM OLTPBENCH) GENERATOR
	 * https://github.com/oltpbenchmark/oltpbench/tree/master/src/com/oltpbenchmark/
	 * benchmarks/smallbank
	 * 
	 */

	private Program_Utils pu;

	public SmallBankProgramGenerator(Program_Utils pu2) {
		this.pu = pu2;
	}

	public Program generate(String... args) {
		/*
		 * 
		 * Tables
		 * 
		 */
		ArrayList<String> txns = new ArrayList<>();
		for (String txn : args)
			txns.add(txn);

		pu.mkTable("accounts", new FieldName("a_custid", true, true, F_Type.NUM),
				new FieldName("a_name", false, false, F_Type.TEXT), new FieldName("a_age", false, false, F_Type.NUM),
				new FieldName("a_dist", false, false, F_Type.NUM),
				new FieldName("a_sex", false, false, F_Type.NUM)
				);

		 pu.mkTable("dist", new FieldName("d_distid", true, true, F_Type.NUM),
		new FieldName("d_city", false, false, F_Type.NUM)
		, 
		new FieldName("d_state",
		 false, false, F_Type.NUM)
		);

		pu.mkTable("savings", new FieldName("s_custid", true, true, F_Type.NUM),
				new FieldName("s_bal", false, false, F_Type.NUM), new FieldName("s_history", false, false, F_Type.NUM)

		);
		pu.mkTable("checking", new FieldName("c_custid", true, true, F_Type.NUM),
				new FieldName("c_bal", false, false, F_Type.NUM)
				,new FieldName("c_history", false, false, F_Type.NUM)

		);

		/*
		 * /*
		 * 
		 * Amalgamate
		 * 
		 */
		if (txns.contains("Amalgamate")) {
			pu.mkTrnasaction("Amalgamate", "am_custId0:int", "am_custId1:int");
			pu.mkAssertion("Amalgamate",
					new E_UnOp(UnOp.NOT, new E_BinOp(BinOp.EQ, pu.getArg("am_custId1"), pu.getArg("am_custId0"))));
			// retrieve customer0's name by id
			WHC GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("am_custId0")));
			Select_Query GetAccount0 = pu.addSelectQuery("Amalgamate", "accounts", GetAccount0_WHC, "a_name");
			GetAccount0.setImplicitlyUsed(pu.getFieldName("a_name"));
			pu.addQueryStatement("Amalgamate", GetAccount0);
			// retrieve customer1's name by id
			WHC GetAccount1_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("am_custId1")));
			Select_Query GetAccount1 = pu.addSelectQuery("Amalgamate", "accounts", GetAccount1_WHC, "a_name");
			GetAccount1.setImplicitlyUsed(pu.getFieldName("a_name"));
			pu.addQueryStatement("Amalgamate", GetAccount1);

			// retrieve savings balance of cust0
			WHC GetSavings0_WHC = new WHC(pu.getIsAliveFieldName("savings"), new WHC_Constraint(
					pu.getTableName("savings"), pu.getFieldName("s_custid"), BinOp.EQ, pu.getArg("am_custId0")));
			Select_Query GetSavings0 = pu.addSelectQuery("Amalgamate", "savings", GetSavings0_WHC, "s_bal");
			pu.addQueryStatement("Amalgamate", GetSavings0);

			// retrieve checking balance of cust0
			WHC GetChecking1_WHC = new WHC(pu.getIsAliveFieldName("checking"), new WHC_Constraint(
					pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("am_custId0")));
			Select_Query GetChecking1 = pu.addSelectQuery("Amalgamate", "checking", GetChecking1_WHC, "c_bal");
			pu.addQueryStatement("Amalgamate", GetChecking1);

			// zero cust0's checking balance
			WHC ZeroCheckingBalance_WHC = new WHC(pu.getIsAliveFieldName("checking"), new WHC_Constraint(
					pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("am_custId0")));
			Update_Query ZeroCheckingBalance = pu.addUpdateQuery("Amalgamate", "checking", ZeroCheckingBalance_WHC);
			ZeroCheckingBalance.addUpdateExp(pu.getFieldName("c_bal"), new E_Const_Num(0));
			pu.addQueryStatement("Amalgamate", ZeroCheckingBalance);

			// zero cust0's savings balance
			WHC savingsZeroCheckingBalance_WHC = new WHC(pu.getIsAliveFieldName("savings"), new WHC_Constraint(
					pu.getTableName("savings"), pu.getFieldName("s_custid"), BinOp.EQ, pu.getArg("am_custId0")));
			Update_Query savingsZeroCheckingBalance = pu.addUpdateQuery("Amalgamate", "savings",
					savingsZeroCheckingBalance_WHC);
			savingsZeroCheckingBalance.addUpdateExp(pu.getFieldName("s_bal"), new E_Const_Num(0));
			pu.addQueryStatement("Amalgamate", savingsZeroCheckingBalance);

			// incremenet cust1's savings balance
			WHC UpdateSavingsBalance_WHC = new WHC(pu.getIsAliveFieldName("savings"), new WHC_Constraint(
					pu.getTableName("savings"), pu.getFieldName("s_custid"), BinOp.EQ, pu.getArg("am_custId1")));
			Update_Query UpdateSavingsBalance = pu.addUpdateQuery("Amalgamate", "savings", UpdateSavingsBalance_WHC);
			UpdateSavingsBalance.addUpdateExp(pu.getFieldName("s_bal"), new E_BinOp(BinOp.PLUS,
					pu.mkProjExpr("Amalgamate", 2, "s_bal", 1), pu.mkProjExpr("Amalgamate", 3, "c_bal", 1)));
			pu.addQueryStatement("Amalgamate", UpdateSavingsBalance);
		}

		/*
		 * 
		 * TEST
		 * 
		 */

		/*
		 * if (txns.contains("test")) {
		 * 
		 * pu.mkTrnasaction("test", "ba_custName:string");
		 * 
		 * WHC Balance_GetAccount0_WHC1 = new WHC(pu.getIsAliveFieldName("accounts"),
		 * new WHC_Constraint( pu.getTableName("accounts"), pu.getFieldName("a_custid"),
		 * BinOp.EQ, new E_Const_Num(69))); Select_Query Balance_GetAccount01 =
		 * pu.addSelectQuery("test", "accounts", Balance_GetAccount0_WHC1, "a_custid");
		 * pu.addQueryStatement("test", Balance_GetAccount01);
		 * 
		 * // get customer's id based on his/her name WHC Balance_GetAccount0_WHC = new
		 * WHC(pu.getIsAliveFieldName("accounts"), new
		 * WHC_Constraint(pu.getTableName("accounts"), pu.getFieldName("a_custid"),
		 * BinOp.EQ, pu.mkProjExpr("test", 0, "a_custid", 1))); Select_Query
		 * Balance_GetAccount0 = pu.addSelectQuery("test", "accounts",
		 * Balance_GetAccount0_WHC, "a_name"); pu.addQueryStatement("test",
		 * Balance_GetAccount0);
		 * 
		 * // retrieve customer's savings balance based on the retrieved id WHC
		 * Balance_GetSavings_WHC = new WHC(pu.getIsAliveFieldName("savings"), new
		 * WHC_Constraint(pu.getTableName("savings"), pu.getFieldName("s_custid"),
		 * BinOp.EQ, pu.mkProjExpr("test", 0, "a_custid", 1))); Select_Query
		 * Balance_GetSavings = pu.addSelectQuery("test", "savings",
		 * Balance_GetSavings_WHC, "s_bal"); pu.addQueryStatement("test",
		 * Balance_GetSavings);
		 * 
		 * // retrieve customer's checking balance based on the retrieved id WHC
		 * Balance_GetChecking_WHC = new WHC(pu.getIsAliveFieldName("checking"), new
		 * WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"),
		 * BinOp.EQ, pu.mkProjExpr("test", 0, "a_custid", 1))); Select_Query
		 * Balance_GetChecking = pu.addSelectQuery("test", "checking",
		 * Balance_GetChecking_WHC, "c_bal"); pu.addQueryStatement("test",
		 * Balance_GetChecking);
		 * 
		 * // write customer's new checking balance WHC DepositChecking_WHC = new
		 * WHC(pu.getIsAliveFieldName("checking"), new WHC_Constraint(
		 * pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ, new
		 * E_BinOp(BinOp.PLUS, new E_BinOp(BinOp.MULT, (pu.mkProjExpr("test", 2,
		 * "s_bal", 1)), new E_Const_Num(11)), new E_Const_Num(1)))); Update_Query
		 * DepositChecking = pu.addUpdateQuery("test", "checking", DepositChecking_WHC);
		 * DepositChecking.addUpdateExp(pu.getFieldName("c_bal"), new
		 * E_BinOp(BinOp.PLUS, pu.mkProjExpr("test", 2, "s_bal", 1), new
		 * E_Const_Num(1))); DepositChecking.addUpdateExp(pu.getFieldName("c_score"),
		 * new E_BinOp(BinOp.PLUS, pu.mkProjExpr("test", 0, "a_custid", 1), new
		 * E_Const_Num(69))); pu.addQueryStatement("test", DepositChecking);
		 * 
		 * // write customer's new checking balance WHC DepositChecking_WHC11 = new
		 * WHC(pu.getIsAliveFieldName("checking"), new
		 * WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"),
		 * BinOp.EQ, new E_BinOp(BinOp.PLUS, new E_Const_Num(1), new E_BinOp(BinOp.MULT,
		 * new E_Const_Num(11), (pu.mkProjExpr("test", 2, "s_bal", 1)))
		 * 
		 * ))); Update_Query DepositChecking11 = pu.addUpdateQuery("test", "checking",
		 * DepositChecking_WHC11);
		 * DepositChecking11.addUpdateExp(pu.getFieldName("c_age"), new
		 * E_Const_Num(69)); pu.addQueryStatement("test", DepositChecking11);
		 * 
		 * // select on car WHC select_whc_1 = new WHC(pu.getIsAliveFieldName("car"),
		 * new WHC_Constraint(pu.getTableName("car"), pu.getFieldName("car_id"),
		 * BinOp.EQ, new E_Const_Num(10))); Select_Query select_1 =
		 * pu.addSelectQuery("test", "car", select_whc_1, "car_id", "car_maker",
		 * "car_model", "car_type"); pu.addQueryStatement("test", select_1);
		 * 
		 * // select corresponding maker WHC select_whc_2 = new
		 * WHC(pu.getIsAliveFieldName("makers"), new
		 * WHC_Constraint(pu.getTableName("makers"), pu.getFieldName("maker_id"),
		 * BinOp.EQ, pu.mkProjExpr("test", 4, "car_maker", 1))); Select_Query select_2 =
		 * pu.addSelectQuery("test", "makers", select_whc_2, "maker_id", "maker_name",
		 * "maker_budget", "maker_country"); pu.addQueryStatement("test", select_2);
		 * 
		 * // a write on savings (which will be used to test basic duplication) WHC
		 * DepositChecking_WHC111 = new WHC(pu.getIsAliveFieldName("savings"), new
		 * WHC_Constraint( pu.getTableName("savings"), pu.getFieldName("s_custid"),
		 * BinOp.EQ, new E_Const_Num(96))); Update_Query DepositChecking111 =
		 * pu.addUpdateQuery("test", "savings", DepositChecking_WHC111);
		 * DepositChecking111.addUpdateExp(pu.getFieldName("s_bal"), new
		 * E_Const_Num(69)); pu.addQueryStatement("test", DepositChecking111);
		 * 
		 * // a write on makers (which will be used to test OTM duplication) WHC
		 * DepositChecking_WHC1111 = new WHC(pu.getIsAliveFieldName("makers"), new
		 * WHC_Constraint( pu.getTableName("makers"), pu.getFieldName("maker_id"),
		 * BinOp.EQ, new E_Const_Num(12))); Update_Query DepositChecking1111 =
		 * pu.addUpdateQuery("test", "makers", DepositChecking_WHC1111);
		 * DepositChecking1111.addUpdateExp(pu.getFieldName("maker_budget"), new
		 * E_BinOp(BinOp.PLUS, pu.mkProjExpr("test", 5, "maker_budget", 1), new
		 * E_Const_Num(100))); pu.addQueryStatement("test", DepositChecking1111);
		 * 
		 * // select a maker's budget from the original table (this will be redirected
		 * to // CRDT table) WHC select_whc_21 = new
		 * WHC(pu.getIsAliveFieldName("makers"), new
		 * WHC_Constraint(pu.getTableName("makers"), pu.getFieldName("maker_id"),
		 * BinOp.EQ, new E_Const_Num(10))); Select_Query select_21 =
		 * pu.addSelectQuery("test", "makers", select_whc_21, "maker_budget");
		 * pu.addQueryStatement("test", select_21);
		 * 
		 * // just an operation to use the variable selected by the above query WHC
		 * select_whc_211 = new WHC(pu.getIsAliveFieldName("accounts"), new
		 * WHC_Constraint(pu.getTableName("accounts"), pu.getFieldName("a_custid"),
		 * BinOp.EQ, pu.mkProjExpr("test", 6, "maker_budget", 1))); Select_Query
		 * select_211 = pu.addSelectQuery("test", "accounts", select_whc_211, "a_name");
		 * pu.addQueryStatement("test", select_211);
		 * 
		 * }
		 */
		/*
		 * 
		 * Balance
		 * 
		 */

		if (txns.contains("Balance")) {

			pu.mkTrnasaction("Balance", "ba_custName:string");

			// get customer's id based on his/her name
			WHC Balance_GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_name"), BinOp.EQ, pu.getArg("ba_custName")));
			Select_Query Balance_GetAccount0 = pu.addSelectQuery("Balance", "accounts", Balance_GetAccount0_WHC,
					"a_custid");
			pu.addQueryStatement("Balance", Balance_GetAccount0);

			// retrieve customer's savings balance based on the retrieved id
			WHC Balance_GetSavings_WHC = new WHC(pu.getIsAliveFieldName("savings"),
					new WHC_Constraint(pu.getTableName("savings"), pu.getFieldName("s_custid"), BinOp.EQ,
							pu.mkProjExpr("Balance", 0, "a_custid", 1)));
			Select_Query Balance_GetSavings = pu.addSelectQuery("Balance", "savings", Balance_GetSavings_WHC, "s_bal");
			Balance_GetSavings.setImplicitlyUsed(pu.getFieldName("s_bal"));
			pu.addQueryStatement("Balance", Balance_GetSavings);

			// retrieve customer's checking balance based on the retrieved id
			WHC Balance_GetChecking_WHC = new WHC(pu.getIsAliveFieldName("checking"),
					new WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ,
							pu.mkProjExpr("Balance", 0, "a_custid", 1)));
			Select_Query Balance_GetChecking = pu.addSelectQuery("Balance", "checking", Balance_GetChecking_WHC,
					"c_bal");
			Balance_GetChecking.setImplicitlyUsed(pu.getFieldName("c_bal"));
			pu.addQueryStatement("Balance", Balance_GetChecking);

		}
		/*
		 * 
		 * DepositChecking
		 * 
		 */
		if (txns.contains("DepositChecking")) {
			// retirve customer's id based on his/her name
			pu.mkTrnasaction("DepositChecking", "dc_custName:string", "dc_amount:int");
			WHC DepositChecking_GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_name"), BinOp.EQ, pu.getArg("dc_custName")));
			Select_Query DepositChecking_GetAccount0 = pu.addSelectQuery("DepositChecking", "accounts",
					DepositChecking_GetAccount0_WHC, "a_custid");
			pu.addQueryStatement("DepositChecking", DepositChecking_GetAccount0);

			// retrive customer's old checking balance
			WHC DepositChecking_GetChecking_WHC = new WHC(pu.getIsAliveFieldName("checking"),
					new WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ,
							pu.mkProjExpr("DepositChecking", 0, "a_custid", 1)));
			Select_Query DepositChecking_GetChecking = pu.addSelectQuery("DepositChecking", "checking",
					DepositChecking_GetChecking_WHC, "c_bal");
			pu.addQueryStatement("DepositChecking", DepositChecking_GetChecking);

			// write customer's new checking balance
			WHC DepositChecking_WHC = new WHC(pu.getIsAliveFieldName("checking"),
					new WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ,
							pu.mkProjExpr("DepositChecking", 0, "a_custid", 1)));
			Update_Query DepositChecking = pu.addUpdateQuery("DepositChecking", "checking", DepositChecking_WHC);
			DepositChecking.addUpdateExp(pu.getFieldName("c_bal"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr("DepositChecking", 1, "c_bal", 1), pu.getArg("dc_amount")));
			pu.addQueryStatement("DepositChecking", DepositChecking);
		}
		/*
		 * 
		 * SendPayment
		 * 
		 */
		if (txns.contains("SendPayment")) {
			// retrieve both accounts' names

			pu.mkTrnasaction("SendPayment", "sp_sendAcct:int", "sp_destAcct:int", "sp_amount:int");
			pu.mkAssertion("SendPayment",
					new E_UnOp(UnOp.NOT, new E_BinOp(BinOp.EQ, pu.getArg("sp_sendAcct"), pu.getArg("sp_destAcct"))));
			WHC SendPayment_GetAccount_send_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("sp_sendAcct")));
			Select_Query SendPayment_GetAccount_send = pu.addSelectQuery("SendPayment", "accounts",
					SendPayment_GetAccount_send_WHC, "a_name");
			SendPayment_GetAccount_send.setcanBeRemoved(false);
			pu.addQueryStatement("SendPayment", SendPayment_GetAccount_send);

			WHC SendPayment_GetAccount_dest_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("sp_destAcct")));
			Select_Query SendPayment_GetAccount_dest = pu.addSelectQuery("SendPayment", "accounts",
					SendPayment_GetAccount_dest_WHC, "a_name");
			SendPayment_GetAccount_send.setcanBeRemoved(false);
			pu.addQueryStatement("SendPayment", SendPayment_GetAccount_dest);

			// retrieve sender's old checking balance
			WHC SendPayment_GetChecking_WHC = new WHC(pu.getIsAliveFieldName("checking"), new WHC_Constraint(
					pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("sp_sendAcct")));
			Select_Query SendPayment_GetChecking = pu.addSelectQuery("SendPayment", "checking",
					SendPayment_GetChecking_WHC, "c_bal");
			pu.addQueryStatement("SendPayment", SendPayment_GetChecking);

			// if the balance is greater than amount
			Expression SendPayment_IF1_C = new E_BinOp(BinOp.GT, pu.mkProjExpr("SendPayment", 2, "c_bal", 1),
					pu.getArg("sp_amount"));
			pu.addIfStatement("SendPayment", SendPayment_IF1_C);

			// update sender's checking
			WHC SendPayment_U1_WHC = new WHC(pu.getIsAliveFieldName("checking"), new WHC_Constraint(
					pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("sp_sendAcct")));
			Update_Query SendPayment_U1 = pu.addUpdateQuery("SendPayment", "checking", SendPayment_U1_WHC);
			SendPayment_U1.addUpdateExp(pu.getFieldName("c_bal"),
					new E_BinOp(BinOp.MINUS, pu.mkProjExpr("SendPayment", 2, "c_bal", 1), pu.getArg("sp_amount")));
			pu.addQueryStatementInIf("SendPayment", 0, SendPayment_U1);

			// retrieve dest's old checking balance
			WHC SendPayment_GetChecking_dest_WHC = new WHC(pu.getIsAliveFieldName("checking"), new WHC_Constraint(
					pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("sp_destAcct")));
			Select_Query SendPayment_GetChecking_dest = pu.addSelectQuery("SendPayment", "checking",
					SendPayment_GetChecking_dest_WHC, "c_bal");
			pu.addQueryStatementInIf("SendPayment", 0, SendPayment_GetChecking_dest);

			// write dest's new checking balance
			WHC SendPayment_U1_dest_WHC = new WHC(pu.getIsAliveFieldName("checking"), new WHC_Constraint(
					pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("sp_destAcct")));
			Update_Query SendPayment_U1_dest = pu.addUpdateQuery("SendPayment", "checking", SendPayment_U1_dest_WHC);
			SendPayment_U1_dest.addUpdateExp(pu.getFieldName("c_bal"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr("SendPayment", 3, "c_bal", 1), pu.getArg("sp_amount")));
			pu.addQueryStatementInIf("SendPayment", 0, SendPayment_U1_dest);
		}
		/*
		 * 
		 * TransactSavings
		 * 
		 */
		if (txns.contains("TransactSavings")) {
			pu.mkTrnasaction("TransactSavings", "ts_custName:string", "ts_amount:int");
			// retrieve customer's id based on his/her name
			WHC TransactSavings_GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_name"), BinOp.EQ, pu.getArg("ts_custName")));
			Select_Query TransactSavings_GetAccount0 = pu.addSelectQuery("TransactSavings", "accounts",
					TransactSavings_GetAccount0_WHC, "a_custid");
			
			pu.addQueryStatement("TransactSavings", TransactSavings_GetAccount0);

			// retrieve customer's old savings balance
			WHC TransactSavings_GetSavings_WHC = new WHC(pu.getIsAliveFieldName("savings"),
					new WHC_Constraint(pu.getTableName("savings"), pu.getFieldName("s_custid"), BinOp.EQ,
							pu.mkProjExpr("TransactSavings", 0, "a_custid", 1)));
			Select_Query TransactSavings_GetSavings = pu.addSelectQuery("TransactSavings", "savings",
					TransactSavings_GetSavings_WHC, "s_bal");
			pu.addQueryStatement("TransactSavings", TransactSavings_GetSavings);

			// if the balance is larger than amount
			Expression TransactSavings_IF1_C = new E_BinOp(BinOp.GT, pu.mkProjExpr("TransactSavings", 1, "s_bal", 1),
					pu.getArg("ts_amount"));
			pu.addIfStatement("TransactSavings", TransactSavings_IF1_C);

			// write customer's new saving's balance
			WHC TransactSavings_U1_WHC = new WHC(pu.getIsAliveFieldName("savings"),
					new WHC_Constraint(pu.getTableName("savings"), pu.getFieldName("s_custid"), BinOp.EQ,
							pu.mkProjExpr("TransactSavings", 0, "a_custid", 1)));
			Update_Query TransactSavings_U1 = pu.addUpdateQuery("TransactSavings", "savings", TransactSavings_U1_WHC);
			TransactSavings_U1.addUpdateExp(pu.getFieldName("s_bal"),
					new E_BinOp(BinOp.MINUS, pu.mkProjExpr("TransactSavings", 1, "s_bal", 1), pu.getArg("ts_amount")));
			pu.addQueryStatementInIf("TransactSavings", 0, TransactSavings_U1);
		}
		/*
		 * 
		 * WriteCheck
		 * 
		 */
		if (txns.contains("WriteCheck")) {
			pu.mkTrnasaction("WriteCheck", "wc_custName:string", "wc_amount:int");
			// retrive customer's id based on his/her name
			WHC WriteCheck_GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_name"), BinOp.EQ, pu.getArg("wc_custName")));
			Select_Query WriteCheck_GetAccount0 = pu.addSelectQuery("WriteCheck", "accounts",
					WriteCheck_GetAccount0_WHC, "a_custid");
			pu.addQueryStatement("WriteCheck", WriteCheck_GetAccount0);

			// get their checkinbg balance
			WHC WriteCheck_GetChecking_WHC = new WHC(pu.getIsAliveFieldName("checking"),
					new WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ,
							pu.mkProjExpr("WriteCheck", 0, "a_custid", 1)));
			Select_Query WriteCheck_GetChecking = pu.addSelectQuery("WriteCheck", "checking",
					WriteCheck_GetChecking_WHC, "c_bal");
			pu.addQueryStatement("WriteCheck", WriteCheck_GetChecking);
			// get their savings balance
			WHC WriteCheck_GetSavings_WHC = new WHC(pu.getIsAliveFieldName("savings"),
					new WHC_Constraint(pu.getTableName("savings"), pu.getFieldName("s_custid"), BinOp.EQ,
							pu.mkProjExpr("WriteCheck", 0, "a_custid", 1)));
			Select_Query WriteCheck_GetSavings = pu.addSelectQuery("WriteCheck", "savings", WriteCheck_GetSavings_WHC,
					"s_bal");
			pu.addQueryStatement("WriteCheck", WriteCheck_GetSavings);

			// if the total of balances is high enough
			E_BinOp total = new E_BinOp(BinOp.PLUS, pu.mkProjExpr("WriteCheck", 1, "c_bal", 1),
					pu.mkProjExpr("WriteCheck", 2, "s_bal", 1));
			Expression WriteCheck_IF1_C = new E_BinOp(BinOp.GT, total, pu.getArg("wc_amount"));
			pu.addIfStatement("WriteCheck", WriteCheck_IF1_C);
			// update their checking
			WHC WriteCheck_U1_dest_WHC = new WHC(pu.getIsAliveFieldName("checking"),
					new WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ,
							pu.mkProjExpr("WriteCheck", 0, "a_custid", 1)));
			Update_Query WriteCheck_U1_dest = pu.addUpdateQuery("WriteCheck", "checking", WriteCheck_U1_dest_WHC);
			WriteCheck_U1_dest.addUpdateExp(pu.getFieldName("c_bal"),
					new E_BinOp(BinOp.MINUS, pu.mkProjExpr("WriteCheck", 1, "c_bal", 1), pu.getArg("wc_amount")));
			pu.addQueryStatementInIf("WriteCheck", 0, WriteCheck_U1_dest);

			// else: update their checking
			WHC WriteCheck_U1_dest_WHC_else = new WHC(pu.getIsAliveFieldName("checking"),
					new WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ,
							pu.mkProjExpr("WriteCheck", 0, "a_custid", 1)));
			Update_Query WriteCheck_U1_dest_else = pu.addUpdateQuery("WriteCheck", "checking",
					WriteCheck_U1_dest_WHC_else);
			E_BinOp penalty = new E_BinOp(BinOp.PLUS, pu.getArg("wc_amount"), new E_Const_Num(1));
			WriteCheck_U1_dest_else.addUpdateExp(pu.getFieldName("c_bal"),
					new E_BinOp(BinOp.MINUS, pu.mkProjExpr("WriteCheck", 1, "c_bal", 1), penalty));
			pu.addQueryStatementInElse("WriteCheck", 0, WriteCheck_U1_dest_else);
		}
		return pu.generateProgram();
	}
}
