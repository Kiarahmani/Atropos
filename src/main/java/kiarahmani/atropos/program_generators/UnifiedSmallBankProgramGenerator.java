package kiarahmani.atropos.program_generators;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_BinUp;
import kiarahmani.atropos.DML.expression.E_UUID;
import kiarahmani.atropos.DML.expression.E_UnOp;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.E_UnOp.UnOp;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.query.Delete_Query;
import kiarahmani.atropos.DML.query.Insert_Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.utils.Program_Utils;

public class UnifiedSmallBankProgramGenerator implements ProgramGenerator {

	/*
	 * 
	 * SMALLBANK APPLICATION (FROM OLTPBENCH) GENERATOR
	 * https://github.com/oltpbenchmark/oltpbench/tree/master/src/com/oltpbenchmark/
	 * benchmarks/smallbank
	 * 
	 */

	public Program generate(String... args) {
		/*
		 * 
		 * Tables
		 * 
		 */
		ArrayList<String> txns = new ArrayList<>();
		for (String txn : args)
			txns.add(txn);

		Program_Utils pu = new Program_Utils("SmallBank");
		pu.addTable("accounts", new FieldName("custid", true, true, F_Type.NUM),
				new FieldName("name", false, false, F_Type.TEXT), new FieldName("checking", false, false, F_Type.NUM),
				new FieldName("savings", false, false, F_Type.NUM));
		/*
		 * 
		 * Amalgamate
		 * 
		 */
		if (txns.contains("Amalgamate")) {
			pu.addTrnasaction("Amalgamate", "am_custId0:int", "am_custId1:int");
			pu.addAssertion("Amalgamate", new E_UnOp(UnOp.NOT, new E_BinUp(BinOp.EQ, pu.getArg("am_custId1"), pu.getArg("am_custId0"))));
			// retrieve customer0's data by id
			WHC GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("custid"), BinOp.EQ, pu.getArg("am_custId0")));
			Select_Query GetAccount0 = pu.addSelectQuery("Amalgamate", "accounts", true, GetAccount0_WHC, "name",
					"savings", "checking");
			pu.addQueryStatement("Amalgamate", GetAccount0);
			// retrieve customer1's data by id
			WHC GetAccount1_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("custid"), BinOp.EQ, pu.getArg("am_custId1")));
			Select_Query GetAccount1 = pu.addSelectQuery("Amalgamate", "accounts", true, GetAccount1_WHC, "name",
					"savings");
			pu.addQueryStatement("Amalgamate", GetAccount1);

			// zero cust0's savings and checking balance
			WHC ZeroCheckingBalance_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("custid"), BinOp.EQ, pu.getArg("am_custId0")));
			Update_Query ZeroCheckingBalance = pu.addUpdateQuery("Amalgamate", "accounts", true,
					ZeroCheckingBalance_WHC);
			ZeroCheckingBalance.addUpdateExp(pu.getFieldName("checking"), new E_Const_Num(0));
			ZeroCheckingBalance.addUpdateExp(pu.getFieldName("savings"), new E_Const_Num(0));
			pu.addQueryStatement("Amalgamate", ZeroCheckingBalance);

			// incremenet cust1's savings balance
			WHC UpdateSavingsBalance_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("custid"), BinOp.EQ, pu.getArg("am_custId1")));
			Update_Query UpdateSavingsBalance = pu.addUpdateQuery("Amalgamate", "accounts", true,
					UpdateSavingsBalance_WHC);
			UpdateSavingsBalance.addUpdateExp(pu.getFieldName("savings"), new E_BinUp(BinOp.PLUS,
					pu.getProjExpr("Amalgamate", 0, "savings", 1), pu.getProjExpr("Amalgamate", 0, "checking", 1)));
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
					pu.getTableName("accounts"), pu.getFieldName("name"), BinOp.EQ, pu.getArg("ba_custName")));
			Select_Query Balance_GetAccount0 = pu.addSelectQuery("Balance", "accounts", false, Balance_GetAccount0_WHC,
					"custid", "savings", "checking");
			pu.addQueryStatement("Balance", Balance_GetAccount0);

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
					pu.getTableName("accounts"), pu.getFieldName("name"), BinOp.EQ, pu.getArg("dc_custName")));
			Select_Query DepositChecking_GetAccount0 = pu.addSelectQuery("DepositChecking", "accounts", false,
					DepositChecking_GetAccount0_WHC, "custid", "checking");
			pu.addQueryStatement("DepositChecking", DepositChecking_GetAccount0);

			// write customer's new checking balance
			WHC DepositChecking_WHC = new WHC(pu.getIsAliveFieldName("accounts"),
					new WHC_Constraint(pu.getTableName("accounts"), pu.getFieldName("custid"), BinOp.EQ,
							pu.getProjExpr("DepositChecking", 0, "custid", 1)));
			Update_Query DepositChecking = pu.addUpdateQuery("DepositChecking", "accounts", true, DepositChecking_WHC);
			DepositChecking.addUpdateExp(pu.getFieldName("checking"), new E_BinUp(BinOp.PLUS,
					pu.getProjExpr("DepositChecking", 0, "checking", 1), pu.getArg("dc_amount")));
			pu.addQueryStatement("DepositChecking", DepositChecking);
		}
		/*
		 * 
		 * SendPayment
		 * 
		 */
		if (txns.contains("SendPayment")) {
			pu.addTrnasaction("SendPayment", "sp_sendAcct:int", "sp_destAcct:int", "sp_amount:int");
			pu.addAssertion("SendPayment", new E_UnOp(UnOp.NOT, new E_BinUp(BinOp.EQ, pu.getArg("sp_sendAcct"), pu.getArg("sp_destAcct"))));
			// retrieve sender accounts' data
			WHC SendPayment_GetAccount_send_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("custid"), BinOp.EQ, pu.getArg("sp_sendAcct")));
			Select_Query SendPayment_GetAccount_send = pu.addSelectQuery("SendPayment", "accounts", true,
					SendPayment_GetAccount_send_WHC, "name", "checking");
			pu.addQueryStatement("SendPayment", SendPayment_GetAccount_send);

			// retrieve destination account's data
			WHC SendPayment_GetAccount_dest_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("custid"), BinOp.EQ, pu.getArg("sp_destAcct")));
			Select_Query SendPayment_GetAccount_dest = pu.addSelectQuery("SendPayment", "accounts", true,
					SendPayment_GetAccount_dest_WHC, "name", "checking");
			pu.addQueryStatement("SendPayment", SendPayment_GetAccount_dest);

			// if the sender's checking balance is greater than amount
			Expression SendPayment_IF1_C = new E_BinUp(BinOp.GT, pu.getProjExpr("SendPayment", 0, "checking", 1),
					pu.getArg("sp_amount"));
			pu.addIfStatement("SendPayment", SendPayment_IF1_C);
			// update sender's checking
			WHC SendPayment_U1_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("custid"), BinOp.EQ, pu.getArg("sp_sendAcct")));
			Update_Query SendPayment_U1 = pu.addUpdateQuery("SendPayment", "accounts", true, SendPayment_U1_WHC);
			SendPayment_U1.addUpdateExp(pu.getFieldName("checking"),
					new E_BinUp(BinOp.MINUS, pu.getProjExpr("SendPayment", 0, "checking", 1), pu.getArg("sp_amount")));
			pu.addQueryStatementInIf("SendPayment", 0, SendPayment_U1);

			// write dest's new checking balance
			WHC SendPayment_U1_dest_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("custid"), BinOp.EQ, pu.getArg("sp_destAcct")));
			Update_Query SendPayment_U1_dest = pu.addUpdateQuery("SendPayment", "accounts", true,
					SendPayment_U1_dest_WHC);
			SendPayment_U1_dest.addUpdateExp(pu.getFieldName("checking"),
					new E_BinUp(BinOp.PLUS, pu.getProjExpr("SendPayment", 1, "checking", 1), pu.getArg("sp_amount")));
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
					pu.getTableName("accounts"), pu.getFieldName("name"), BinOp.EQ, pu.getArg("ts_custName")));
			Select_Query TransactSavings_GetAccount0 = pu.addSelectQuery("TransactSavings", "accounts", false,
					TransactSavings_GetAccount0_WHC, "custid", "savings");
			pu.addQueryStatement("TransactSavings", TransactSavings_GetAccount0);

			// if the balance is larger than amount
			Expression TransactSavings_IF1_C = new E_BinUp(BinOp.GT, pu.getProjExpr("TransactSavings", 0, "savings", 1),
					pu.getArg("ts_amount"));
			pu.addIfStatement("TransactSavings", TransactSavings_IF1_C);

			// write customer's new saving's balance
			WHC TransactSavings_U1_WHC = new WHC(pu.getIsAliveFieldName("accounts"),
					new WHC_Constraint(pu.getTableName("accounts"), pu.getFieldName("savings"), BinOp.EQ,
							pu.getProjExpr("TransactSavings", 0, "custid", 1)));
			Update_Query TransactSavings_U1 = pu.addUpdateQuery("TransactSavings", "accounts", true,
					TransactSavings_U1_WHC);
			TransactSavings_U1.addUpdateExp(pu.getFieldName("savings"), new E_BinUp(BinOp.MINUS,
					pu.getProjExpr("TransactSavings", 0, "savings", 1), pu.getArg("ts_amount")));
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
					pu.getTableName("accounts"), pu.getFieldName("name"), BinOp.EQ, pu.getArg("wc_custName")));
			Select_Query WriteCheck_GetAccount0 = pu.addSelectQuery("WriteCheck", "accounts", false,
					WriteCheck_GetAccount0_WHC, "custid", "checking", "savings");
			pu.addQueryStatement("WriteCheck", WriteCheck_GetAccount0);

			// if the total of balances is high enough
			E_BinUp total = new E_BinUp(BinOp.PLUS, pu.getProjExpr("WriteCheck", 0, "checking", 1),
					pu.getProjExpr("WriteCheck", 0, "savings", 1));
			Expression WriteCheck_IF1_C = new E_BinUp(BinOp.GT, total, pu.getArg("wc_amount"));
			pu.addIfStatement("WriteCheck", WriteCheck_IF1_C);
			// update their checking
			WHC WriteCheck_U1_dest_WHC = new WHC(pu.getIsAliveFieldName("accounts"),
					new WHC_Constraint(pu.getTableName("accounts"), pu.getFieldName("custid"), BinOp.EQ,
							pu.getProjExpr("WriteCheck", 0, "custid", 1)));
			Update_Query WriteCheck_U1_dest = pu.addUpdateQuery("WriteCheck", "accounts", true, WriteCheck_U1_dest_WHC);
			WriteCheck_U1_dest.addUpdateExp(pu.getFieldName("checking"),
					new E_BinUp(BinOp.MINUS, pu.getProjExpr("WriteCheck", 0, "checking", 1), pu.getArg("wc_amount")));
			pu.addQueryStatementInIf("WriteCheck", 0, WriteCheck_U1_dest);

			// else: update their checking
			WHC WriteCheck_U1_dest_WHC_else = new WHC(pu.getIsAliveFieldName("accounts"),
					new WHC_Constraint(pu.getTableName("accounts"), pu.getFieldName("custid"), BinOp.EQ,
							pu.getProjExpr("WriteCheck", 0, "custid", 1)));
			Update_Query WriteCheck_U1_dest_else = pu.addUpdateQuery("WriteCheck", "accounts", true,
					WriteCheck_U1_dest_WHC_else);
			E_BinUp penalty = new E_BinUp(BinOp.PLUS, pu.getArg("wc_amount"), new E_Const_Num(1));
			WriteCheck_U1_dest_else.addUpdateExp(pu.getFieldName("checking"),
					new E_BinUp(BinOp.MINUS, pu.getProjExpr("WriteCheck", 0, "checking", 1), penalty));
			pu.addQueryStatementInElse("WriteCheck", 0, WriteCheck_U1_dest_else);
		}
		return pu.getProgram();

	}

}
