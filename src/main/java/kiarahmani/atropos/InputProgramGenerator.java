package kiarahmani.atropos;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_BinUp;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.expression.constants.E_Const_Text;
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
	 * VERY SIMPLE BANKING APPLICATION GENERATOR
	 * 
	 * 
	 */
	public Program generateVerySimpleBankingProgram() {
		Program_Utils pu = new Program_Utils("very simple banking");
		pu.addTrnasaction("inc", "inc_id:int", "inc_amnt:int");
		// pu.addTrnasaction("dec", "dec_id:int", "dec_amnt:int");
		pu.addTable("accounts", new FieldName("acc_id", true, true, F_Type.NUM),
				new FieldName("acc_name", false, false, F_Type.TEXT),
				new FieldName("acc_balance", false, false, F_Type.NUM));

		// pu.addBasicTable("departments", "dept_id", "dept_address", "dept_budget");
		WHC INC_S1_WHC = new WHC(new WHC_Constraint(pu.getTableName("accounts"), pu.getFieldName("acc_id"), BinOp.EQ,
				pu.getArg("inc_id")));
		Select_Query INC_S1 = pu.addSelectQuery(0, "inc", "accounts", true, INC_S1_WHC, "acc_balance");
		pu.addQueryStatement("inc", INC_S1);

		WHC INC_U1_WHC = new WHC(new WHC_Constraint(pu.getTableName("accounts"), pu.getFieldName("acc_id"), BinOp.EQ,
				pu.getArg("inc_id")));
		Update_Query INC_U1 = pu.addUpdateQuery(1, "inc", "accounts", true, INC_U1_WHC);
		INC_U1.addUpdateExp(pu.getFieldName("acc_balance"),
				new E_BinUp(BinOp.PLUS, pu.getProjExpr("inc", 0, "acc_balance", 1), pu.getArg("inc_amnt")));
		pu.addQueryStatement("inc", INC_U1);

		// dec transaction
		WHC DEC_S1_WHC = new WHC(new WHC_Constraint(pu.getTableName("accounts"), pu.getFieldName("acc_id"), BinOp.EQ,
				pu.getArg("dec_id")));
		Select_Query DEC_S1 = pu.addSelectQuery(0, "dec", "accounts", true, DEC_S1_WHC, "acc_balance");
		pu.addQueryStatement("dec", DEC_S1);

		Expression DEC_IF1_C = new E_BinUp(BinOp.GT, pu.getProjExpr("dec", 0, "acc_balance", 3), pu.getArg("dec_amnt"));
		pu.addIfStatement("dec", DEC_IF1_C);

		WHC DEC_U1_WHC = new WHC(new WHC_Constraint(pu.getTableName("accounts"), pu.getFieldName("acc_id"), BinOp.EQ,
				pu.getArg("dec_id")));
		Update_Query DEC_U1 = pu.addUpdateQuery(1, "dec", "accounts", true, DEC_U1_WHC);
		DEC_U1.addUpdateExp(pu.getFieldName("acc_balance"),
				new E_BinUp(BinOp.MINUS, pu.getProjExpr("dec", 0, "acc_balance", 1), pu.getArg("dec_amnt")));
		pu.addQueryStatementInIf("dec", 0, DEC_U1);

		Update_Query DEC_U2 = pu.addUpdateQuery(2, "dec", "accounts", true, DEC_U1_WHC);
		DEC_U2.addUpdateExp(pu.getFieldName("acc_balance"),
				new E_BinUp(BinOp.MINUS, pu.getProjExpr("dec", 0, "acc_balance", 1), new E_Const_Num(10)));
		pu.addQueryStatementInElse("dec", 0, DEC_U2);

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
	public Program generateSmallBankProgram() {
		/*
		 * 
		 * Tables
		 * 
		 */
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
		
		/*
		pu.addTrnasaction("Amalgamate", "am_custId0:int", "am_custId1:int");
		// retrieve customer0's name by id
		WHC GetAccount0_WHC = new WHC(new WHC_Constraint(pu.getTableName("accounts"), pu.getFieldName("a_custid"),
				BinOp.EQ, pu.getArg("am_custId0")));
		Select_Query GetAccount0 = pu.addSelectQuery(0, "Amalgamate", "accounts", true, GetAccount0_WHC, "a_name");
		pu.addQueryStatement("Amalgamate", GetAccount0);
		// retrieve customer1's name by id
		WHC GetAccount1_WHC = new WHC(new WHC_Constraint(pu.getTableName("accounts"), pu.getFieldName("a_custid"),
				BinOp.EQ, pu.getArg("am_custId1")));
		Select_Query GetAccount1 = pu.addSelectQuery(1, "Amalgamate", "accounts", true, GetAccount1_WHC, "a_name");
		pu.addQueryStatement("Amalgamate", GetAccount1);

		// retrieve savings balance of cust0
		WHC GetSavings0_WHC = new WHC(new WHC_Constraint(pu.getTableName("savings"), pu.getFieldName("s_custid"),
				BinOp.EQ, pu.getArg("am_custId0")));
		Select_Query GetSavings0 = pu.addSelectQuery(2, "Amalgamate", "savings", true, GetSavings0_WHC, "s_bal");
		pu.addQueryStatement("Amalgamate", GetSavings0);

		// retrieve checking balance of cust1
		WHC GetChecking1_WHC = new WHC(new WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"),
				BinOp.EQ, pu.getArg("am_custId1")));
		Select_Query GetChecking1 = pu.addSelectQuery(3, "Amalgamate", "checking", true, GetChecking1_WHC, "c_bal");
		pu.addQueryStatement("Amalgamate", GetChecking1);

		// zero cust0's savings balance
		WHC ZeroCheckingBalance_WHC = new WHC(new WHC_Constraint(pu.getTableName("checking"),
				pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("am_custId0")));
		Update_Query ZeroCheckingBalance = pu.addUpdateQuery(4, "Amalgamate", "checking", true,
				ZeroCheckingBalance_WHC);
		ZeroCheckingBalance.addUpdateExp(pu.getFieldName("c_bal"), new E_Const_Num(0));
		pu.addQueryStatement("Amalgamate", ZeroCheckingBalance);

		// incremenet cust1's savings balance
		WHC UpdateSavingsBalance_WHC = new WHC(new WHC_Constraint(pu.getTableName("savings"),
				pu.getFieldName("s_custid"), BinOp.EQ, pu.getArg("am_custId1")));
		Update_Query UpdateSavingsBalance = pu.addUpdateQuery(5, "Amalgamate", "savings", true,
				UpdateSavingsBalance_WHC);
		UpdateSavingsBalance.addUpdateExp(pu.getFieldName("s_bal"), new E_BinUp(BinOp.PLUS,
				pu.getProjExpr("Amalgamate", 2, "s_bal", 1), pu.getProjExpr("Amalgamate", 3, "c_bal", 1)));
		pu.addQueryStatement("Amalgamate", UpdateSavingsBalance);
*/
		/*
		 * 
		 * Balance
		 * 
		 */
		
	/*	
		pu.addTrnasaction("Balance", "ba_custName:string");
		// get customer's id based on his/her name
		WHC Balance_GetAccount0_WHC = new WHC(new WHC_Constraint(pu.getTableName("accounts"),
				pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("ba_custName")));
		Select_Query Balance_GetAccount0 = pu.addSelectQuery(0, "Balance", "accounts", false, Balance_GetAccount0_WHC,
				"a_custid");
		pu.addQueryStatement("Balance", Balance_GetAccount0);

		// retrieve customer's savings balance based on the retrieved id
		WHC Balance_GetSavings_WHC = new WHC(new WHC_Constraint(pu.getTableName("savings"), pu.getFieldName("s_custid"),
				BinOp.EQ, pu.getProjExpr("Balance", 0, "a_custid", 1)));
		Select_Query Balance_GetSavings = pu.addSelectQuery(1, "Balance", "savings", true, Balance_GetSavings_WHC,
				"s_bal");
		pu.addQueryStatement("Balance", Balance_GetSavings);

		// retrieve customer's checking balance based on the retrieved id
		WHC Balance_GetChecking_WHC = new WHC(new WHC_Constraint(pu.getTableName("checking"),
				pu.getFieldName("c_custid"), BinOp.EQ, pu.getProjExpr("Balance", 0, "a_custid", 1)));
		Select_Query Balance_GetChecking = pu.addSelectQuery(2, "Balance", "checking", true, Balance_GetChecking_WHC,
				"c_bal");
		pu.addQueryStatement("Balance", Balance_GetChecking);
*/
		/*
		 * 
		 * DepositChecking
		 * 
		 */
	/*	
		// retirve customer's id based on his/her name
		pu.addTrnasaction("DepositChecking", "dc_custName:string", "dc_amount:int");
		WHC DepositChecking_GetAccount0_WHC = new WHC(new WHC_Constraint(pu.getTableName("accounts"),
				pu.getFieldName("a_name"), BinOp.EQ, pu.getArg("dc_custName")));
		Select_Query DepositChecking_GetAccount0 = pu.addSelectQuery(0, "DepositChecking", "accounts", false,
				DepositChecking_GetAccount0_WHC, "a_custid");
		pu.addQueryStatement("DepositChecking", DepositChecking_GetAccount0);

		// retrive customer's old checking balance
		WHC DepositChecking_GetChecking_WHC = new WHC(new WHC_Constraint(pu.getTableName("checking"),
				pu.getFieldName("c_custid"), BinOp.EQ, pu.getProjExpr("DepositChecking", 0, "a_custid", 1)));
		Select_Query DepositChecking_GetChecking = pu.addSelectQuery(1, "DepositChecking", "checking", true,
				DepositChecking_GetChecking_WHC, "c_bal");
		pu.addQueryStatement("DepositChecking", DepositChecking_GetChecking);

		// write customer's new checking balance
		WHC DepositChecking_WHC = new WHC(new WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"),
				BinOp.EQ, pu.getProjExpr("DepositChecking", 0, "a_custid", 1)));
		Update_Query DepositChecking = pu.addUpdateQuery(2, "DepositChecking", "checking", true, DepositChecking_WHC);
		DepositChecking.addUpdateExp(pu.getFieldName("c_bal"),
				new E_BinUp(BinOp.PLUS, pu.getProjExpr("DepositChecking", 1, "c_bal", 1), pu.getArg("dc_amount")));
		pu.addQueryStatement("DepositChecking", DepositChecking);
	
		*/
		/*
		 * 
		 * SendPayment
		 * 
		 */
		// retrieve both accounts' names
	/*
		pu.addTrnasaction("SendPayment", "sp_sendAcct:int", "sp_destAcct:int", "sp_amount:int");
		WHC SendPayment_GetAccount_send_WHC = new WHC(new WHC_Constraint(pu.getTableName("accounts"),
				pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("sp_sendAcct")));
		Select_Query SendPayment_GetAccount_send = pu.addSelectQuery(0, "SendPayment", "accounts", true,
				SendPayment_GetAccount_send_WHC, "a_name");
		pu.addQueryStatement("SendPayment", SendPayment_GetAccount_send);

		WHC SendPayment_GetAccount_dest_WHC = new WHC(new WHC_Constraint(pu.getTableName("accounts"),
				pu.getFieldName("a_custid"), BinOp.EQ, pu.getArg("sp_destAcct")));
		Select_Query SendPayment_GetAccount_dest = pu.addSelectQuery(1, "SendPayment", "accounts", true,
				SendPayment_GetAccount_dest_WHC, "a_name");
		pu.addQueryStatement("SendPayment", SendPayment_GetAccount_dest);

		// retrieve sender's old checking balance
		WHC SendPayment_GetChecking_WHC = new WHC(new WHC_Constraint(pu.getTableName("checking"),
				pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("sp_sendAcct")));
		Select_Query SendPayment_GetChecking = pu.addSelectQuery(2, "SendPayment", "checking", true,
				SendPayment_GetChecking_WHC, "c_bal");
		pu.addQueryStatement("SendPayment", SendPayment_GetChecking);

		// if the balance is greater than amount
		Expression SendPayment_IF1_C = new E_BinUp(BinOp.GT, pu.getProjExpr("SendPayment", 2, "c_bal", 1),
				pu.getArg("sp_amount"));
		pu.addIfStatement("SendPayment", SendPayment_IF1_C);

		// update sender's checking
		WHC SendPayment_U1_WHC = new WHC(new WHC_Constraint(pu.getTableName("checking"), pu.getFieldName("c_custid"),
				BinOp.EQ, pu.getArg("sp_sendAcct")));
		Update_Query SendPayment_U1 = pu.addUpdateQuery(3, "SendPayment", "checking", true, SendPayment_U1_WHC);
		SendPayment_U1.addUpdateExp(pu.getFieldName("c_bal"),
				new E_BinUp(BinOp.MINUS, pu.getProjExpr("SendPayment", 2, "c_bal", 1), pu.getArg("sp_amount")));
		pu.addQueryStatementInIf("SendPayment", 0, SendPayment_U1);

		// retrieve dest's old checking balance
		WHC SendPayment_GetChecking_dest_WHC = new WHC(new WHC_Constraint(pu.getTableName("checking"),
				pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("sp_destAcct")));
		Select_Query SendPayment_GetChecking_dest = pu.addSelectQuery(4, "SendPayment", "checking", true,
				SendPayment_GetChecking_dest_WHC, "c_bal");
		pu.addQueryStatementInIf("SendPayment", 0, SendPayment_GetChecking_dest);

		// write dest's new checking balance
		WHC SendPayment_U1_dest_WHC = new WHC(new WHC_Constraint(pu.getTableName("checking"),
				pu.getFieldName("c_custid"), BinOp.EQ, pu.getArg("sp_destAcct")));
		Update_Query SendPayment_U1_dest = pu.addUpdateQuery(5, "SendPayment", "accounts", true,
				SendPayment_U1_dest_WHC);
		SendPayment_U1_dest.addUpdateExp(pu.getFieldName("c_bal"),
				new E_BinUp(BinOp.PLUS, pu.getProjExpr("SendPayment", 3, "c_bal", 1), pu.getArg("sp_amount")));
		pu.addQueryStatementInIf("SendPayment", 0, SendPayment_U1_dest);
*/
		/*
		 * 
		 * TransactSavings
		 * 
		 */
		
		pu.addTrnasaction("TransactSavings", "ts_custName:string", "ts_amount:int");
		// retrieve customer's id based on his/her name
		WHC TransactSavings_GetAccount0_WHC = new WHC(new WHC_Constraint(pu.getTableName("accounts"),
				pu.getFieldName("a_name"), BinOp.EQ, new E_Const_Text("kir")));
		Select_Query TransactSavings_GetAccount0 = pu.addSelectQuery(0, "TransactSavings", "accounts", false,
				TransactSavings_GetAccount0_WHC, "a_custid");
		pu.addQueryStatement("TransactSavings", TransactSavings_GetAccount0);

		// retrieve customer's old savings balance
		WHC TransactSavings_GetSavings_WHC = new WHC(new WHC_Constraint(pu.getTableName("savings"),
				pu.getFieldName("s_custid"), BinOp.EQ, new E_Const_Num(1)));
		Select_Query TransactSavings_GetSavings = pu.addSelectQuery(1, "TransactSavings", "savings", true,
				TransactSavings_GetSavings_WHC, "s_bal");
		pu.addQueryStatement("TransactSavings", TransactSavings_GetSavings);

		// if the balance is larger than amount
		Expression TransactSavings_IF1_C = new E_BinUp(BinOp.GT, pu.getProjExpr("TransactSavings", 1, "s_bal", 1),
				pu.getArg("ts_amount"));
		//pu.addIfStatement("TransactSavings", TransactSavings_IF1_C);

		// write customer's new saving's balance
		WHC TransactSavings_U1_WHC = new WHC(new WHC_Constraint(pu.getTableName("savings"), pu.getFieldName("s_custid"),
				BinOp.EQ,new E_Const_Num(1)));
		Update_Query TransactSavings_U1 = pu.addUpdateQuery(2, "TransactSavings", "savings", true, TransactSavings_U1_WHC);
		TransactSavings_U1.addUpdateExp(pu.getFieldName("s_bal"),
				new E_Const_Num(1));
		pu.addQueryStatement("TransactSavings", TransactSavings_U1);

		/*
		 * 
		 * WriteCheck
		 * 
		 */
		/*
		pu.addTrnasaction("WriteCheck", "wc_custName:string", "wc_amount:int");
		// retrive customer's id based on his/her name
		WHC WriteCheck_GetAccount0_WHC = new WHC(new WHC_Constraint(pu.getTableName("accounts"),
				pu.getFieldName("a_name"), BinOp.EQ, pu.getArg("wc_custName")));
		Select_Query WriteCheck_GetAccount0 = pu.addSelectQuery(0, "WriteCheck", "accounts", false,
				WriteCheck_GetAccount0_WHC, "a_custid");
		pu.addQueryStatement("WriteCheck", WriteCheck_GetAccount0);

		// get their checkinbg balance
		WHC WriteCheck_GetChecking_WHC = new WHC(new WHC_Constraint(pu.getTableName("checking"),
				pu.getFieldName("c_custid"), BinOp.EQ, pu.getProjExpr("WriteCheck", 0, "a_custid", 1)));
		Select_Query WriteCheck_GetChecking = pu.addSelectQuery(1, "WriteCheck", "checking", true,
				WriteCheck_GetChecking_WHC, "c_bal");
		pu.addQueryStatement("WriteCheck", WriteCheck_GetChecking);
		// get their savings balance
		WHC WriteCheck_GetSavings_WHC = new WHC(new WHC_Constraint(pu.getTableName("savings"),
				pu.getFieldName("s_custid"), BinOp.EQ, pu.getProjExpr("WriteCheck", 0, "a_custid", 1)));
		Select_Query WriteCheck_GetSavings = pu.addSelectQuery(2, "WriteCheck", "savings", true,
				WriteCheck_GetSavings_WHC, "s_bal");
		pu.addQueryStatement("WriteCheck", WriteCheck_GetSavings);

		// if the total of balances is high enough
		E_BinUp total = new E_BinUp(BinOp.PLUS, pu.getProjExpr("WriteCheck", 1, "c_bal", 1),
				pu.getProjExpr("WriteCheck", 2, "s_bal", 1));
		Expression WriteCheck_IF1_C = new E_BinUp(BinOp.GT, total, pu.getArg("wc_amount"));
		pu.addIfStatement("WriteCheck", WriteCheck_IF1_C);
		// update their checking
		WHC WriteCheck_U1_dest_WHC = new WHC(new WHC_Constraint(pu.getTableName("checking"),
				pu.getFieldName("c_custid"), BinOp.EQ, pu.getProjExpr("WriteCheck", 0, "a_custid", 1)));
		Update_Query WriteCheck_U1_dest = pu.addUpdateQuery(3, "WriteCheck", "accounts", true, WriteCheck_U1_dest_WHC);
		WriteCheck_U1_dest.addUpdateExp(pu.getFieldName("c_bal"),
				new E_BinUp(BinOp.MINUS, pu.getProjExpr("WriteCheck", 1, "c_bal", 1), pu.getArg("wc_amount")));
		pu.addQueryStatementInIf("WriteCheck", 0, WriteCheck_U1_dest);

		// else: update their checking
		WHC WriteCheck_U1_dest_WHC_else = new WHC(new WHC_Constraint(pu.getTableName("checking"),
				pu.getFieldName("c_custid"), BinOp.EQ, pu.getProjExpr("WriteCheck", 0, "a_custid", 1)));
		Update_Query WriteCheck_U1_dest_else = pu.addUpdateQuery(3, "WriteCheck", "accounts", true,
				WriteCheck_U1_dest_WHC_else);
		E_BinUp penalty = new E_BinUp(BinOp.PLUS, pu.getArg("wc_amount"), new E_Const_Num(1));
		WriteCheck_U1_dest_else.addUpdateExp(pu.getFieldName("c_bal"),
				new E_BinUp(BinOp.MINUS, pu.getProjExpr("WriteCheck", 1, "c_bal", 1), penalty));
		pu.addQueryStatementInElse("WriteCheck", 0, WriteCheck_U1_dest_else);
*/
		return pu.getProgram();

	}
	
}
