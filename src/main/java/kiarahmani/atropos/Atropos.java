package kiarahmani.atropos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
import kiarahmani.atropos.refactoring_engine.deltas.ADDPK;
import kiarahmani.atropos.refactoring_engine.deltas.CHSK;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_F;
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
		Naive_search_engine nse = new Naive_search_engine();
		Set<Program> results = new HashSet<>();
		Program_Utils pu = new Program_Utils("SmallBank");
		ProgramGenerator ipg = new SmallBankProgramGenerator(pu);
		String txn_name = "test";
		int max_new_intros = 20;
		int smt_check_freq = 3;
		Program program = ipg.generate("Balance1", "Amalgamate1", "TransactSavings1", "DepositChecking1", "SendPaymen1",
				"WriteCheck1", txn_name);
		program.printProgram();
		pu.lock();
		// search the refactoring space
		nse.set(pu, max_new_intros);
		for (int i = 0; i < max_new_intros; i++) {
			Delta schema_intro = nse.nextIntroduction();
			pu.refactor(schema_intro);
			while (nse.hasNextRef()) {
				Delta sche_update = nse.nextRefactoring();
				pu.refactor(sche_update);
				program = pu.generateProgram();
				program.printProgram();
			}	
		}

		while (nse.hasNextRef()) {
			Delta delta = nse.nextRefactoring();
			pu.refactor(delta);
			program = pu.generateProgram();
			program.printProgram();
			if (pu.getVersion() % smt_check_freq == 0)
				if (programIsAccepted(program, results))
					results.add(program);
		}
		printStats(System.currentTimeMillis() - time_begin, results);
	}

	private static boolean programIsAccepted(Program candidate, Set<Program> current_results) {
		// TODO: implement
		return true;
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
