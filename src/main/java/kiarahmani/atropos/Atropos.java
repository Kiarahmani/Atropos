package kiarahmani.atropos;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.refactoring_engine.Refactoring_Engine;

public class Atropos {

	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public static void main(String[] args) {
		logger.debug("Enter main");
		InputProgramGenerator ipg = new InputProgramGenerator();
		Refactoring_Engine re = new Refactoring_Engine();

		Program program = ipg.generateVerySimpleBankingProgram();
		Conflict_Graph conflict_graph = re.constructConfGraph(program);

		program.printProgram();
		conflict_graph.printGraph();
	}
}
