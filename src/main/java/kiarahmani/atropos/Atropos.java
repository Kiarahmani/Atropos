package kiarahmani.atropos;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI_Graph;
import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.SmallBank.SmallBankProgramGenerator;
import kiarahmani.atropos.refactoring_engine.Refactoring_Engine;
import kiarahmani.atropos.refactoring_engine.Modifiers.Query_Modifier;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.search_engine.Naive_search_engine;
import kiarahmani.atropos.utils.Constants;
import kiarahmani.atropos.utils.Program_Utils;

public class Atropos {

	private static final Logger logger = LogManager.getLogger(Atropos.class);
	static int min_anomalies_cnt = Integer.MAX_VALUE;

	public static void main(String[] args) {
		int _refactoring_depth = 10;

		long time_begin = System.currentTimeMillis();
		try {
			new Constants();
		} catch (IOException e) {
		}
		Refactoring_Engine re = new Refactoring_Engine();
		Set<Program> results = new HashSet<>();
		Program_Utils pu = new Program_Utils("SmallBank");
		Program program = (new SmallBankProgramGenerator(pu)).generate("Balance", "Amalgamate", "TransactSavings",
				"DepositChecking", "SendPayment", "WriteCheck");
		program.printProgram();
		// min_anomalies_cnt = analyze(program);
		min_anomalies_cnt = 24;
		pu.lock();
		// search the refactoring space
		Naive_search_engine nse = new Naive_search_engine(pu);
		for (int i = 0; i < _refactoring_depth; i++) {
			logger.error("======== ITER " + i + " (current min: " + min_anomalies_cnt + ")");
			nse.reset();
			Delta[] refactorings = nse.nextRefactorings();
			logger.error("candidate refactorings are proposed");
			re.refactor_schema_seq(pu, refactorings);
			logger.error("candidate refactorings are applied");
			re.atomicize(pu);
			logger.error("program is atomicized");
			program = pu.generateProgram();
			program.printProgram();
			if (!programIsAccepted(pu.generateProgram())) {
				logger.error("refactoring rejected: ready to revert");
				re.revert_refactor_schema_seq(pu, refactorings);
				logger.error("revert successful");
			}
		}

		program = pu.generateProgram();
		program.printProgram();
		// print stats and exit
		printStats(System.currentTimeMillis() - time_begin, results);
	}

	private static boolean programIsAccepted(Program candidate) {
		logger.error("before analyze");
		int anm_cnt = analyze(candidate);
		if (anm_cnt < min_anomalies_cnt) {
			min_anomalies_cnt = anm_cnt;
			return true;
		} else
			return false;
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
	 */

	private static int analyze(Program program) {
		Conflict_Graph cg = new Conflict_Graph(program);
		Encoding_Engine ee = new Encoding_Engine(program.getName());
		DAI_Graph dai_graph = ee.constructInitialDAIGraph(program, cg);
		return dai_graph.getDAICnt();
		// cg.printGraph();
		// dai_graph.printDAIGraph();
	}

	private static void printStats(long time, Set<Program> results) {
		System.out.println(
				"\n\n\n\n============================================================================================");
		System.out.println();
		System.out.println("Final Programs Count: " + results.size());
		System.out.println(results);
		System.out.println("Total Memory: "
				+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000 + " MB");
		System.out.println("Total Time:   " + time / 1000.0 + " s\n");
	}
}
