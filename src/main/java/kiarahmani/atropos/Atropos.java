package kiarahmani.atropos;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.vc.*;
import kiarahmani.atropos.DDL.vc.VC.VC_Agg;
import kiarahmani.atropos.DDL.vc.VC.VC_Type;
import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI_Graph;
import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.program_generators.ProgramGenerator;
import kiarahmani.atropos.program_generators.SmallBank.SmallBankProgramGenerator;
import kiarahmani.atropos.refactoring_engine.Refactoring_Engine;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_F;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_R;
import kiarahmani.atropos.refactoring_engine.deltas.Modifiers.Query_Redirector;
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
		String test_string = "Balance";

		Program program = ipg.generate("Balance1", "Amalgamate1", "TransactSavings1", "DepositChecking1", "SendPaymen1",
				"WriteCheck1", test_string);
		program.printProgram();

		// create new refactoring engine
		Refactoring_Engine re = new Refactoring_Engine();

		Delta intro_f = new INTRO_F("accounts", "a_check_bal", F_Type.NUM);
		re.refactor(pu, intro_f);
		pu.mkVC("checking", "accounts", VC_Agg.VC_ID, VC_Type.VC_OTO,
				new VC_Constraint(pu.getFieldName("a_custid"), pu.getFieldName("c_custid")));
		pu.addFieldTupleToVC("vc_0", "c_custid", "a_custid");
		pu.addFieldTupleToVC("vc_0", "c_bal", "a_check_bal");
		Program refactored_program = re.refactor(pu, intro_f).generateProgram();
		refactored_program.printProgram();

		// Conflict_Graph cg = new Conflict_Graph(program);
		// Encoding_Engine ee = new Encoding_Engine(program.getName());
		// DAI_Graph dai_graph = ee.constructInitialDAIGraph(program, cg); //
		long time_end = System.currentTimeMillis();
		// program.printProgram();
		// cg.printGraph();
		// dai_graph.printDAIGraph();
		System.out.println("\nTotal Time: " + (time_end - time_begin) / 1000.0 + " s\n");

		System.out.println("\n\n\n\n\n\n\n\n");
		//pu.redirectQuery(test_string, 2, "accounts");
		//re.deleteQuery(pu, 2, test_string);
		//re.InsertQueriesAtPO(pu, test_string, 0, new Query_Statement[] {pu.mkTestQryStmt(10),pu.mkTestQryStmt(11),pu.mkTestQryStmt(12)});
		//refactored_program = pu.generateProgram();
		//refactored_program.printProgram();
		
		Query_Redirector qry_red = new Query_Redirector();
		qry_red.set(pu,test_string,"accounts");
		re.applyAtIndex(pu, qry_red, 2, test_string);
		refactored_program = pu.generateProgram();
		refactored_program.printProgram();
		
		//re.deleteQuery(pu, 2, test_string);
		//refactored_program = pu.generateProgram();
		//refactored_program.printProgram();

	}
}
