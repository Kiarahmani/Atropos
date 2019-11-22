package kiarahmani.atropos;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.InputProgramGenerator;
import kiarahmani.atropos.program_generators.TestInputProgramGenerator;
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
		TestInputProgramGenerator ipg = new TestInputProgramGenerator();
		Program program = ipg.generateUnitTestProgram("inc", "");

		Conflict_Graph cg = new Conflict_Graph(program);
		Encoding_Engine ee = new Encoding_Engine(program);

		program.printProgram();
		cg.printGraph();
		ee.constructInitialDAIGraph(program.getName(), cg);
	}
}
