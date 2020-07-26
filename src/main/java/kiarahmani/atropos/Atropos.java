package kiarahmani.atropos;

import java.beans.Statement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.DB.statementBuilder;
import kiarahmani.atropos.DDL.vc.VC;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI;
import kiarahmani.atropos.dependency.DAI_Graph;
import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program_generators.SEATSProgramGenerator;
import kiarahmani.atropos.program_generators.SIBenchProgramGenerator;
import kiarahmani.atropos.program_generators.TPCCProgramGenerator;
import kiarahmani.atropos.program_generators.TWITTERProgramGenerator;
import kiarahmani.atropos.program_generators.WikipediaProgramGenerator;
import kiarahmani.atropos.program_generators.SmallBank.SmallBankProgramGenerator;
import kiarahmani.atropos.refactoring.Refactor;
import kiarahmani.atropos.program_generators.SmallBank.OnlineCourse;
import kiarahmani.atropos.refactoring_engine.Refactoring_Engine;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_VC;
import kiarahmani.atropos.search_engine.Naive_search_engine;
import kiarahmani.atropos.search_engine.Optimal_search_engine;
import kiarahmani.atropos.search_engine.Optimal_search_engine_seats;
import kiarahmani.atropos.search_engine.Optimal_search_engine_sibench;
import kiarahmani.atropos.search_engine.Optimal_search_engine_tpcc;
import kiarahmani.atropos.search_engine.Optimal_search_engine_twitter;
import kiarahmani.atropos.search_engine.Optimal_search_engine_wikipedia;
import kiarahmani.atropos.utils.Constants;
import kiarahmani.atropos.utils.Program_Utils;

public class Atropos {

	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public static void main(String[] args) {
		long time_begin = System.currentTimeMillis();
		try {
			new Constants();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// initialize
		Program_Utils pu = new Program_Utils("Course");
		Refactor re = new Refactor();
		// get the first instance of the program
		new OnlineCourse(pu).generate();
		pu.generateProgram().printProgram();
		// find anomlous access pairs
		ArrayList<DAI> anmls = analyze(pu).getDAIs();
		// prform pre-analysis step
		re.pre_process(pu, anmls);
		System.out.println("\n\nbefore filtering:");
		for (DAI anml : anmls)
			System.out.println(anml);
		anmls = filter_duplicate_anmls(anmls);
		System.out.println("\n\nafter filtering:");
		for (DAI anml : anmls)
			System.out.println(anml);
		pu.generateProgram().printProgram();

		assert (false);

		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		int iter = 0;

		out: while (iter < 1) {
			// Refactoring_Engine re = new Refactoring_Engine();
			pu = new Program_Utils("SmallBank");

			// program = (new OnlineCourse(pu)).generate("Amalgamate", "Balance1",
			// "DepositChecking1", "SendPayment1",
			// "TransactSavings1", "WriteCheck1");

			// program.printProgram();

			// re.atomicize(pu);
			// program.printProgram();
			// analyze(pu);
			assert (false);

			/*
			 * 
			 * 
			 */

			pu.lock();
			// re.pre_analysis(pu);
			// search the refactoring space
			Optimal_search_engine_wikipedia se = new Optimal_search_engine_wikipedia();
			// Naive_search_engine se = new Naive_search_engine(history);
			int _refactoring_depth = 1;
			HashSet<VC> local_hist = new HashSet<>();

			for (int j = 0; j < _refactoring_depth; j++) {
				if (!se.reset(pu)) {
					logger.debug("reset failed: continue the main loop");
					iter++;
					continue out;
				}
				do {
					Delta ref = se.nextRefactoring(pu);
					if (ref == null) {
						iter++;
						continue out;
					}
					if (ref instanceof INTRO_VC) {
						INTRO_VC new_name = (INTRO_VC) ref;
						local_hist.add(new_name.getVC());
					}
					// re.refactor_schema(pu, ref);
				} while (se.hasNext());
			}
			iter++;
			pu.generateProgram().printProgram();
			// re.atomicize(pu);
			pu.generateProgram().printProgram();
			System.out.println("refactoring time: " + (System.currentTimeMillis() - time_begin));
			int anml_cnt = analyze(pu).getDAICnt();
			System.gc();
			// print stats and exit
			printStats(System.currentTimeMillis() - time_begin, anml_cnt);
		}

	}

	/**
	 * @param anmls
	 * @returns a filtered set of anomalies which ensures that each query is at most
	 *          involved in a single DAI
	 */
	private static ArrayList<DAI> filter_duplicate_anmls(ArrayList<DAI> anmls) {
		HashSet<String> stmts = new HashSet<>();
		ArrayList<DAI> result = new ArrayList<>();
		for (DAI anml : anmls) {
			String txnName = anml.getTransaction().getName();
			if (stmts.contains(txnName + anml.getQuery(1).getId())
					|| stmts.contains(txnName + anml.getQuery(2).getId()))
				continue;
			stmts.add(txnName + anml.getQuery(1).getId());
			stmts.add(txnName + anml.getQuery(2).getId());
			result.add(anml);
		}
		return result;
	}

	private static HashMap<String, HashMap<String, HashSet<VC>>> initHist(Program_Utils pu) {
		HashMap<String, HashMap<String, HashSet<VC>>> history = new HashMap<>();
		for (Table t : pu.getTables().values()) {
			HashMap<String, HashSet<VC>> newMap = new HashMap<>();
			for (Table tt : pu.getTables().values())
				newMap.put(tt.getTableName().getName(), new HashSet<>());
			history.put(t.getTableName().getName(), newMap);
		}
		return history;
	}

	private static DAI_Graph analyze(Program_Utils pu) {
		Encoding_Engine ee = new Encoding_Engine(pu.getProgramName());
		DAI_Graph dai_graph = ee.constructInitialDAIGraph(pu);
		dai_graph.printDAIGraph();
		return dai_graph;
	}

	private static void printStats(long time, int number_of_anomalies) {

		System.out.println(
				"\n\n\n\n============================================================================================");
		System.out.println();
		System.out.println("Total Memory: "
				+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000 + " MB");
		System.out.println("Total Time:   " + time / 1000.0 + " s\n");
		try {
			Files.write(Paths.get("results.atropos"), ("\n" + number_of_anomalies + "," + (time / 1000.0)).getBytes(),
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			// exception handling left as an exercise for the reader
		}

	}
}
