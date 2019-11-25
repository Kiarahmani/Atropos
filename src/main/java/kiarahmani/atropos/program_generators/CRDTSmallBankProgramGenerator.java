package kiarahmani.atropos.program_generators;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_BinUp;
import kiarahmani.atropos.DML.expression.E_UUID;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.query.Delete_Query;
import kiarahmani.atropos.DML.query.Insert_Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.utils.Program_Utils;

public class CRDTSmallBankProgramGenerator {

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
				new FieldName("a_name", false, false, F_Type.TEXT));
		pu.addTable("checking", new FieldName("c_custid", true, true, F_Type.NUM),
				new FieldName("c_uuid", true, false, F_Type.NUM), new FieldName("c_bal", false, false, F_Type.NUM));
		pu.addTable("savings", new FieldName("s_custid", true, true, F_Type.NUM),
				new FieldName("s_uuid", true, false, F_Type.NUM), new FieldName("s_bal", false, false, F_Type.NUM));
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
			// retrieve customer0's name by id
			WHC GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("am_custId0")));
			Select_Query GetAccount0 = pu.addSelectQuery(txn_name, "accounts", true, GetAccount0_WHC, "a_name");
			pu.addQueryStatement(txn_name, GetAccount0);
			// retrieve customer1's name by id
			WHC GetAccount1_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
					pu.getTableName("accounts"), pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("am_custId1")));
			Select_Query GetAccount1 = pu.addSelectQuery(txn_name, "accounts", true, GetAccount1_WHC, "a_name");
			pu.addQueryStatement(txn_name, GetAccount1);

			// retrieve savings balance of cust0
			WHC GetSavings0_WHC = new WHC(pu.getIsAliveFieldName("savings"), new WHC_Constraint(
					pu.getTableName("savings"), pu.getFieldName("s_custid"), BinOp.EQ, pu.getArg("am_custId0")));
			Select_Query GetSavings0 = pu.addSelectQuery(txn_name, "savings", true, GetSavings0_WHC, "s_bal");
			pu.addQueryStatement(txn_name, GetSavings0);

			// retrieve checking balance of cust0
			WHC GetChecking0_WHC = new WHC(pu.getIsAliveFieldName("checking"), new WHC_Constraint(
					pu.getTableName("checking"), pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("am_custId0")));
			Select_Query GetChecking0 = pu.addSelectQuery(txn_name, "checking", true, GetChecking0_WHC, "c_bal");
			pu.addQueryStatement(txn_name, GetChecking0);

			// zero saving of cust0
			WHC_Constraint ZeroCheckingBalance_WHC_1 = new WHC_Constraint(pu.getTableName("savings"),
					pu.getFieldName("s_custid"), BinOp.EQ, pu.getArg("am_custId0"));
			WHC_Constraint ZeroCheckingBalance_WHC_2 = new WHC_Constraint(pu.getTableName("savings"),
					pu.getFieldName("s_uuid"), BinOp.EQ, new E_UUID());
			Insert_Query ZeroCheckingBalance = pu.addInsertQuery(txn_name, "savings", true, ZeroCheckingBalance_WHC_1,
					ZeroCheckingBalance_WHC_2);
			ZeroCheckingBalance.addInsertExp(pu.getFieldName("s_bal"),
					new E_BinUp(BinOp.MINUS, new E_Const_Num(0), pu.getProjExpr(txn_name, 2, "s_bal", 1)));
			pu.addQueryStatement(txn_name, ZeroCheckingBalance);

			// zero checking of cust0
			WHC_Constraint ZeroCheckingBalance_WHC_11 = new WHC_Constraint(pu.getTableName("checking"),
					pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("am_custId0"));
			WHC_Constraint ZeroCheckingBalance_WHC_21 = new WHC_Constraint(pu.getTableName("checking"),
					pu.getFieldName("c_uuid"), BinOp.EQ, new E_UUID());
			Insert_Query ZeroCheckingBalance1 = pu.addInsertQuery(txn_name, "checking", true,
					ZeroCheckingBalance_WHC_11, ZeroCheckingBalance_WHC_21);
			ZeroCheckingBalance1.addInsertExp(pu.getFieldName("c_bal"),
					new E_BinUp(BinOp.MINUS, new E_Const_Num(0), pu.getProjExpr(txn_name, 3, "c_bal", 1)));
			pu.addQueryStatement(txn_name, ZeroCheckingBalance1);

			// update saving balance of cust1
			WHC_Constraint ZeroCheckingBalance_WHC_12 = new WHC_Constraint(pu.getTableName("savings"),
					pu.getFieldName("s_custid"), BinOp.EQ, pu.getArg("am_custId1"));
			WHC_Constraint ZeroCheckingBalance_WHC_22 = new WHC_Constraint(pu.getTableName("savings"),
					pu.getFieldName("s_uuid"), BinOp.EQ, new E_UUID());
			Insert_Query ZeroCheckingBalance2 = pu.addInsertQuery(txn_name, "savings", true,
					ZeroCheckingBalance_WHC_12, ZeroCheckingBalance_WHC_22);
			ZeroCheckingBalance2.addInsertExp(pu.getFieldName("s_bal"), new E_BinUp(BinOp.PLUS,
					pu.getProjExpr(txn_name, 2, "s_bal", 1), pu.getProjExpr(txn_name, 3, "c_bal", 1)));
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

		}
		/*
		 * 
		 * SendPayment
		 * 
		 */
		if (txns.contains("SendPayment")) {
			pu.addTrnasaction("SendPayment", "sp_sendAcct:int", "sp_destAcct:int", "sp_amount:int");
			// retrieve sender accounts' data

		}
		/*
		 * 
		 * TransactSavings
		 * 
		 */
		if (txns.contains("TransactSavings")) {
			pu.addTrnasaction("TransactSavings", "ts_custName:string", "ts_amount:int");

		}
		/*
		 * 
		 * WriteCheck
		 * 
		 */
		if (txns.contains("WriteCheck")) {
			pu.addTrnasaction("WriteCheck", "wc_custName:string", "wc_amount:int");

		}
		return pu.getProgram();

	}

}
