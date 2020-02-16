package kiarahmani.atropos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.DDL.vc.VC;
import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI_Graph;
import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.SmallBank.SmallBankProgramGenerator;
import kiarahmani.atropos.refactoring_engine.Refactoring_Engine;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_VC;
import kiarahmani.atropos.search_engine.Naive_search_engine;
import kiarahmani.atropos.search_engine.Optimal_search_engine;
import kiarahmani.atropos.utils.Constants;
import kiarahmani.atropos.utils.Program_Utils;

public class Atropos {

	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public static void main(String[] args) {
		long time_begin = System.currentTimeMillis();
		HashSet<VC> history = new HashSet<>();
		try {
			new Constants();
		} catch (IOException e) {
		}
		int iter = 0;
		out: while (true) {
			System.out.println("\n#" + (iter++) + "\n");
			Refactoring_Engine re = new Refactoring_Engine();
			Program_Utils pu = new Program_Utils("SmallBank");
			Program program = (new SmallBankProgramGenerator(pu)).generate("Balance", "Amalgamate", "TransactSavings",
					"DepositChecking", "SendPayment", "WriteCheck");
			pu.lock();
			// program.printProgram();
			re.pre_analysis(pu);
			// search the refactoring space
			Naive_search_engine se = new Naive_search_engine(history);
			int _refactoring_depth = 4;
			HashSet<VC> history_local = new HashSet<>();
			for (int j = 0; j < _refactoring_depth; j++) {
				if (!se.reset(pu))
					continue out;
				do {
					Delta ref = se.nextRefactoring(pu);
					if (ref == null)
						continue out;
					if (ref instanceof INTRO_VC) {
						INTRO_VC introvc = (INTRO_VC) ref;
						history_local.add(introvc.getVC());
					}
					re.refactor_schema(pu, ref);
				} while (se.hasNext());
			}
			history.addAll(history_local);
			re.atomicize(pu);
			// program = pu.generateProgram();
			// program.printProgram();
			// int anml_cnt = analyze(program);
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.gc();
			// print stats and exit
			// printStats(System.currentTimeMillis() - time_begin, anml_cnt);
		}
	}

	private static int analyze(Program program) {
		Conflict_Graph cg = new Conflict_Graph(program);
		Encoding_Engine ee = new Encoding_Engine(program.getName());
		DAI_Graph dai_graph = ee.constructInitialDAIGraph(program, cg);
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
