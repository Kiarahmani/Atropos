package kiarahmani.atropos;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.logging.log4j.LogManager;
import org.junit.Test;

import com.microsoft.z3.Status;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.dependency.Conflict;
import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI;
import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.encoding_engine.Z3.Z3Driver;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.program_generators.TestInputProgramGenerator;
import kiarahmani.atropos.utils.Constants;



public class AppTest {
	
	public AppTest() {
		Constants._IS_TEST = true;
	}
	
	
	
	@Test
	public void select_update_tests() {
		try {
			new Constants();
		} catch (IOException e) {
			e.printStackTrace();
		}
		TestInputProgramGenerator ipg = new TestInputProgramGenerator();
		Status[] expected_status = new Status[] { Status.SATISFIABLE, Status.UNSATISFIABLE, Status.SATISFIABLE,
				Status.SATISFIABLE, Status.UNSATISFIABLE };
		int i = 1;
		for (Status stat : expected_status) {
			String prog_name = "select-update-test-" + i;
			Program program = ipg.generate(prog_name, "");
			Conflict_Graph cg = new Conflict_Graph(program);
			Transaction txn = program.getTransactions().get(0);
			program.printProgram();
			Query q1 = txn.getAllQueries().get(0);
			Query q2 = txn.getAllQueries().get(1);
			ArrayList<FieldName> accessed_by_q1 = (q1.isWrite()) ? q1.getWrittenFieldNames() : q1.getReadFieldNames();
			ArrayList<FieldName> accessed_by_q2 = (q2.isWrite()) ? q2.getWrittenFieldNames() : q2.getReadFieldNames();
			DAI dai = new DAI(txn, q1, accessed_by_q1, q2, accessed_by_q2);
			int iter = 0;
			for (Conflict c1 : cg.getConfsFromQuery(q1))
				for (Conflict c2 : cg.getConfsFromQuery(q2)) {
					Z3Driver local_z3_driver = new Z3Driver();
					System.out.println("Round# " + iter++ + "");
					printBaseAnomaly(iter, dai, c1, c2);
					long begin = System.currentTimeMillis();
					Status status = local_z3_driver.generateDAI(program, 4, dai, c1, c2);
					long end = System.currentTimeMillis();
					printResults(status, stat, end - begin);
					assertTrue(status == stat);
					local_z3_driver = null;
				}
			i++;
		}
	}

	@Test
	public void select_insert_tests() {
		try {
			new Constants();
		} catch (IOException e) {
			e.printStackTrace();
		}
		TestInputProgramGenerator ipg = new TestInputProgramGenerator();
		Status[] expected_status = new Status[] { Status.SATISFIABLE, Status.SATISFIABLE, Status.SATISFIABLE,
				Status.UNSATISFIABLE, Status.UNSATISFIABLE, Status.UNSATISFIABLE, Status.SATISFIABLE,
				Status.SATISFIABLE, Status.SATISFIABLE, Status.SATISFIABLE, Status.SATISFIABLE, Status.SATISFIABLE,
				Status.SATISFIABLE, Status.SATISFIABLE, Status.UNSATISFIABLE, Status.UNSATISFIABLE, Status.SATISFIABLE,
				Status.SATISFIABLE, Status.UNSATISFIABLE, Status.UNSATISFIABLE, Status.UNSATISFIABLE, Status.UNSATISFIABLE};
		int prog_iter = 1;
		for (int i = 0; i < expected_status.length;) {
			String prog_name = "select-insert-test-" + prog_iter++;
			Program program = ipg.generate(prog_name, "");
			Conflict_Graph cg = new Conflict_Graph(program);
			Transaction txn = program.getTransactions().get(0);
			program.printProgram();
			Query q1 = txn.getAllQueries().get(0);
			Query q2 = txn.getAllQueries().get(1);
			ArrayList<FieldName> accessed_by_q1 = (q1.isWrite()) ? q1.getWrittenFieldNames() : q1.getReadFieldNames();
			ArrayList<FieldName> accessed_by_q2 = (q2.isWrite()) ? q2.getWrittenFieldNames() : q2.getReadFieldNames();
			DAI dai = new DAI(txn, q1, accessed_by_q1, q2, accessed_by_q2);
			int iter = 0;
			for (Conflict c1 : cg.getConfsFromQuery(q1))
				for (Conflict c2 : cg.getConfsFromQuery(q2)) {
					Z3Driver local_z3_driver = new Z3Driver();
					System.out.println("Round# " + iter++ + "");
					printBaseAnomaly(iter, dai, c1, c2);
					long begin = System.currentTimeMillis();
					Status status = local_z3_driver.generateDAI(program, 4, dai, c1, c2);
					long end = System.currentTimeMillis();
					Status stat = expected_status[i++];
					printResults(status, stat, end - begin);
					assertTrue(status == stat);
					local_z3_driver = null;
				}
		}
	}
	
	
	
	@Test
	public void select_delete_tests() {
		try {
			new Constants();
		} catch (IOException e) {
			e.printStackTrace();
		}
		TestInputProgramGenerator ipg = new TestInputProgramGenerator();
		Status[] expected_status = new Status[] { Status.SATISFIABLE,Status.SATISFIABLE,Status.SATISFIABLE,Status.SATISFIABLE,Status.SATISFIABLE,Status.UNSATISFIABLE};
		int prog_iter = 1;
		for (int i = 0; i < expected_status.length;) {
			String prog_name = "select-delete-test-" + prog_iter++;
			Program program = ipg.generate(prog_name, "");
			Conflict_Graph cg = new Conflict_Graph(program);
			Transaction txn = program.getTransactions().get(0);
			program.printProgram();
			Query q1 = txn.getAllQueries().get(0);
			Query q2 = txn.getAllQueries().get(1);
			ArrayList<FieldName> accessed_by_q1 = (q1.isWrite()) ? q1.getWrittenFieldNames() : q1.getReadFieldNames();
			ArrayList<FieldName> accessed_by_q2 = (q2.isWrite()) ? q2.getWrittenFieldNames() : q2.getReadFieldNames();
			DAI dai = new DAI(txn, q1, accessed_by_q1, q2, accessed_by_q2);
			int iter = 0;
			for (Conflict c1 : cg.getConfsFromQuery(q1))
				for (Conflict c2 : cg.getConfsFromQuery(q2)) {
					Z3Driver local_z3_driver = new Z3Driver();
					System.out.println("Round# " + iter++ + "");
					printBaseAnomaly(iter, dai, c1, c2);
					long begin = System.currentTimeMillis();
					Status status = local_z3_driver.generateDAI(program, 4, dai, c1, c2);
					long end = System.currentTimeMillis();
					Status stat = expected_status[i++];
					printResults(status, stat, end - begin);
					assertTrue(status == stat);
					local_z3_driver = null;
				}
		}
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
	 * 
	 */

	private void printBaseAnomaly(int iter, DAI dai, Conflict c1, Conflict c2) {
		String txn1_name = dai.getTransaction().getName();
		String txn_first_name = c1.getTransaction(2).getName();
		String txn_last_name = c2.getTransaction(2).getName();
		String txn1_line = String.format("%0" + txn1_name.length() + "d", 0).replace("0", "-");
		String txn_first_line = String.format("%0" + txn_first_name.length() + "d", 0).replace("0", "-");
		String txn_last_line = String.format("%0" + txn_last_name.length() + "d", 0).replace("0", "-");
		System.out.println("\n***********************************");
		System.out.printf("%-20s%s\n", txn1_name, txn_first_name);
		System.out.printf("%-20s%s\n", txn1_line, txn_first_line);
		System.out.printf("%-8s ========== %s\n", c1.getQuery(1).getId(), c1.getQuery(2).getId());
		//
		System.out.println();
		//
		System.out.printf("%-20s%s\n", "", txn_last_name);
		System.out.printf("%-20s%s\n", "", txn_last_line);
		System.out.printf("%-8s ========== %s\n", dai.getQuery(2).getId(), c2.getQuery(2).getId());
		System.out.println();
	}

	private void printResults(Status status, Status expected, long time) {

		String status_string = (status == Status.SATISFIABLE) ? "SAT" : "UNSAT";
		String exp = (expected == Status.SATISFIABLE) ? "SAT" : "UNSAT";
		if (status == Status.UNKNOWN)
			status_string = "UNKNOWN";
		if (expected == Status.UNKNOWN)
			exp = "UNKNOWN";
		System.out.println("" + status_string + " (" + (time) + "ms)" + "    \n" + exp + " (expected)\n\n");
	}

}
