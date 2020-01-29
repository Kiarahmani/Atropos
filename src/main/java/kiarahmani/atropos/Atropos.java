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
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.ProgramGenerator;
import kiarahmani.atropos.program_generators.SmallBank.SmallBankProgramGenerator;
import kiarahmani.atropos.refactoring_engine.Refactoring_Engine;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_F;
import kiarahmani.atropos.refactoring_engine.deltas.Modifiers.Query_Redirector;
import kiarahmani.atropos.refactoring_engine.deltas.Modifiers.SELECT_Splitter;
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

		Program base_program = ipg.generate("Balance1", "Amalgamate1", "TransactSavings1", "DepositChecking1",
				"SendPaymen1", "WriteCheck1", test_txn);
		base_program.printProgram();

		// Create new refactoring engine
		Refactoring_Engine re = new Refactoring_Engine();

		// Add a new field
		Delta intro_f = new INTRO_F("accounts", "a_sav_bal", F_Type.NUM);
		re.refactor(pu, intro_f);
		Program new_field_program = pu.generateProgram();
		new_field_program.printProgram();

		// Add a new vc
		pu.mkVC("savings", "accounts", VC_Agg.VC_ID, VC_Type.VC_OTO,
				new VC_Constraint(pu.getFieldName("a_custid"), pu.getFieldName("s_custid")));
		pu.addFieldTupleToVC("vc_0", "s_custid", "a_custid");
		pu.addFieldTupleToVC("vc_0", "s_bal", "a_sav_bal");
		Program refactored_program = pu.generateProgram();
		refactored_program.printProgram();

		// Instantiate a new modifier (redirector) and apply it
		Query_Redirector qry_red = new Query_Redirector();
		qry_red.set(pu, test_txn, "savings", "accounts");
		re.applyAndPropagate(pu, qry_red, 2, test_txn);
		Program redirected_program = pu.generateProgram();
		redirected_program.printProgram();

		// Instantiate a new modifier (redirector) and apply it
		SELECT_Splitter qry_splt = new SELECT_Splitter();
		ArrayList<FieldName> excluded_fns = new ArrayList<>();
		excluded_fns.add(pu.getFieldName("a_name"));
		qry_splt.set(pu, test_txn, excluded_fns);
		re.applyAndPropagate(pu, qry_splt, 0, test_txn);
		Program splitted_program = pu.generateProgram();
		splitted_program.printProgram();

		// Print Running Time
		long time_end = System.currentTimeMillis();
		System.out.println("\nTotal Time: " + (time_end - time_begin) / 1000.0 + " s\n");

	}
}
