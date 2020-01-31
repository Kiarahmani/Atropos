package kiarahmani.atropos.program_generators.SmallBank;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_BinOp;
import kiarahmani.atropos.DML.expression.E_UUID;
import kiarahmani.atropos.DML.expression.E_UnOp;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.E_UnOp.UnOp;
import kiarahmani.atropos.DML.expression.constants.E_Const_Bool;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.query.Delete_Query;
import kiarahmani.atropos.DML.query.Insert_Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.ProgramGenerator;
import kiarahmani.atropos.utils.Program_Utils;

public class UnifiedCRDTSmallBankProgramGenerator  implements ProgramGenerator{

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
		pu.addTable("accounts", new FieldName("a_custid", true, true, F_Type.NUM),
				new FieldName("a_name", false, false, F_Type.TEXT), new FieldName("a_uuid", true, false, F_Type.NUM),
				new FieldName("a_bal", false, false, F_Type.NUM),
				new FieldName("is_checking", true, false, F_Type.BOOL));
		/*
		 * 
		 * Amalgamate
		 * 
		 */
		if (txns.contains("Amalgamate")) {
			String txn_name = "Amalgamate";
			String arg1 = "am_custId0:int";
			String arg2 = "am_custId1:int";
			pu.addTrnasaction(txn_name, arg1, arg2);
			pu.addAssertion(txn_name, new E_UnOp(UnOp.NOT, new E_BinOp(BinOp.EQ, pu.getArg("am_custId1"), pu.getArg("am_custId0"))));
			// retrieve customer0's data by id
			WHC GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("am_custId0")));
			Select_Query GetAccount0 = pu.addSelectQuery(txn_name, "accounts", true, GetAccount0_WHC, "a_name",
					"a_bal");
			pu.addQueryStatement(txn_name, GetAccount0);

			// retrieve customer1's name by id
			WHC GetAccount1_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("am_custId1")));
			Select_Query GetAccount1 = pu.addSelectQuery(txn_name, "accounts", true, GetAccount1_WHC, "a_name");
			pu.addQueryStatement(txn_name, GetAccount1);

			// zero saving of cust0
			WHC_Constraint ZeroCheckingBalance_WHC_1 = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("am_custId0"));
			WHC_Constraint ZeroCheckingBalance_WHC_2 = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_uuid"), BinOp.EQ, new E_UUID());
			WHC_Constraint ZeroCheckingBalance_WHC_3 = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("is_checking"), BinOp.EQ, new E_Const_Bool(false));
			Insert_Query ZeroCheckingBalance = pu.addInsertQuery(txn_name, "accounts", true, ZeroCheckingBalance_WHC_1,
					ZeroCheckingBalance_WHC_2, ZeroCheckingBalance_WHC_3);
			ZeroCheckingBalance.addInsertExp(pu.getFieldName("a_bal"),
					new E_BinOp(BinOp.MINUS, new E_Const_Num(0), pu.getProjExpr(txn_name, 0, "a_bal", 1)));
			pu.addQueryStatement(txn_name, ZeroCheckingBalance);

			// zero checking of cust0
			WHC_Constraint ZeroCheckingBalance_WHC_11 = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("am_custId0"));
			WHC_Constraint ZeroCheckingBalance_WHC_21 = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_uuid"), BinOp.EQ, new E_UUID());
			WHC_Constraint ZeroCheckingBalance_WHC_31 = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("is_checking"), BinOp.EQ, new E_Const_Bool(true));
			Insert_Query ZeroCheckingBalance1 = pu.addInsertQuery(txn_name, "accounts", true,
					ZeroCheckingBalance_WHC_11, ZeroCheckingBalance_WHC_21, ZeroCheckingBalance_WHC_31);
			ZeroCheckingBalance1.addInsertExp(pu.getFieldName("a_bal"),
					new E_BinOp(BinOp.MINUS, new E_Const_Num(0), pu.getProjExpr(txn_name, 0, "a_bal", 1)));
			pu.addQueryStatement(txn_name, ZeroCheckingBalance1);

			// update saving balance of cust1
			WHC_Constraint ZeroCheckingBalance_WHC_12 = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("am_custId1"));
			WHC_Constraint ZeroCheckingBalance_WHC_22 = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_uuid"), BinOp.EQ, new E_UUID());
			WHC_Constraint ZeroCheckingBalance_WHC_311 = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("is_checking"), BinOp.EQ, new E_Const_Bool(false));
			Insert_Query ZeroCheckingBalance2 = pu.addInsertQuery(txn_name, "accounts", true,
					ZeroCheckingBalance_WHC_12, ZeroCheckingBalance_WHC_22, ZeroCheckingBalance_WHC_311);
			ZeroCheckingBalance2.addInsertExp(pu.getFieldName("a_bal"), pu.getProjExpr(txn_name, 0, "a_bal", 1));
			pu.addQueryStatement(txn_name, ZeroCheckingBalance2);
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
					"a_custid", "a_bal");
			pu.addQueryStatement("Balance", Balance_GetAccount0);

		}
		/*
		 * 
		 * DepositChecking
		 * 
		 */
		if (txns.contains("DepositChecking")) {
			String txn_name = "DepositChecking";
			pu.addTrnasaction("DepositChecking", "dc_custName:string", "dc_amount:int");
			// retirve customer's id based on his/her name
			WHC DepositChecking_GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_name"), BinOp.EQ, pu.getArg("dc_custName")));
			Select_Query DepositChecking_GetAccount0 = pu.addSelectQuery("DepositChecking", "accounts", false,
					DepositChecking_GetAccount0_WHC, "a_custid");
			pu.addQueryStatement("DepositChecking", DepositChecking_GetAccount0);

			// update saving balance of cust1
			WHC_Constraint ZeroCheckingBalance_WHC_1 = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_custid"), BinOp.EQ, pu.getProjExpr(txn_name, 0, "a_custid", 1));
			WHC_Constraint ZeroCheckingBalance_WHC_2 = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_uuid"), BinOp.EQ, new E_UUID());
			WHC_Constraint ZeroCheckingBalance_WHC_3 = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("is_checking"), BinOp.EQ, new E_Const_Bool(false));
			Insert_Query ZeroCheckingBalance2 = pu.addInsertQuery(txn_name, "accounts", true, ZeroCheckingBalance_WHC_1,
					ZeroCheckingBalance_WHC_2, ZeroCheckingBalance_WHC_3);
			ZeroCheckingBalance2.addInsertExp(pu.getFieldName("a_bal"), pu.getArg("dc_amount"));
			ZeroCheckingBalance2.addInsertExp(pu.getFieldName("a_name"), pu.getArg("dc_custName"));
			pu.addQueryStatement(txn_name, ZeroCheckingBalance2);

		}
		/*
		 * 
		 * SendPayment
		 * 
		 */
		if (txns.contains("SendPayment")) {
			String txn_name = "SendPayment";
			pu.addTrnasaction(txn_name, "sp_sendAcct:int", "sp_destAcct:int", "sp_amount:int");
			pu.addAssertion(txn_name, new E_UnOp(UnOp.NOT, new E_BinOp(BinOp.EQ, pu.getArg("sp_sendAcct"), pu.getArg("sp_destAcct"))));
			// retrieve sender accounts' data
			WHC SendPayment_GetAccount_send_WHC = new WHC(pu.getIsAliveFieldName("accounts"),
					new WHC_Constraint(pu.getTableName("accounts"), pu.getFieldName("a_custid"), BinOp.EQ,
							pu.getArg("sp_sendAcct")),
					new WHC_Constraint(pu.getTableName("accounts"), pu.getFieldName("is_checking"), BinOp.EQ,
							new E_Const_Bool(true)));
			Select_Query SendPayment_GetAccount_send = pu.addSelectQuery(txn_name, "accounts", true,
					SendPayment_GetAccount_send_WHC, "a_name", "a_bal");
			pu.addQueryStatement(txn_name, SendPayment_GetAccount_send);

			// if the sender's checking balance is greater than amount
			Expression SendPayment_IF1_C = new E_BinOp(BinOp.GT, pu.getProjExpr("SendPayment", 0, "a_bal", 1),
					pu.getArg("sp_amount"));
			pu.addIfStatement("SendPayment", SendPayment_IF1_C);

			// update checking balance of sender
			WHC_Constraint ZeroCheckingBalance_WHC_1 = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("sp_sendAcct"));
			WHC_Constraint ZeroCheckingBalance_WHC_2 = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_uuid"), BinOp.EQ, new E_UUID());
			WHC_Constraint ZeroCheckingBalance_WHC_3 = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("is_checking"), BinOp.EQ, new E_Const_Bool(true));
			Insert_Query SendPayment_U1 = pu.addInsertQuery(txn_name, "accounts", true, ZeroCheckingBalance_WHC_1,
					ZeroCheckingBalance_WHC_2, ZeroCheckingBalance_WHC_3);
			SendPayment_U1.addInsertExp(pu.getFieldName("a_bal"), pu.getArg("sp_amount"));
			pu.addQueryStatementInIf(txn_name, 0, SendPayment_U1);

			// update checking balance of dest
			WHC_Constraint ZeroCheckingBalance_WHC_1_dest = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("sp_destAcct"));
			WHC_Constraint ZeroCheckingBalance_WHC_2_dest = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_uuid"), BinOp.EQ, new E_UUID());
			WHC_Constraint ZeroCheckingBalance_WHC_3_dest = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("is_checking"), BinOp.EQ, new E_Const_Bool(true));
			Insert_Query SendPayment_U1_dest = pu.addInsertQuery(txn_name, "accounts", true,
					ZeroCheckingBalance_WHC_1_dest, ZeroCheckingBalance_WHC_2_dest, ZeroCheckingBalance_WHC_3_dest);
			SendPayment_U1_dest.addInsertExp(pu.getFieldName("a_bal"), pu.getArg("sp_amount"));
			pu.addQueryStatementInIf(txn_name, 0, SendPayment_U1_dest);

		}
		/*
		 * 
		 * TransactSavings
		 * 
		 */
		if (txns.contains("TransactSavings")) {
			String txn_name = "TransactSavings";
			pu.addTrnasaction(txn_name, "ts_custName:string", "ts_amount:int");
			// retrieve customer's id based on his/her name
			WHC TransactSavings_GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"),
					new WHC_Constraint(pu.getTableName("accounts"), pu.getFieldName("a_name"), BinOp.EQ,
							pu.getArg("ts_custName")),
					new WHC_Constraint(pu.getTableName("accounts"), pu.getFieldName("is_checking"), BinOp.EQ,
							new E_Const_Bool(false)));
			Select_Query TransactSavings_GetAccount0 = pu.addSelectQuery("TransactSavings", "accounts", false,
					TransactSavings_GetAccount0_WHC, "a_custid", "a_bal");
			pu.addQueryStatement("TransactSavings", TransactSavings_GetAccount0);

			// if the balance is larger than amount
			Expression TransactSavings_IF1_C = new E_BinOp(BinOp.GT, pu.getProjExpr("TransactSavings", 0, "a_bal", 1),
					pu.getArg("ts_amount"));
			pu.addIfStatement("TransactSavings", TransactSavings_IF1_C);

			// write customer's new saving's balance
			WHC_Constraint ZeroCheckingBalance_WHC_1_dest = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_custid"), BinOp.EQ, pu.getProjExpr(txn_name, 0, "a_custid", 1));
			WHC_Constraint ZeroCheckingBalance_WHC_2_dest = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_uuid"), BinOp.EQ, new E_UUID());
			WHC_Constraint ZeroCheckingBalance_WHC_3_dest = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("is_checking"), BinOp.EQ, new E_Const_Bool(false));
			Insert_Query SendPayment_U1_dest = pu.addInsertQuery(txn_name, "accounts", true,
					ZeroCheckingBalance_WHC_1_dest, ZeroCheckingBalance_WHC_2_dest, ZeroCheckingBalance_WHC_3_dest);
			SendPayment_U1_dest.addInsertExp(pu.getFieldName("a_bal"), pu.getArg("ts_amount"));
			//SendPayment_U1_dest.addInsertExp(pu.getFieldName("a_name"), pu.getArg("ts_custName"));
			pu.addQueryStatementInIf("TransactSavings", 0, SendPayment_U1_dest);

		}
		/*
		 * 
		 * WriteCheck
		 * 
		 */
		if (txns.contains("WriteCheck")) {
			String txn_name = "WriteCheck";
			pu.addTrnasaction("WriteCheck", "wc_custName:string", "wc_amount:int");
			// retrive customer's id based on his/her name
			WHC WriteCheck_GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_name"), BinOp.EQ, pu.getArg("wc_custName")));
			Select_Query WriteCheck_GetAccount0 = pu.addSelectQuery("WriteCheck", "accounts", false,
					WriteCheck_GetAccount0_WHC, "a_custid", "a_bal");
			pu.addQueryStatement("WriteCheck", WriteCheck_GetAccount0);

			// if the total of balances is high enough
			Expression WriteCheck_IF1_C = new E_BinOp(BinOp.GT, pu.getProjExpr("WriteCheck", 0, "a_bal", 1),
					pu.getArg("wc_amount"));
			pu.addIfStatement("WriteCheck", WriteCheck_IF1_C);
			// update their checking
			WHC_Constraint ZeroCheckingBalance_WHC_1_dest = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_custid"), BinOp.EQ, pu.getProjExpr(txn_name, 0, "a_custid", 1));
			WHC_Constraint ZeroCheckingBalance_WHC_2_dest = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_uuid"), BinOp.EQ, new E_UUID());
			WHC_Constraint ZeroCheckingBalance_WHC_3_dest = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("is_checking"), BinOp.EQ, new E_Const_Bool(true));
			Insert_Query SendPayment_U1_dest = pu.addInsertQuery(txn_name, "accounts", true,
					ZeroCheckingBalance_WHC_1_dest, ZeroCheckingBalance_WHC_2_dest, ZeroCheckingBalance_WHC_3_dest);
			SendPayment_U1_dest.addInsertExp(pu.getFieldName("a_bal"), pu.getArg("wc_amount"));
			pu.addQueryStatementInIf("WriteCheck", 0, SendPayment_U1_dest);

			// else: update their checking
			WHC_Constraint ZeroCheckingBalance_WHC_1_dest_else = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_custid"), BinOp.EQ, pu.getProjExpr(txn_name, 0, "a_custid", 1));
			WHC_Constraint ZeroCheckingBalance_WHC_2_dest_else = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("a_uuid"), BinOp.EQ, new E_UUID());
			WHC_Constraint ZeroCheckingBalance_WHC_3_dest_else = new WHC_Constraint(pu.getTableName("accounts"),
					pu.getFieldName("is_checking"), BinOp.EQ, new E_Const_Bool(true));
			Insert_Query SendPayment_U1_dest_else = pu.addInsertQuery(txn_name, "accounts", true,
					ZeroCheckingBalance_WHC_1_dest_else, ZeroCheckingBalance_WHC_2_dest_else,
					ZeroCheckingBalance_WHC_3_dest_else);
			SendPayment_U1_dest_else.addInsertExp(pu.getFieldName("a_bal"),
					new E_BinOp(BinOp.PLUS, pu.getArg("wc_amount"), new E_Const_Num(1)));
			pu.addQueryStatementInElse("WriteCheck", 0, SendPayment_U1_dest_else);
		}
		return pu.getProgram();

	}

}
