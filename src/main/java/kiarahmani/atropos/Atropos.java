package kiarahmani.atropos;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.program.Program;
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
		logger.debug("New Constants object initialized");
		InputProgramGenerator ipg = new InputProgramGenerator();
		// WriteCheck
		// TransactSavings
		// SendPayment
		// DepositChecking
		// Balance
		// Amalgamate
		Program program = ipg.generateUnitTestProgram("DepositChecking", "Balance", "WriteCheck", "TransactSavings",
				"SendPayment", "Amalgamate");

		Conflict_Graph cg = new Conflict_Graph(program);
		Encoding_Engine ee = new Encoding_Engine(program);

		program.printProgram();
		cg.printGraph();
		ee.constructInitialDAIGraph(program.getName(), cg);

		// conflict_graph.printGraph();
	}

}
