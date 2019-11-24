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
			

		}
		/*
		 * 
		 * Balance
		 * 
		 */

		if (txns.contains("Balance")) {
			pu.addTrnasaction("Balance", "ba_custName:string");
			// get customer's id based on his/her name


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
