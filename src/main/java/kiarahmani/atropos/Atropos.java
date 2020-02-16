package kiarahmani.atropos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import kiarahmani.atropos.search_engine.Optimal_search_engine;
import kiarahmani.atropos.utils.Constants;
import kiarahmani.atropos.utils.Program_Utils;

class Analizer implements Callable<Boolean> {

	@Override
	public Boolean call() throws Exception {
		long time_begin = System.currentTimeMillis();
		try {
			new Constants();
		} catch (IOException e) {
		}
		Refactoring_Engine re = new Refactoring_Engine();
		Program_Utils pu = new Program_Utils("SmallBank");
		Program program = (new SmallBankProgramGenerator(pu)).generate("Balance", "Amalgamate", "TransactSavings",
				"DepositChecking", "SendPayment", "WriteCheck");
		pu.lock();
		program.printProgram();
		re.pre_analysis(pu);
		// search the refactoring space
		Naive_search_engine se = new Naive_search_engine();
		int _refactoring_depth = 1;
		for (int j = 0; j < _refactoring_depth; j++) {
			if (!se.reset(pu))
				continue;
			do {
				Delta ref = se.nextRefactoring(pu);
				re.refactor_schema(pu, ref);
			} while (se.hasNext());
		}
		re.atomicize(pu);
		program = pu.generateProgram();
		program.printProgram();
		int anml_cnt = analyze(program);
		System.gc();

		// print stats and exit
		printStats(System.currentTimeMillis() - time_begin, anml_cnt);
		return true;
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

public class Atropos {

	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public static void main(String[] args) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			executor.invokeAll(Arrays.asList(new Analizer()), 10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} // Timeout of 10 minutes.
		System.out.println("Time out...");
		executor.shutdown();
	}

}
