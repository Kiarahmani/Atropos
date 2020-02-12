package kiarahmani.atropos;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI_Graph;
import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.SmallBank.SmallBankProgramGenerator;
import kiarahmani.atropos.refactoring_engine.Refactoring_Engine;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.search_engine.Naive_search_engine;
import kiarahmani.atropos.utils.Constants;
import kiarahmani.atropos.utils.Program_Utils;

public class Atropos {

	private static final Logger logger = LogManager.getLogger(Atropos.class);
	static int min_anomalies_cnt = Integer.MAX_VALUE;

	public static void main(String[] args) {

		long time_begin = System.currentTimeMillis();
		try {
			new Constants();
		} catch (IOException e) {
		}
		Refactoring_Engine re = new Refactoring_Engine();
		Program_Utils pu = new Program_Utils("SmallBank");
		Program program = (new SmallBankProgramGenerator(pu)).generate("Balance", "Amalgamate", "TransactSavings",
				"DepositChecking", "SendPayment", "WriteCheck");
		program.printProgram();
		pu.lock();
		// search the refactoring space
		Naive_search_engine nse = new Naive_search_engine(pu);
		// define constants
		int _max_iterations = 1;
		int _refactoring_depth = 1;
		for (int i = 0; i < _max_iterations; i++) {
			ArrayList<Delta> all_refs = new ArrayList<>();
			for (int j = 0; j < _refactoring_depth; j++)
				for (Delta d : nse.nextRefactorings(pu))
					all_refs.add(d);
			re.refactor_schema_seq(pu, all_refs);
			re.atomicize(pu);
			program = pu.generateProgram();
			program.printProgram();
			analyze(program);
			System.gc();
		}
		// print stats and exit
		printStats(System.currentTimeMillis() - time_begin);
	}

	/*
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
	}

	private static void printStats(long time) {
		System.out.println(
				"\n\n\n\n============================================================================================");
		System.out.println();
		System.out.println("Total Memory: "
				+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000 + " MB");
		System.out.println("Total Time:   " + time / 1000.0 + " s\n");
	}
}
