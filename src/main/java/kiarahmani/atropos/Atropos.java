package kiarahmani.atropos;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.refactoring_engine.Refactoring_Engine;

public class Atropos {

	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public static void main(String[] args) {
		logger.debug("Enter main");
		InputProgramGenerator input_program_generator = new InputProgramGenerator();
		Refactoring_Engine refactoring_engine = new Refactoring_Engine();
		Encoding_Engine encoding_engine = new Encoding_Engine();

		Program bank = input_program_generator.generateBankProgram();

		refactoring_engine.setInitConfGraph(bank);

		bank.printProgram();

	}
}
