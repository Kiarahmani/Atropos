package kiarahmani.atropos;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.Transaction;

public class Atropos {

	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public static void main(String[] args) {
		logger.debug("Enter main");
		Program bank = new Program("bank");
		bank.printProgram();
	}
}
