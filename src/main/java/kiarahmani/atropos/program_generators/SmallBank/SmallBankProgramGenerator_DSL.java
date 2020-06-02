package kiarahmani.atropos.program_generators.SmallBank;

import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.ProgramGenerator;
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
		pu.Transaction("inc").arg("input_id:int").arg("input_amount:int").done();
		pu.addStmt("inc").select("bal").from("balance").as("x").where("id", "=", pu.arg("input_id")).done();
		pu.addIfStmt("inc", pu.gt(pu.at("bal", "x", 1), pu.arg("input_amount")));
		pu.addInIf("inc", 0).update("balance").set("bal", pu.minus(pu.at("bal", "x", 1), pu.arg("input_amount")))
				.where("id", "=", pu.arg("input_id")).done();
		pu.addInElse("inc", 0).update("balance").set("bal", pu.minus(pu.at("bal", "x", 1), pu.cons(69)))
				.where("id", "=", pu.arg("input_id")).done();

		pu.addStmt("inc").update("balance").set("bal", pu.cons(1))
				.where("id", "=", pu.minus(pu.at("bal", "x", 1), pu.cons(10))).done();

		//
		//
		//
		//
		//
		//
		return pu.generateProgram();
	}
}
