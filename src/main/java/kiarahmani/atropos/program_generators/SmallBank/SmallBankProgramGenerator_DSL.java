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
import kiarahmani.atropos.DML.where_clause.WHCC;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.ProgramGenerator;
import kiarahmani.atropos.utils.Program_Utils;
import kiarahmani.atropos.utils.Program_Utils;

public class SmallBankProgramGenerator_DSL implements ProgramGenerator {

	private Program_Utils pu;

	public SmallBankProgramGenerator_DSL(Program_Utils pu2) {
		this.pu = pu2;
	}

	public Program generate(String... args) {

		/*
		 * 
		 * Tables
		 * 
		 */
		pu.Table("balance").pk("id", "int").field("bal", "int").done();

		/*
		 * 
		 * Transactions
		 * 
		 */
		pu.Transaction("inc").arg("input_id:int").done();
		pu.addStmt("inc").select("bal").from("balance").as("b1").where("id", "=", pu.arg("input_id")).done();
		pu.addStmt("inc").update("balance").set("bal", pu.con(1))
				.where("id", "=", pu.plus(pu.proj("bal", "b1", 1), pu.con(1))).done();

		//
		//
		//
		//
		//
		//
		return pu.generateProgram();
	}
}
