package kiarahmani.atropos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI_Graph;
import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.TPCCProgramGenerator;
import kiarahmani.atropos.program_generators.SmallBank.SmallBankProgramGenerator;
import kiarahmani.atropos.refactoring_engine.Refactoring_Engine;
import kiarahmani.atropos.utils.Constants;
import kiarahmani.atropos.utils.Program_Utils;

public class Atropos {

	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public static void main(String[] args) {
		try {
			new Constants();
		} catch (IOException e) {
			e.printStackTrace();
		}

		long time_begin = System.currentTimeMillis();
		Program_Utils pu = new Program_Utils("TPC-C");
		Program program = (new TPCCProgramGenerator(pu)).generate("newOrder", "payment", "stockLevel", "orderStatus",
				"delivery");
		
		//Program_Utils pu = new Program_Utils("SmallBank");
		//Program program = (new SmallBankProgramGenerator(pu)).generate("Balance", "Amalgamate", "TransactSavings",
		//		"DepositChecking", "SendPayment", "WriteCheck");
		
		pu.lock();
		program.printProgram();
		int anml_cnt = analyze(pu);
		printStats(System.currentTimeMillis() - time_begin, anml_cnt);
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
