package kiarahmani.atropos;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI_Graph;
import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.ProgramGenerator;
import kiarahmani.atropos.program_generators.SmallBankProgramGenerator;
import kiarahmani.atropos.program_generators.UnifiedCRDTSmallBankProgramGenerator;
import kiarahmani.atropos.program_generators.UnifiedSmallBankProgramGenerator;
import kiarahmani.atropos.program_generators.SemiUnifiedCRDTSmallBankProgramGenerator;

import kiarahmani.atropos.utils.Constants;

public class Atropos {

	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public static void main(String[] args) {
		logger.debug("Enter main");
		try { 
			new Constants();
		} catch (IOException e) {
			e.printStackTrace();
		}
		long time_begin = System.currentTimeMillis();
		logger.debug("New Constants object initialized");

		ProgramGenerator ipg = new SemiUnifiedCRDTSmallBankProgramGenerator();
		Program program = ipg.generate("Balance1", "Amalgamate1", "TransactSavings1", "DepositChecking1", "SendPayment1",
				"WriteCheck");
		program.printProgram();

		Conflict_Graph cg = new Conflict_Graph(program);
		Encoding_Engine ee = new Encoding_Engine(program.getName());
		DAI_Graph dai_graph = ee.constructInitialDAIGraph(program, cg); //
		long time_end = System.currentTimeMillis();
		program.printProgram();
		cg.printGraph();
		dai_graph.printDAIGraph();
		System.out.println("\nTotal Time: " + (time_end - time_begin) / 1000.0 + " s\n");
	}
}
