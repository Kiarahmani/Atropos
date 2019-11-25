package kiarahmani.atropos;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.CRDTSmallBankProgramGenerator;
import kiarahmani.atropos.program_generators.SmallBankProgramGenerator;
import kiarahmani.atropos.program_generators.TestInputProgramGenerator;
import kiarahmani.atropos.program_generators.UnifiedCRDTSmallBankProgramGenerator;
import kiarahmani.atropos.program_generators.UnifiedSmallBankProgramGenerator;
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
		//SmallBankProgramGenerator ipg = new SmallBankProgramGenerator();
		//UnifiedSmallBankProgramGenerator ipg = new UnifiedSmallBankProgramGenerator();
		//CRDTSmallBankProgramGenerator ipg = new CRDTSmallBankProgramGenerator();
		UnifiedCRDTSmallBankProgramGenerator ipg = new UnifiedCRDTSmallBankProgramGenerator(); 
		Program program = ipg.generate("Balance1", "Amalgamate1", "TransactSavings", "DepositChecking", "SendPayment1",
				"WriteCheck1");

		Conflict_Graph cg = new Conflict_Graph(program);
		Encoding_Engine ee = new Encoding_Engine(program);

		program.printProgram();
		cg.printGraph();
		ee.constructInitialDAIGraph(program.getName(), cg);
	}
}
