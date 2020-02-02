package kiarahmani.atropos;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.vc.*;
import kiarahmani.atropos.DDL.vc.VC.VC_Agg;
import kiarahmani.atropos.DDL.vc.VC.VC_Type;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.ProgramGenerator;
import kiarahmani.atropos.program_generators.SmallBank.SmallBankProgramGenerator;
import kiarahmani.atropos.refactoring_engine.Refactoring_Engine;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTO.SELECT_Redirector;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.SELECT_Splitter;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.UPDATE_Duplicator;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.UPDATE_Splitter;
import kiarahmani.atropos.refactoring_engine.Modifiers.TTO.SELECT_Merger;
import kiarahmani.atropos.refactoring_engine.Modifiers.TTO.UPDATE_Merger;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_F;
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
		String test_txn = "test";

		Program program = ipg.generate("Balance1", "Amalgamate1", "TransactSavings1", "DepositChecking1", "SendPaymen1",
				"WriteCheck1", test_txn);
		program.printProgram();
		pu.lock(); // make sure that certain features of pu won't be used anymore

		/*
		 * BEGIN REFACTORING
		 */

		// Add a new field
		Delta intro_f = new INTRO_F("accounts", "a_sav_bal", F_Type.NUM);
		pu.refactor(intro_f);
		pu.generateProgram().printProgram();

		// Add a new vc
		pu.mkVC("savings", "accounts", VC_Agg.VC_ID, VC_Type.VC_OTO,
				new VC_Constraint(pu.getFieldName("a_custid"), pu.getFieldName("s_custid")));
		pu.addFieldTupleToVC("vc_0", "s_custid", "a_custid");
		pu.addFieldTupleToVC("vc_0", "s_bal", "a_sav_bal");
		pu.generateProgram().printProgram();

		// add new columns in car table
		Delta intro_car = new INTRO_F("car", "car_maker_name", F_Type.NUM);
		pu.refactor(intro_car);
		intro_car = new INTRO_F("car", "car_maker_budget", F_Type.NUM);
		pu.refactor(intro_car);
		intro_car = new INTRO_F("car", "car_maker_country", F_Type.NUM);
		pu.refactor(intro_car);

		// Add a new vc
		pu.mkVC("makers", "car", VC_Agg.VC_ID, VC_Type.VC_OTM,
				new VC_Constraint(pu.getFieldName("maker_id"), pu.getFieldName("car_maker")));
		pu.addFieldTupleToVC("vc_1", "maker_name", "car_maker_name");
		pu.addFieldTupleToVC("vc_1", "maker_budget", "car_maker_budget");
		pu.addFieldTupleToVC("vc_1", "maker_country", "car_maker_country");
		pu.generateProgram().printProgram();

		// add vc between makers table and a CRDT table to hold maker's budget
		pu.mkVC("makers", "makers_budget_crdt", VC_Agg.VC_SUM, VC_Type.VC_OTM,
				new VC_Constraint(pu.getFieldName("maker_id"), pu.getFieldName("mbc_maker_id")));
		pu.addFieldTupleToVC("vc_2", "maker_budget", "mbc_amnt");

		// redirect SELECT2 from savings to accounts
		pu.redirect_select(test_txn, "savings", "accounts", 2);
		pu.generateProgram().printProgram();

		// split SELECT0
		ArrayList<FieldName> excluded_fns = new ArrayList<>();
		excluded_fns.add(pu.getFieldName("a_name"));
		pu.split_select(test_txn, excluded_fns, 0);
		pu.generateProgram().printProgram();

		// split UPDATE5
		ArrayList<FieldName> excluded_fns_upd = new ArrayList<>();
		excluded_fns_upd.add(pu.getFieldName("c_bal"));
		pu.split_update(test_txn, excluded_fns_upd, 5);
		pu.generateProgram().printProgram();

		// merge UPDATE5 and UPDATE6
		pu.merge_update(test_txn, 5);
		pu.generateProgram().printProgram();

		// merge UPDATE5 and UPDATE6 (again)
		pu.merge_update(test_txn, 5);
		pu.generateProgram().printProgram();

		// merge SELECT0 and SELECT1
		pu.merge_select(test_txn, 0);
		pu.generateProgram().printProgram();

		// merge SELECT0 and SELECT1 (again)
		pu.merge_select(test_txn, 0);
		pu.generateProgram().printProgram();

		// redirect SELECT5 from makers to car
		pu.redirect_select(test_txn, "makers", "car", 5);
		pu.generateProgram().printProgram();

		// merge SELECT4 and SELECT5
		pu.merge_select(test_txn, 4);
		pu.generateProgram().printProgram();

		// duplicate UPDATE5 to accounts table (test OTO VC)
		pu.duplicate_update(test_txn, "savings", "accounts", 5);
		pu.generateProgram().printProgram();

		// duplicate UPDATE(7) to table car (test OTM VC: T1 to T2)
		pu.duplicate_update(test_txn, "makers", "car", 7);
		pu.generateProgram().printProgram();

		// duplicate UPDATE(7) to table car (test OTM VC: T2 to T1)
		pu.duplicate_update(test_txn, "car", "makers", 8);
		pu.generateProgram().printProgram();

		// redirect SELECT(10) to CRDT copy in makers_budget_crdt table
		pu.redirect_select(test_txn, "makers", "makers_budget_crdt", 10);
		pu.generateProgram().printProgram();

		// duplicate UPDATE(9) to makers_budget_crdt
		pu.duplicate_update(test_txn, "makers", "makers_budget_crdt", 9);
		pu.generateProgram().printProgram();

		/*
		 * 
		 * END OF REFACTORING
		 * 
		 */

		// Print Running Time
		long time_end = System.currentTimeMillis();
		System.out.println("\nTotal Time: " + (time_end - time_begin) / 1000.0 + " s\n");

	}

}
