package kiarahmani.atropos;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI_Graph;
import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.refactoring_engine.Refactoring_Engine;
import kiarahmani.atropos.utils.Constants;

public class Atropos {

	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public static void main(String[] args) {
		logger.debug("Enter main");
		try {
			Constants constants = new Constants();
		} catch (IOException e) {
			e.printStackTrace();
		}
		InputProgramGenerator ipg = new InputProgramGenerator();
		Refactoring_Engine re = new Refactoring_Engine();
		Encoding_Engine ee = new Encoding_Engine();

		Program program = ipg.generateVerySimpleBankingProgram();
		Conflict_Graph conflict_graph = re.constructConfGraph(program);
		DAI_Graph dai_graph = ee.constructInitialDAIGraph(program, conflict_graph);

		program.printProgram();
		conflict_graph.printGraph();
	}
}
