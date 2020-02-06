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
		// set the configs
		String txn_name = "test";
		int max_search_depth = 5;
		int max_intro_r = 1;
		int max_intro_f = 2;

		try {
			new Constants();
		} catch (IOException e) {
		}
		Naive_search_engine nse = new Naive_search_engine();
		Refactoring_Engine re = new Refactoring_Engine();
		Set<Program> results = new HashSet<>();
		Program_Utils pu = new Program_Utils("SmallBank");
		ProgramGenerator ipg = new SmallBankProgramGenerator(pu);

		Program program = ipg.generate("Balance1", "Amalgamate1", "TransactSavings1", "DepositChecking1", "SendPaymen1",
				"WriteCheck1", txn_name);
		program.printProgram();
		pu.lock();

		// Add a new field in accounts
		Delta intro_f = new INTRO_F("accounts", "a_sav_bal", F_Type.NUM);
		re.refactor_schema(pu, intro_f);

		// Add a new vc between savings and accounts
		INTRO_VC intro_vc0 = new INTRO_VC(pu, "savings", "accounts", VC_Agg.VC_ID, VC_Type.VC_OTO);
		intro_vc0.addKeyCorrespondenceToVC("s_custid", "a_custid");
		intro_vc0.addFieldTupleToVC("s_custid", "a_custid");
		intro_vc0.addFieldTupleToVC("s_bal", "a_sav_bal");
		re.refactor_schema(pu, intro_vc0);

		// add 3 new columns in car table
		re.refactor_schema(pu, new INTRO_F("car", "car_maker_name", F_Type.NUM));
		re.refactor_schema(pu, new INTRO_F("car", "car_maker_budget", F_Type.NUM));
		re.refactor_schema(pu, new INTRO_F("car", "car_maker_country", F_Type.NUM));

		// add a new vc between makes and car tables
		INTRO_VC intro_vc1 = new INTRO_VC(pu, "makers", "car", VC_Agg.VC_ID, VC_Type.VC_OTM);
		intro_vc1.addKeyCorrespondenceToVC("maker_id", "car_maker");
		intro_vc1.addFieldTupleToVC("maker_name", "car_maker_name");
		intro_vc1.addFieldTupleToVC("maker_budget", "car_maker_budget");
		intro_vc1.addFieldTupleToVC("maker_country", "car_maker_country");
		re.refactor_schema(pu, intro_vc1);

		// add vc between makers table and a CRDT table to hold maker's budget
		INTRO_VC intro_vc2 = new INTRO_VC(pu, "makers", "makers_budget_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM);
		intro_vc2.addKeyCorrespondenceToVC("maker_id", "mbc_maker_id");
		intro_vc2.addFieldTupleToVC("maker_budget", "mbc_amnt");
		re.refactor_schema(pu, intro_vc2);

		// change shard key of table accounts from custid to name
		// CHSK chsk = new CHSK(pu, "accounts", "a_name");
		// re.refactor_schema(pu,chsk);
		// pu.generateProgram().printProgram();
		pu.generateProgram().printProgram();
		String test_txn = "test";
		Query_Modifier x;

		// duplicate UPDATE(9) to makers_budget_crdt
		x = re.duplicate_update(pu, test_txn, "makers", "makers_budget_crdt", 10);
		pu.generateProgram().printProgram();

		re.revert_refactor_program(pu, x);
		pu.generateProgram().printProgram();

		// add a_name as PK of accounts
		// ADDPK addpk = new ADDPK(pu, "accounts", "a_name");
		// pu.refactor(addpk);
		// pu.generateProgram().printProgram();

		/*
		 * 
		 * // search the refactoring space for (int intro_r_index = 0; intro_r_index <
		 * max_intro_r; intro_r_index++) { INTRO_R new_intro_r = nse.nextIntroR();
		 * pu.refactor(new_intro_r); for (int intro_f_index = 0; intro_f_index <
		 * max_intro_f; intro_f_index++) { INTRO_F new_intro_f = nse.nextIntroF();
		 * pu.refactor(new_intro_f); nse.set(pu, max_search_depth); while
		 * (nse.hasNext()) { INTRO_VC new_intro_vc = nse.nextIntroVC();
		 * pu.refactor(new_intro_vc); program = pu.generateProgram();
		 * program.printProgram(); if (programIsAccepted(program, results))
		 * results.add(program); else { //TODO } } } }
		 * 
		 */
		// print stats and exit
		printStats(System.currentTimeMillis() - time_begin, results);
	}

	private static boolean programIsAccepted(Program candidate, Set<Program> current_results) {
		// TODO: implement
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
