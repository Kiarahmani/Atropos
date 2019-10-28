package kiarahmani.atropos;

import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_BinUp;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
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
		pu.addTrnasaction("inc", "inc_id", "inc_amnt");
		pu.addTrnasaction("dec", "dec_id", "dec_amnt");
		pu.addBasicTable("accounts", "acc_id", "acc_balance");

		// inc transaction
		WHC INC_S1_WHC = new WHC(new WHC_Constraint(pu.getFieldName("acc_id"), BinOp.EQ, pu.getArg("inc_id")));
		Select_Query INC_S1 = pu.addSelectQuery("inc", "accounts", true, INC_S1_WHC, "acc_balance");
		pu.addQueryStatement("inc", INC_S1);

		WHC INC_U1_WHC = new WHC(new WHC_Constraint(pu.getFieldName("acc_id"), BinOp.EQ, pu.getArg("inc_id")));
		Update_Query INC_U1 = pu.addUpdateQuery("inc", "accounts", true, INC_U1_WHC);
		INC_U1.addUpdateExp(pu.getFieldName("acc_balance"),
				new E_BinUp(BinOp.PLUS, pu.getProjExpr("inc", 0, "acc_balance", 1), pu.getArg("inc_amnt")));
		pu.addQueryStatement("inc", INC_U1);

		// dec transaction
		WHC DEC_S1_WHC = new WHC(new WHC_Constraint(pu.getFieldName("acc_id"), BinOp.EQ, pu.getArg("dec_id")));
		Select_Query DEC_S1 = pu.addSelectQuery("dec", "accounts", true, DEC_S1_WHC, "acc_balance");
		pu.addQueryStatement("dec", DEC_S1);

		Expression DEC_IF1_C = new E_BinUp(BinOp.GT, pu.getProjExpr("dec", 0, "acc_balance", 1), pu.getArg("dec_amnt"));
		pu.addIfStatement("dec", DEC_IF1_C);

		WHC DEC_U1_WHC = new WHC(new WHC_Constraint(pu.getFieldName("acc_id"), BinOp.EQ, pu.getArg("dec_id")));
		Update_Query DEC_U1 = pu.addUpdateQuery("dec", "accounts", true, DEC_U1_WHC);
		DEC_U1.addUpdateExp(pu.getFieldName("acc_balance"),
				new E_BinUp(BinOp.MINUS, pu.getProjExpr("dec", 0, "acc_balance", 1), pu.getArg("dec_amnt")));
		pu.addQueryStatementInIf("dec", 0, DEC_U1);

		Update_Query DEC_U2 = pu.addUpdateQuery("dec", "accounts", true, DEC_U1_WHC);
		DEC_U2.addUpdateExp(pu.getFieldName("acc_balance"),
				new E_BinUp(BinOp.MINUS, pu.getProjExpr("dec", 0, "acc_balance", 1), new E_Const_Num(10)));
		pu.addQueryStatementInElse("dec", 0, DEC_U2);

		return pu.getProgram();
	}

}
