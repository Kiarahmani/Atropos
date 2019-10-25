package kiarahmani.atropos;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.program.Program;

public class Atropos {

	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public static void main(String[] args) {
		logger.debug("Enter main");
		InputProgramGenerator input_program_generator = new InputProgramGenerator();
		Program bank = input_program_generator.generateBankProgram();
		bank.printProgram();
	}
}
