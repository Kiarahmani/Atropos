package kiarahmani.atropos;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_BinUp;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.expression.constants.E_Const_Text;
import kiarahmani.atropos.DML.query.Delete_Query;
import kiarahmani.atropos.DML.query.Insert_Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.utils.Program_Utils;

public class InputProgramGenerator {

	/*
	 * 
	 * 
	 * UNIT TESTING APPLICATION GENERATOR
	 * 
	 * 
	 */
	public Program generateUnitTestProgram(String... args) {

		ArrayList<String> txns = new ArrayList<>();
		for (String txn : args)
			txns.add(txn);

		Program_Utils pu = new Program_Utils("unit test");

		pu.addTable("accs", new FieldName("key", true, true, F_Type.NUM),
				new FieldName("name", false, false, F_Type.TEXT), new FieldName("value", false, false, F_Type.NUM));

		if (txns.contains("txn")) {
			// dec transaction
			pu.addTrnasaction("txn", "arg_id:int", "arg_amnt:int");
			WHC DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(pu.getTableName("table"),
					pu.getFieldName("key"), BinOp.EQ, pu.getArg("arg_id")));
			Select_Query DEC_S1 = pu.addSelectQuery("txn", "table", true, DEC_S1_WHC, "name", "value");
			pu.addQueryStatement("txn", DEC_S1);

			Expression DEC_IF1_C = new E_BinUp(BinOp.GT, pu.getProjExpr("txn", 0, "value", 1), pu.getArg("arg_amnt"));
			pu.addIfStatement("txn", DEC_IF1_C);

			WHC DEC_U1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(pu.getTableName("table"),
					pu.getFieldName("key"), BinOp.EQ, pu.getArg("arg_id")));
			Update_Query DEC_U1 = pu.addUpdateQuery("txn", "table", true, DEC_U1_WHC);
			DEC_U1.addUpdateExp(pu.getFieldName("value"),
					new E_BinUp(BinOp.MINUS, pu.getProjExpr("txn", 0, "value", 1), pu.getArg("arg_amnt")));
			pu.addQueryStatementInIf("txn", 0, DEC_U1);

			Update_Query DEC_U2 = pu.addUpdateQuery("txn", "table", true, DEC_U1_WHC);
			DEC_U2.addUpdateExp(pu.getFieldName("value"),
					new E_BinUp(BinOp.MINUS, pu.getProjExpr("txn", 0, "value", 1), new E_Const_Num(10)));
			pu.addQueryStatementInElse("txn", 0, DEC_U2);
		}

		if (txns.contains("increments")) {
			pu.addTrnasaction("increments", "inc_id:int", "inc_amnt:int");
			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(
					pu.getTableName("table"), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_id")));
			Select_Query increments_DEC_S1 = pu.addSelectQuery("increments", "table", true, increments_DEC_S1_WHC,
					"name", "value");
			pu.addQueryStatement("increments", increments_DEC_S1);

			WHC increments_DEC_U1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(
					pu.getTableName("table"), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_id")));
			Update_Query increments_DEC_U1 = pu.addUpdateQuery("increments", "table", true, increments_DEC_U1_WHC);
			increments_DEC_U1.addUpdateExp(pu.getFieldName("value"),
					new E_BinUp(BinOp.PLUS, pu.getProjExpr("increments", 0, "value", 1), pu.getArg("inc_amnt")));
			pu.addQueryStatement("increments", increments_DEC_U1);
		}

		if (txns.contains("insert_increments")) {
			pu.addTrnasaction("increments", "inc_id:int", "inc_amnt:int");
			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"),
					new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"), BinOp.EQ,new E_Const_Num(68)));
			Select_Query increments_DEC_S1 = pu.addSelectQuery("increments", "accs", true, increments_DEC_S1_WHC,
					"value");
			pu.addQueryStatement("increments", increments_DEC_S1);
			// update
			//WHC increments_DEC_U1_WHC = new WHC(pu.getIsAliveFieldName("accs"),
			//		new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_id")));
			//Update_Query increments_DEC_U1 = pu.addUpdateQuery("increments", "accs", true, increments_DEC_U1_WHC);
			//increments_DEC_U1.addUpdateExp(pu.getFieldName("value"),
			//		new E_BinUp(BinOp.PLUS, pu.getProjExpr("increments", 0, "value", 1), pu.getArg("inc_amnt")));
			//pu.addQueryStatement("increments", increments_DEC_U1);
			// delete
			WHC increments_DEC_U1_WHC1 = new WHC(pu.getIsAliveFieldName("accs"),
					new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"), BinOp.EQ,
							new E_BinUp(BinOp.PLUS,new E_Const_Num(67), new E_Const_Num(1))));
			Delete_Query increments_DEC_U11 = pu.addDeleteQuery("increments", "accs", true, increments_DEC_U1_WHC1);
			pu.addQueryStatement("increments", increments_DEC_U11);
			// insert
			WHC_Constraint increments_DEC_U1_WHC2 = new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"),
					BinOp.EQ,new E_Const_Num(69));
			Insert_Query increments_DEC_U12 = pu.addInsertQuery("increments", "accs", true, increments_DEC_U1_WHC2);
			increments_DEC_U12.addInsertExp(pu.getFieldName("value"), pu.getArg("inc_amnt"));
			increments_DEC_U12.addInsertExp(pu.getFieldName("name"), new E_Const_Text("kia"));
			pu.addQueryStatement("increments", increments_DEC_U12);

		}

		return pu.getProgram();
	}

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
	 * SMALLBANK APPLICATION (FROM OLTPBENCH) GENERATOR
	 * https://github.com/oltpbenchmark/oltpbench/tree/master/src/com/oltpbenchmark/
	 * benchmarks/smallbank
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
	public Program generateSmallBankProgram(String... args) {
		/*
		 * 
		 * Tables
		 * 
		 */
		ArrayList<String> txns = new ArrayList<>();
		for (String txn : args)
			txns.add(txn);

		Program_Utils pu = new Program_Utils("SmallBank");
		pu.addTable("accounts", new FieldName("a_custid", true, true, F_Type.NUM),
				new FieldName("a_name", false, false, F_Type.TEXT));
		pu.addTable("savings", new FieldName("s_custid", true, true, F_Type.NUM),
				new FieldName("s_bal", false, false, F_Type.NUM));
		pu.addTable("checking", new FieldName("c_custid", true, true, F_Type.NUM),
				new FieldName("c_bal", false, false, F_Type.NUM));
		/*
		 * 
		 * Amalgamate
		 * 
		 */
		if (txns.contains("Amalgamate")) {
			pu.addTrnasaction("Amalgamate", "am_custId0:int", "am_custId1:int");
			// retrieve customer0's name by id
			WHC GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("am_custId0")));
			Select_Query GetAccount0 = pu.addSelectQuery("Amalgamate", "accounts", true, GetAccount0_WHC, "a_name");
			pu.addQueryStatement("Amalgamate", GetAccount0);
			// retrieve customer1's name by id
			WHC GetAccount1_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("am_custId1")));
			Select_Query GetAccount1 = pu.addSelectQuery("Amalgamate", "accounts", true, GetAccount1_WHC, "a_name");
			pu.addQueryStatement("Amalgamate", GetAccount1);

			// retrieve savings balance of cust0
			WHC GetSavings0_WHC = new WHC(pu.getIsAliveFieldName("savings"), new WHC_Constraint(
					pu.getTableName("savings"), pu.getFieldName("s_custid"), BinOp.EQ, pu.getArg("am_custId0")));
			Select_Query GetSavings0 = pu.addSelectQuery("Amalgamate", "savings", true, GetSavings0_WHC, "s_bal");
			pu.addQueryStatement("Amalgamate", GetSavings0);

			// retrieve checking balance of cust1
			WHC GetChecking1_WHC = new WHC(pu.getIsAliveFieldName("checking"), new WHC_Constraint(
					pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("am_custId1")));
			Select_Query GetChecking1 = pu.addSelectQuery("Amalgamate", "checking", true, GetChecking1_WHC, "c_bal");
			pu.addQueryStatement("Amalgamate", GetChecking1);

			// zero cust0's savings balance
			WHC ZeroCheckingBalance_WHC = new WHC(pu.getIsAliveFieldName("checking"), new WHC_Constraint(
					pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("am_custId0")));
			Update_Query ZeroCheckingBalance = pu.addUpdateQuery("Amalgamate", "checking", true,
					ZeroCheckingBalance_WHC);
			ZeroCheckingBalance.addUpdateExp(pu.getFieldName("c_bal"), new E_Const_Num(0));
			pu.addQueryStatement("Amalgamate", ZeroCheckingBalance);

			// incremenet cust1's savings balance
			WHC UpdateSavingsBalance_WHC = new WHC(pu.getIsAliveFieldName("savings"), new WHC_Constraint(
					pu.getTableName("savings"), pu.getFieldName("s_custid"), BinOp.EQ, pu.getArg("am_custId1")));
			Update_Query UpdateSavingsBalance = pu.addUpdateQuery("Amalgamate", "savings", true,
					UpdateSavingsBalance_WHC);
			UpdateSavingsBalance.addUpdateExp(pu.getFieldName("s_bal"), new E_BinUp(BinOp.PLUS,
					pu.getProjExpr("Amalgamate", 2, "s_bal", 1), pu.getProjExpr("Amalgamate", 3, "c_bal", 1)));
			pu.addQueryStatement("Amalgamate", UpdateSavingsBalance);
		}
		/*
		 * 
		 * Balance
		 * 
		 */

		if (txns.contains("Balance")) {
			pu.addTrnasaction("Balance", "ba_custName:string");
			// get customer's id based on his/her name
			WHC Balance_GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_name"), BinOp.EQ, pu.getArg("ba_custName")));
			Select_Query Balance_GetAccount0 = pu.addSelectQuery("Balance", "accounts", false, Balance_GetAccount0_WHC,
					"a_custid");
			pu.addQueryStatement("Balance", Balance_GetAccount0);

			// retrieve customer's savings balance based on the retrieved id
			WHC Balance_GetSavings_WHC = new WHC(pu.getIsAliveFieldName("savings"),
					new WHC_Constraint(pu.getTableName("savings"), pu.getFieldName("s_custid"), BinOp.EQ,
							pu.getProjExpr("Balance", 0, "a_custid", 1)));
			Select_Query Balance_GetSavings = pu.addSelectQuery("Balance", "savings", true, Balance_GetSavings_WHC,
					"s_bal");
			pu.addQueryStatement("Balance", Balance_GetSavings);

			// retrieve customer's checking balance based on the retrieved id
			WHC Balance_GetChecking_WHC = new WHC(pu.getIsAliveFieldName("checking"),
					new WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ,
							pu.getProjExpr("Balance", 0, "a_custid", 1)));
			Select_Query Balance_GetChecking = pu.addSelectQuery("Balance", "checking", true, Balance_GetChecking_WHC,
					"c_bal");
			pu.addQueryStatement("Balance", Balance_GetChecking);
		}
		/*
		 * 
		 * DepositChecking
		 * 
		 */
		if (txns.contains("DepositChecking")) {
			// retirve customer's id based on his/her name
			pu.addTrnasaction("DepositChecking", "dc_custName:string", "dc_amount:int");
			WHC DepositChecking_GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_name"), BinOp.EQ, pu.getArg("dc_custName")));
			Select_Query DepositChecking_GetAccount0 = pu.addSelectQuery("DepositChecking", "accounts", false,
					DepositChecking_GetAccount0_WHC, "a_custid");
			pu.addQueryStatement("DepositChecking", DepositChecking_GetAccount0);

			// retrive customer's old checking balance
			WHC DepositChecking_GetChecking_WHC = new WHC(pu.getIsAliveFieldName("checking"),
					new WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ,
							pu.getProjExpr("DepositChecking", 0, "a_custid", 1)));
			Select_Query DepositChecking_GetChecking = pu.addSelectQuery("DepositChecking", "checking", true,
					DepositChecking_GetChecking_WHC, "c_bal");
			pu.addQueryStatement("DepositChecking", DepositChecking_GetChecking);

			// write customer's new checking balance
			WHC DepositChecking_WHC = new WHC(pu.getIsAliveFieldName("checking"),
					new WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ,
							pu.getProjExpr("DepositChecking", 0, "a_custid", 1)));
			Update_Query DepositChecking = pu.addUpdateQuery("DepositChecking", "checking", true, DepositChecking_WHC);
			DepositChecking.addUpdateExp(pu.getFieldName("c_bal"),
					new E_BinUp(BinOp.PLUS, pu.getProjExpr("DepositChecking", 1, "c_bal", 1), pu.getArg("dc_amount")));
			pu.addQueryStatement("DepositChecking", DepositChecking);
		}
		/*
		 * 
		 * SendPayment
		 * 
		 */
		if (txns.contains("SendPayment")) {
			// retrieve both accounts' names

			pu.addTrnasaction("SendPayment", "sp_sendAcct:int", "sp_destAcct:int", "sp_amount:int");
			WHC SendPayment_GetAccount_send_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("sp_sendAcct")));
			Select_Query SendPayment_GetAccount_send = pu.addSelectQuery("SendPayment", "accounts", true,
					SendPayment_GetAccount_send_WHC, "a_name");
			pu.addQueryStatement("SendPayment", SendPayment_GetAccount_send);

			WHC SendPayment_GetAccount_dest_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("sp_destAcct")));
			Select_Query SendPayment_GetAccount_dest = pu.addSelectQuery("SendPayment", "accounts", true,
					SendPayment_GetAccount_dest_WHC, "a_name");
			pu.addQueryStatement("SendPayment", SendPayment_GetAccount_dest);

			// retrieve sender's old checking balance
			WHC SendPayment_GetChecking_WHC = new WHC(pu.getIsAliveFieldName("checking"), new WHC_Constraint(
					pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("sp_sendAcct")));
			Select_Query SendPayment_GetChecking = pu.addSelectQuery("SendPayment", "checking", true,
					SendPayment_GetChecking_WHC, "c_bal");
			pu.addQueryStatement("SendPayment", SendPayment_GetChecking);

			// if the balance is greater than amount
			Expression SendPayment_IF1_C = new E_BinUp(BinOp.GT, pu.getProjExpr("SendPayment", 2, "c_bal", 1),
					pu.getArg("sp_amount"));
			pu.addIfStatement("SendPayment", SendPayment_IF1_C);

			// update sender's checking
			WHC SendPayment_U1_WHC = new WHC(pu.getIsAliveFieldName("checking"), new WHC_Constraint(
					pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("sp_sendAcct")));
			Update_Query SendPayment_U1 = pu.addUpdateQuery("SendPayment", "checking", true, SendPayment_U1_WHC);
			SendPayment_U1.addUpdateExp(pu.getFieldName("c_bal"),
					new E_BinUp(BinOp.MINUS, pu.getProjExpr("SendPayment", 2, "c_bal", 1), pu.getArg("sp_amount")));
			pu.addQueryStatementInIf("SendPayment", 0, SendPayment_U1);

			// retrieve dest's old checking balance
			WHC SendPayment_GetChecking_dest_WHC = new WHC(pu.getIsAliveFieldName("checking"), new WHC_Constraint(
					pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("sp_destAcct")));
			Select_Query SendPayment_GetChecking_dest = pu.addSelectQuery("SendPayment", "checking", true,
					SendPayment_GetChecking_dest_WHC, "c_bal");
			pu.addQueryStatementInIf("SendPayment", 0, SendPayment_GetChecking_dest);

			// write dest's new checking balance
			WHC SendPayment_U1_dest_WHC = new WHC(pu.getIsAliveFieldName("checking"), new WHC_Constraint(
					pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("sp_destAcct")));
			Update_Query SendPayment_U1_dest = pu.addUpdateQuery("SendPayment", "checking", true,
					SendPayment_U1_dest_WHC);
			SendPayment_U1_dest.addUpdateExp(pu.getFieldName("c_bal"),
					new E_BinUp(BinOp.PLUS, pu.getProjExpr("SendPayment", 3, "c_bal", 1), pu.getArg("sp_amount")));
			pu.addQueryStatementInIf("SendPayment", 0, SendPayment_U1_dest);
		}
		/*
		 * 
		 * TransactSavings
		 * 
		 */
		if (txns.contains("TransactSavings")) {
			pu.addTrnasaction("TransactSavings", "ts_custName:string", "ts_amount:int");
			// retrieve customer's id based on his/her name
			WHC TransactSavings_GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_name"), BinOp.EQ, pu.getArg("ts_custName")));
			Select_Query TransactSavings_GetAccount0 = pu.addSelectQuery("TransactSavings", "accounts", false,
					TransactSavings_GetAccount0_WHC, "a_custid");
			pu.addQueryStatement("TransactSavings", TransactSavings_GetAccount0);

			// retrieve customer's old savings balance
			WHC TransactSavings_GetSavings_WHC = new WHC(pu.getIsAliveFieldName("savings"),
					new WHC_Constraint(pu.getTableName("savings"), pu.getFieldName("s_custid"), BinOp.EQ,
							pu.getProjExpr("TransactSavings", 0, "a_custid", 1)));
			Select_Query TransactSavings_GetSavings = pu.addSelectQuery("TransactSavings", "savings", true,
					TransactSavings_GetSavings_WHC, "s_bal");
			pu.addQueryStatement("TransactSavings", TransactSavings_GetSavings);

			// if the balance is larger than amount
			Expression TransactSavings_IF1_C = new E_BinUp(BinOp.GT, pu.getProjExpr("TransactSavings", 1, "s_bal", 1),
					pu.getArg("ts_amount"));
			pu.addIfStatement("TransactSavings", TransactSavings_IF1_C);

			// write customer's new saving's balance
			WHC TransactSavings_U1_WHC = new WHC(pu.getIsAliveFieldName("savings"),
					new WHC_Constraint(pu.getTableName("savings"), pu.getFieldName("s_custid"), BinOp.EQ,
							pu.getProjExpr("TransactSavings", 0, "a_custid", 1)));
			Update_Query TransactSavings_U1 = pu.addUpdateQuery("TransactSavings", "savings", true,
					TransactSavings_U1_WHC);
			TransactSavings_U1.addUpdateExp(pu.getFieldName("s_bal"),
					new E_BinUp(BinOp.MINUS, pu.getProjExpr("TransactSavings", 1, "s_bal", 1), pu.getArg("ts_amount")));
			pu.addQueryStatementInIf("TransactSavings", 0, TransactSavings_U1);
		}
		/*
		 * 
		 * WriteCheck
		 * 
		 */
		if (txns.contains("WriteCheck")) {
			pu.addTrnasaction("WriteCheck", "wc_custName:string", "wc_amount:int");
			// retrive customer's id based on his/her name
			WHC WriteCheck_GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_name"), BinOp.EQ, pu.getArg("wc_custName")));
			Select_Query WriteCheck_GetAccount0 = pu.addSelectQuery("WriteCheck", "accounts", false,
					WriteCheck_GetAccount0_WHC, "a_custid");
			pu.addQueryStatement("WriteCheck", WriteCheck_GetAccount0);

			// get their checkinbg balance
			WHC WriteCheck_GetChecking_WHC = new WHC(pu.getIsAliveFieldName("checking"),
					new WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ,
							pu.getProjExpr("WriteCheck", 0, "a_custid", 1)));
			Select_Query WriteCheck_GetChecking = pu.addSelectQuery("WriteCheck", "checking", true,
					WriteCheck_GetChecking_WHC, "c_bal");
			pu.addQueryStatement("WriteCheck", WriteCheck_GetChecking);
			// get their savings balance
			WHC WriteCheck_GetSavings_WHC = new WHC(pu.getIsAliveFieldName("savings"),
					new WHC_Constraint(pu.getTableName("savings"), pu.getFieldName("s_custid"), BinOp.EQ,
							pu.getProjExpr("WriteCheck", 0, "a_custid", 1)));
			Select_Query WriteCheck_GetSavings = pu.addSelectQuery("WriteCheck", "savings", true,
					WriteCheck_GetSavings_WHC, "s_bal");
			pu.addQueryStatement("WriteCheck", WriteCheck_GetSavings);

			// if the total of balances is high enough
			E_BinUp total = new E_BinUp(BinOp.PLUS, pu.getProjExpr("WriteCheck", 1, "c_bal", 1),
					pu.getProjExpr("WriteCheck", 2, "s_bal", 1));
			Expression WriteCheck_IF1_C = new E_BinUp(BinOp.GT, total, pu.getArg("wc_amount"));
			pu.addIfStatement("WriteCheck", WriteCheck_IF1_C);
			// update their checking
			WHC WriteCheck_U1_dest_WHC = new WHC(pu.getIsAliveFieldName("checking"),
					new WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ,
							pu.getProjExpr("WriteCheck", 0, "a_custid", 1)));
			Update_Query WriteCheck_U1_dest = pu.addUpdateQuery("WriteCheck", "checking", true, WriteCheck_U1_dest_WHC);
			WriteCheck_U1_dest.addUpdateExp(pu.getFieldName("c_bal"),
					new E_BinUp(BinOp.MINUS, pu.getProjExpr("WriteCheck", 1, "c_bal", 1), pu.getArg("wc_amount")));
			pu.addQueryStatementInIf("WriteCheck", 0, WriteCheck_U1_dest);

			// else: update their checking
			WHC WriteCheck_U1_dest_WHC_else = new WHC(pu.getIsAliveFieldName("checking"),
					new WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ,
							pu.getProjExpr("WriteCheck", 0, "a_custid", 1)));
			Update_Query WriteCheck_U1_dest_else = pu.addUpdateQuery("WriteCheck", "checking", true,
					WriteCheck_U1_dest_WHC_else);
			E_BinUp penalty = new E_BinUp(BinOp.PLUS, pu.getArg("wc_amount"), new E_Const_Num(1));
			WriteCheck_U1_dest_else.addUpdateExp(pu.getFieldName("c_bal"),
					new E_BinUp(BinOp.MINUS, pu.getProjExpr("WriteCheck", 1, "c_bal", 1), penalty));
			pu.addQueryStatementInElse("WriteCheck", 0, WriteCheck_U1_dest_else);
		}
		return pu.getProgram();

	}

}
