package kiarahmani.atropos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.DDL.vc.VC;
import kiarahmani.atropos.dependency.Conflict_Graph;
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
import kiarahmani.atropos.program_generators.SmallBank.SmallBankProgramGenerator_DSL;
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

		////////////// TEST
		Program_Utils pu1 = new Program_Utils("TPC-C");
		Program program1 = (new SmallBankProgramGenerator_DSL(pu1)).generate("Amalgamate", "Balance1",
				"DepositChecking1", "SendPayment1", "TransactSavings1", "WriteCheck1");

		program1.printProgram();
		assert (false);

		////////////////////
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

		Program_Utils pu = new Program_Utils("TPC-C");
//		Program program = (new SEATSProgramGenerator(pu)).generate("deleteReservation", "findFlights", "findOpenSeats",
//				"newReservation", "updateCustomer", "updateReservation");
//		HashMap<String, HashMap<String, HashSet<VC>>> history = new HashMap<>();
//		for (Table t : pu.getTables().values()) {
//			HashMap<String, HashSet<VC>> newMap = new HashMap<>();
//			for (Table tt : pu.getTables().values())
//				newMap.put(tt.getTableName().getName(), new HashSet<>());
//			history.put(t.getTableName().getName(), newMap);
//		}

		try {
			new Constants();
		} catch (IOException e) {
			e.printStackTrace();
		}
		long time_begin = System.currentTimeMillis();
		int iter = 0;
		out: while (iter < 1) {
			Refactoring_Engine re = new Refactoring_Engine();
			pu = new Program_Utils("SmallBank");

			Program program = (new SmallBankProgramGenerator_DSL(pu)).generate("Amalgamate", "Balance1",
					"DepositChecking1", "SendPayment1", "TransactSavings1", "WriteCheck1");

			program.printProgram();
			// re.atomicize(pu);
			// program.printProgram();
			// analyze(pu);
			assert (false);

			/*
			 * 
			 * 
			 */

			pu.lock();
			re.pre_analysis(pu);
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
					re.refactor_schema(pu, ref);
				} while (se.hasNext());
			}

//			for (VC vc : local_hist) {
//				if (history.get(vc.T_1) == null)
//					history.put(vc.T_1, new HashMap<>());
//				if (history.get(vc.T_1).get(vc.T_2) == null)
//					history.get(vc.T_1).put(vc.T_2, new HashSet<>());
//				history.get(vc.T_1).get(vc.T_2).add(vc);
//			}

			iter++;
			pu.generateProgram().printProgram();
			re.atomicize(pu);
			pu.generateProgram().printProgram();
			System.out.println("refactoring time: " + (System.currentTimeMillis() - time_begin));
			int anml_cnt = analyze(pu);
			System.gc();
			// print stats and exit
			printStats(System.currentTimeMillis() - time_begin, anml_cnt);
		}

	}

	private static int analyze(Program_Utils pu) {
		Encoding_Engine ee = new Encoding_Engine(pu.getProgramName());
		DAI_Graph dai_graph = ee.constructInitialDAIGraph(pu);
		dai_graph.printDAIGraph();
		return dai_graph.getDAICnt();
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
