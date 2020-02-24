package kiarahmani.atropos.encoding_engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.microsoft.z3.Status;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.dependency.Conflict;
import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI;
import kiarahmani.atropos.dependency.DAI_Graph;
import kiarahmani.atropos.encoding_engine.Z3.Z3Driver;
import kiarahmani.atropos.encoding_engine.Z3.Z3Logger;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.refactoring_engine.Refactoring_Engine;
import kiarahmani.atropos.utils.Constants;
import kiarahmani.atropos.utils.Program_Utils;

public class Encoding_Engine {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private Z3Logger z3logger;
	PrintWriter printer;
	FileWriter writer;

	public Encoding_Engine(String program_name) {
		z3logger = new Z3Logger("smt2/z3-encoding.smt2");
		File file = new File("analytics/" + program_name + ".atps");
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

	public DAI_Graph constructInitialDAIGraph(Program_Utils pu) {
		Program program = pu.generateProgram();

		Conflict_Graph cg = new Conflict_Graph(program);
		Refactoring_Engine re = new Refactoring_Engine();
		DAI_Graph dai_graph = new DAI_Graph();
		ArrayList<DAI> potential_dais = new ArrayList<>();
		// first find all potential dais
		for (Transaction txn : program.getTransactions()) {
			logger.debug("finding potential DAIs in txn:" + txn.getName());
			ArrayList<Query> all_queries = txn.getAllQueries();
			for (int i = 0; i < all_queries.size(); i++)
				for (int j = i + 1; j < all_queries.size(); j++) {
					Query q1 = all_queries.get(i);
					Query q2 = all_queries.get(j);
					ArrayList<FieldName> accessed_by_q1 = (q1.isWrite()) ? q1.getWrittenFieldNames()
							: q1.getReadFieldNames();
					ArrayList<FieldName> accessed_by_q2 = (q2.isWrite()) ? q2.getWrittenFieldNames()
							: q2.getReadFieldNames();
					DAI dai = new DAI(txn, q1, accessed_by_q1, q2, accessed_by_q2);
					potential_dais.add(dai);
				}
		}

		System.out.println("Number of potential DAIs: " + potential_dais.size());
		logger.debug("entering the dais_loop to iterate over all potential dais");
		int iter = 0;
		int dais_loop_iter = 0;
		dais_loop: for (DAI pot_dai : potential_dais) {
			System.out.println("\nDAI #" + (dais_loop_iter++));
			// pre-analysis on the potential dai
			z3logger.reset();
			System.gc();
			Z3Driver local_z3_driver = new Z3Driver();
			logger.debug("new z3 driver created");
			for (Transaction txn : pu.getTrasnsactionMap().values()) {
				txn.is_included = true;
				txn.setAllQueriesIncluded(true);
			}

			Program_Utils snapshot = pu.mkSnapShot();
			re.delete_redundant(snapshot);
			program = snapshot.generateProgram();
			Status valid = local_z3_driver.validDAI(program, pot_dai);
			if (valid == Status.UNSATISFIABLE) {
				logger.debug(
						" discarding the potential DAI due to conflicting path conditions. continue to the next dai");
				continue dais_loop;
			} else
				logger.debug("potential DAI was pre-analyzed and found valid. Further analysis is needed");

			// could not rule out the potential dai: must perform full analysis
			for (Conflict c1 : cg.getConfsFromQuery(pot_dai.getQuery(1), pot_dai.getTransaction())) {
				for (Conflict c2 : cg.getConfsFromQuery(pot_dai.getQuery(2), pot_dai.getTransaction())) {
					logger.debug(" involved transactions: " + pot_dai.getTransaction().getName() + "-"
							+ c1.getTransaction(2).getName() + "-" + c2.getTransaction(2).getName());
					snapshot = pu.mkSnapShot();
					DAI original_dai = new DAI(pot_dai.getTransaction(), pot_dai.getQuery(1), pot_dai.getFieldNames(1),
							pot_dai.getQuery(2), pot_dai.getFieldNames(2));

					for (Transaction txn : snapshot.getTrasnsactionMap().values())
						if (txn.is_equal(pot_dai.getTransaction()) || txn.is_equal(c1.getTransaction(2))
								|| txn.is_equal(c2.getTransaction(2))) {
							txn.is_included = true;
							txn.setAllQueriesIncluded(false); // only some will be set to true below
						} else
							txn.is_included = false;

					// XXX for some reason queries are not referenced by c1 and c2 and must directly
					// be updated
					for (Transaction txn : snapshot.getTrasnsactionMap().values())
						for (Query q : txn.getAllQueries()) {
							if (txn.is_equal(pot_dai.getTransaction())) {
								if (q.equals_ids(pot_dai.getQuery(1)))
									q.setIsIncluded(true);
								if (q.equals_ids(pot_dai.getQuery(2)))
									q.setIsIncluded(true);
							}
							if (txn.is_equal(c1.getTransaction(1)))
								if (q.equals_ids(c1.getQuery(1)))
									q.setIsIncluded(true);

							if (txn.is_equal(c1.getTransaction(2)))
								if (q.equals_ids(c1.getQuery(2)))
									q.setIsIncluded(true);

							if (txn.is_equal(c2.getTransaction(1)))
								if (q.equals_ids(c2.getQuery(1)))
									q.setIsIncluded(true);

							if (txn.is_equal(c2.getTransaction(2)))
								if (q.equals_ids(c2.getQuery(2)))
									q.setIsIncluded(true);
						}

					// prune away unrelated components of the program
					re.delete_unincluded(snapshot);
					program = snapshot.generateProgram();
					program.printProgram();

					// update the po of the queries in dai (since it may have been changed
					// during the pruning)
					for (Transaction txn : snapshot.getTrasnsactionMap().values())
						for (Query q : txn.getAllQueries()) {
							if (txn.is_equal(pot_dai.getTransaction()))
								if (q.getId().equals(pot_dai.getQuery(1).getId()))
									pot_dai.setQuery1(q);
								else if (q.getId().equals(pot_dai.getQuery(2).getId()))
									pot_dai.setQuery2(q);
							if (txn.is_equal(c1.getTransaction(1)))
								if (q.equals_ids(c1.getQuery(1)))
									c1.setQuery1(q);
							if (txn.is_equal(c1.getTransaction(2)))
								if (q.equals_ids(c1.getQuery(2)))
									c1.setQuery2(q);
							if (txn.is_equal(c2.getTransaction(1)))
								if (q.equals_ids(c2.getQuery(1)))
									c2.setQuery1(q);
							if (txn.is_equal(c2.getTransaction(2)))
								if (q.equals_ids(c2.getQuery(2)))
									c2.setQuery2(q);
						}

					// begin encoding the pruned program
					z3logger.reset();
					local_z3_driver = new Z3Driver();
					logger.debug("new z3 driver is created");
					if (Constants._VERBOSE_ANALYSIS) {
						System.out.println(
								"\nRound #" + (iter++) + " (anomalies found: " + dai_graph.getDAIs().size() + ")");
						printBaseAnomaly(iter, pot_dai, c1, c2);
					} else {
						String init = (iter % 10 != 0) ? "" : "\n";
						System.out.print(init + "(rd" + (iter++) + ":");
					}
					long begin = System.currentTimeMillis();
					logger.debug("before running the SAT query");
					Status status = local_z3_driver.generateDAI(program, 4, pot_dai, c1, c2);
					logger.debug("after running the SAT query: " + status);
					long end = System.currentTimeMillis();
					printResults(status, end - begin);
					// if SAT, add the potential DAI to the graph
					if (status == Status.SATISFIABLE) {
						dai_graph.addDAI(original_dai);
						continue dais_loop;
					}
					// free up solver's memory for the next iteration
					local_z3_driver = null;
					System.gc();
				}
			}
			logger.debug("end of analysis for DAI: " + pot_dai);
		}
		System.out.println("\nAnomalies found: " + dai_graph.getDAIs().size() + "\n");
		return dai_graph;

	}

	private void printResults(Status status, long time) {
		String status_string = (status == Status.SATISFIABLE) ? "SAT" : "UNSAT";
		if (status == Status.UNKNOWN)
			status_string = "UNKNOWN";
		if (Constants._VERBOSE_ANALYSIS) {
			System.out.println("" + status_string + " (" + (time) + "ms)");
		} else
			System.out.print("" + status_string + ") ");
		this.printer.append(String.valueOf(time));
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
		System.out.printf("%-8s ========== %s\n", c1.getQuery(1).getId() + "(" + c1.getQuery(1).getPo() + ")",
				c1.getQuery(2).getId() + "(" + c1.getQuery(2).getPo() + ")");
		//
		System.out.println();
		//
		System.out.printf("%-20s%s\n", "", txn_last_name);
		System.out.printf("%-20s%s\n", "", txn_last_line);
		System.out.printf("%-8s ========== %s\n", dai.getQuery(2).getId() + "(" + dai.getQuery(2).getPo() + ")",
				c2.getQuery(2).getId() + "(" + c2.getQuery(2).getPo() + ")");
		System.out.println();
	}

}