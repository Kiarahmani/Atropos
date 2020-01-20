package kiarahmani.atropos;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.ProgramGenerator;
import kiarahmani.atropos.program_generators.SmallBank.SmallBankProgramGenerator;
import kiarahmani.atropos.refactoring_engine.Refactoring_Engine;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_R;
import kiarahmani.atropos.refactoring_engine.vc.VC;
import kiarahmani.atropos.refactoring_engine.vc.VC.VC_Agg;
import kiarahmani.atropos.refactoring_engine.vc.VC.VC_Type;
import kiarahmani.atropos.refactoring_engine.vc.VC_Constraint;
import kiarahmani.atropos.utils.Constants;
import kiarahmani.atropos.utils.Program_Utils;

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

		Program_Utils pu = new Program_Utils("SmallBank");
		ProgramGenerator ipg = new SmallBankProgramGenerator(pu);
		Program program = ipg.generate("Balance", "Amalgamate1", "TransactSavings1", "DepositChecking1", "SendPayment1",
				"WriteCheck1");
		program.printProgram();

		Refactoring_Engine re = new Refactoring_Engine(pu);
		Delta intro_r = new INTRO_R("added");
		Program refactored_program = re.refactor(program, intro_r);
		VC vc = new VC(pu.getTableName("accounts"), pu.getFieldName("a_custid"), pu.getTableName("checking"),
				pu.getFieldName("c_custid"), VC_Agg.VC_ID, VC_Type.VC_OTO);
		vc.addConstraint(new VC_Constraint(pu.getFieldName("a_custid"), pu.getFieldName("c_custid")));
		refactored_program.addVC(vc);
		refactored_program.printProgram();

		// Conflict_Graph cg = new Conflict_Graph(program);
		// Encoding_Engine ee = new Encoding_Engine(program.getName());
		// DAI_Graph dai_graph = ee.constructInitialDAIGraph(program, cg); //
		long time_end = System.currentTimeMillis();
		// program.printProgram();
		// cg.printGraph();
		// dai_graph.printDAIGraph();
		System.out.println("\nTotal Time: " + (time_end - time_begin) / 1000.0 + " s\n");
	}
}
