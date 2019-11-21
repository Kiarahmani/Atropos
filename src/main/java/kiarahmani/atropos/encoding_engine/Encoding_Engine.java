package kiarahmani.atropos.encoding_engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.plaf.SliderUI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.z3.Status;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.dependency.Conflict;
import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI;
import kiarahmani.atropos.dependency.DAI_Graph;
import kiarahmani.atropos.encoding_engine.Z3.Z3Driver;
import kiarahmani.atropos.encoding_engine.Z3.Z3Logger;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Transaction;

public class Encoding_Engine {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private Program program;
	private Z3Logger z3logger;
	PrintWriter printer;
	FileWriter writer;

	public Encoding_Engine(Program program) {
		z3logger = new Z3Logger("smt2/z3-encoding.smt2");
		this.program = program;
		File file = new File("analytics/" + program.getName() + ".atps");
		this.printer = null;
		try {
			writer = new FileWriter(file, false);
			printer = new PrintWriter(writer);
			printer.append("##;##\n" + "@LiveGraph demo file.\nTime");
			printer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void constructInitialDAIGraph(String program_name, Conflict_Graph cg) {
		DAI_Graph dai_graph = new DAI_Graph();
		int iter = 0;
		System.out.println("\n\n## DAI");
		for (Transaction txn : program.getTransactions()) {
			logger.debug("finding potential DAIs in txn:" + txn.getName());
			ArrayList<Query> all_queries = txn.getAllQueries();
			for (int i = 0; i < all_queries.size(); i++) {
				for (int j = i + 1; j < all_queries.size(); j++) {
					Query q1 = all_queries.get(i);
					Query q2 = all_queries.get(j);
					DAI dai = new DAI(txn, q1, q1.getAccessedFieldNames(), q2, q2.getAccessedFieldNames());
					logger.debug("potential DAI: " + dai);
					outer_conflicts: for (Conflict c1 : cg.getConfsFromQuery(q1))
						inner_conflicts: for (Conflict c2 : cg.getConfsFromQuery(q2)) {
							// the potential DAI:
							Z3Driver local_z3_driver = new Z3Driver();
							// check if it is actualy a valid instance
							System.out.println("Round# " + iter++ + "");
							//printBaseAnomaly(iter, dai, c1, c2);
							long begin = System.currentTimeMillis();
							Status status = local_z3_driver.generateDAI(this.program, 4, dai, c1, c2);
							long end = System.currentTimeMillis();
							printResults(status, end - begin);
							// free up solver's memory for the next iteration
							local_z3_driver = null;
							z3logger.reset();
							// if SAT, add the potential DAI to the graph
							if (status == Status.SATISFIABLE) {
								dai_graph.addDAI(dai);
								break outer_conflicts;
							}

						}
				}
			}
		}
		dai_graph.printDAIGraph();
	}

	private void printResults(Status status, long time) {
		String status_string = (status == Status.SATISFIABLE) ? "SAT" : "UNSAT";
		System.out.println("" + status_string + " (" + (time) + "ms)\n\n");
		this.printer.append(String.valueOf(time) + "\n");
		this.printer.flush();
	}

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

}