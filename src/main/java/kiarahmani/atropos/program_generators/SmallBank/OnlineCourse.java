package kiarahmani.atropos.program_generators.SmallBank;

import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.ProgramGenerator;
import kiarahmani.atropos.utils.Program_Utils;

public class OnlineCourse implements ProgramGenerator {

	private Program_Utils pu;

	public OnlineCourse(Program_Utils pu) {
		this.pu = pu;
	}

	public Program generate(String... args) {

		/*
		 * Tables
		 */
		pu.Table("student").pk("st_id", "int").field("st_name", "string").field("st_em_id", "int")
				.field("st_co_id", "int").field("st_reg", "int").done();
		pu.Table("course").pk("co_id", "int").field("co_avail", "int").field("co_st_cnt", "int").done();
		pu.Table("email").pk("em_id", "int").field("em_addr", "string").done();

		/*
		 * Transactions
		 */
		// GETST
		pu.Transaction("getSt").arg("input_id:int").done();
		pu.addStmt("getSt").select("*").from("student").as("x").where("st_id", "=", pu.arg("input_id")).done();
		pu.addStmt("getSt").select("em_addr").from("email").as("y").where("em_id", "=", pu.at("st_em_id", "x", 1))
				.done();
		pu.addStmt("getSt").select("co_avail").from("course").as("z").where("co_id", "=", pu.at("st_co_id", "x", 1))
				.done();

		// SETST
		pu.Transaction("setSt").arg("input_id:int").arg("input_name:string").arg("input_email:string").done();
		pu.addStmt("setSt").select("st_em_id").from("student").where("st_id", "=", pu.arg("input_id")).as("x1").done();
		pu.addStmt("setSt").update("student").set("st_name", pu.arg("input_name"))
				.where("st_id", "=", pu.arg("input_id")).done();
		pu.addStmt("setSt").update("email").set("em_addr", pu.arg("input_email"))
				.where("em_id", "=", pu.at("st_em_id", "x1", 1)).done();

		// REGST
		pu.Transaction("regSt").arg("input_id:int").arg("input_co_id:int").done();
		pu.addStmt("regSt").update("student").set("st_co_id", pu.arg("input_co_id")).set("st_reg", pu.cons(1))
				.where("st_id", "=", pu.arg("input_id")).done();
		pu.addStmt("regSt").select("co_st_cnt").from("course").as("x2").where("co_id", "=", pu.arg("input_co_id"))
				.done();
		pu.addStmt("regSt").update("course").set("co_st_cnt", pu.plus(pu.at("co_st_cnt", "x2", 1), pu.cons(1)))
				.set("co_avail", pu.cons(1)).where("co_id", "=", pu.arg("input_co_id")).done();

		return pu.generateProgram();
	}
}
