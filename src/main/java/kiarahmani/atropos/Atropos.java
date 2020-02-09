package kiarahmani.atropos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.vc.VC.VC_Agg;
import kiarahmani.atropos.DDL.vc.VC.VC_Type;
import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI_Graph;
import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.ProgramGenerator;
import kiarahmani.atropos.program_generators.SmallBank.SmallBankProgramGenerator;
import kiarahmani.atropos.refactoring_engine.Refactoring_Engine;
import kiarahmani.atropos.refactoring_engine.Modifiers.Query_Modifier;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTO.SELECT_Redirector;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.SELECT_Splitter;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.UPDATE_Duplicator;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.UPDATE_Splitter;
import kiarahmani.atropos.refactoring_engine.Modifiers.TTO.SELECT_Merger;
import kiarahmani.atropos.refactoring_engine.Modifiers.TTO.UPDATE_Merger;
import kiarahmani.atropos.refactoring_engine.deltas.ADDPK;
import kiarahmani.atropos.refactoring_engine.deltas.CHSK;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_F;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_R;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_VC;
import kiarahmani.atropos.search_engine.Naive_search_engine;
import kiarahmani.atropos.utils.Constants;
import kiarahmani.atropos.utils.Program_Utils;

public class Atropos {

	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public static void main(String[] args) {
		long time_begin = System.currentTimeMillis();
		try {
			new Constants();
		} catch (IOException e) {
		}
		Refactoring_Engine re = new Refactoring_Engine();
		Set<Program> results = new HashSet<>();
		Program_Utils pu = new Program_Utils("SmallBank");
		Program program = (new SmallBankProgramGenerator(pu)).generate("Balance1", "Amalgamate1", "TransactSavings1",
				"DepositChecking", "SendPayment1", "WriteCheck1");
		program.printProgram();
		pu.lock();

		// analyze the initial program
		// analyze(program);

		/*
		 * 
		 * Manual Schema Refactoring
		 * 
		 */

		// introduce new fields
		Delta delta_1 = new INTRO_F("accounts", "a_check_bal", F_Type.NUM);
		Delta delta_2 = new INTRO_F("accounts", "a_save_bal", F_Type.NUM);
		re.refactor_schema_seq(pu, new Delta[] { delta_1, delta_2 });

		// introduce vc between checking and accounts
		INTRO_VC delta_3 = new INTRO_VC(pu, "checking", "accounts", VC_Agg.VC_ID, VC_Type.VC_OTO);
		delta_3.addKeyCorrespondenceToVC("c_custid", "a_custid");
		delta_3.addFieldTupleToVC("c_bal", "a_check_bal");

		// introduce vc between savings and accounts
		INTRO_VC delta_4 = new INTRO_VC(pu, "savings", "accounts", VC_Agg.VC_ID, VC_Type.VC_OTO);
		delta_4.addKeyCorrespondenceToVC("s_custid", "a_custid");
		delta_4.addFieldTupleToVC("s_bal", "a_save_bal");
		re.refactor_schema_seq(pu, new Delta[] { delta_3, delta_4 });

		// program = pu.generateProgram();
		// program.printProgram();
		// analyze(program);

		// introduce a CRDT table for checking balance
		Delta delta_5 = new INTRO_R("checkin_bal_crdt", true);
		re.refactor_schema(pu, delta_5);

		// introduce new fields in checkin_bal_crdt
		Delta delta_6 = new INTRO_F("checkin_bal_crdt", "cbc_custid", F_Type.NUM);
		Delta delta_7 = new INTRO_F("checkin_bal_crdt", "cbc_uuids", F_Type.NUM, true, false);
		Delta delta_8 = new INTRO_F("checkin_bal_crdt", "cbc_bal", F_Type.NUM, false, true);

		re.refactor_schema_seq(pu, new Delta[] { delta_6, delta_7, delta_8 });

		Delta delta_9 = new ADDPK(pu, "checkin_bal_crdt", "cbc_custid");
		Delta delta_10 = new ADDPK(pu, "checkin_bal_crdt", "cbc_uuids");
		Delta delta_11 = new CHSK(pu, "checkin_bal_crdt", "cbc_custid");
		re.refactor_schema_seq(pu, new Delta[] { delta_9, delta_10, delta_11 });

		// introduce vc between accounts and checkin_bal_crdt
		INTRO_VC delta_12 = new INTRO_VC(pu, "accounts", "checkin_bal_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
		delta_12.addKeyCorrespondenceToVC("a_custid", "cbc_custid");
		delta_12.addFieldTupleToVC("a_check_bal", "cbc_bal");
		re.refactor_schema_seq(pu, new Delta[] { delta_12 });

		/*
		 * 
		 * End of refactoring
		 * 
		 */

		program = pu.generateProgram();
		program.printProgram();
		String txn = "DepositChecking";
		re.redirect_select(pu, txn, "checking", "accounts", 1, false);
		re.redirect_select(pu, txn, "accounts", "checkin_bal_crdt", 1, false);
		re.deleteQuery(pu, 2, txn);

		program = pu.generateProgram();
		program.printProgram();

		// analyze(program);

		// print stats and exit
		printStats(System.currentTimeMillis() - time_begin, results);
	}

	private static boolean programIsAccepted(Program candidate, Set<Program> current_results) {
		// TODO: implement acc/rej
		return true;
	}

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	private static void analyze(Program program) {
		Conflict_Graph cg = new Conflict_Graph(program);
		Encoding_Engine ee = new Encoding_Engine(program.getName());
		DAI_Graph dai_graph = ee.constructInitialDAIGraph(program, cg);
		cg.printGraph();
		dai_graph.printDAIGraph();
	}

	private static void printStats(long time, Set<Program> results) {
		System.out.println(
				"\n\n\n\n============================================================================================");
		System.out.println();
		System.out.println("Final Programs Count: " + results.size());
		System.out.println(results);
		System.out.println("Total Memory: "
				+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000 + " MB");
		System.out.println("Total Time:   " + time / 1000.0 + " s\n");
	}
}
