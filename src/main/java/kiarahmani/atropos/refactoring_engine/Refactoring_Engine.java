package kiarahmani.atropos.refactoring_engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_R;
import kiarahmani.atropos.utils.Program_Utils;
import kiarahmani.atropos.utils.Program_Utils_NEW;

public class Refactoring_Engine {
	private static final Logger logger = LogManager.getLogger(Atropos.class);

	/*
	 * Constructor
	 */
	public Refactoring_Engine() {
	}

	public Program_Utils_NEW refactor(Program_Utils_NEW input_pu, Delta delta) {
		String delta_class = delta.getClass().getSimpleName().toString();
		switch (delta_class) {
		case "INTRO_R":
			return apply_intro_r(input_pu, (INTRO_R) delta);
		case "ADDPK":
			return apply_addpk(input_pu);
		case "CHSK":
			return apply_chsk(input_pu);
		case "INTRO_F":
			return apply_intro_f(input_pu);
		default:
			assert false : "Case not catched!" + delta_class;
			break;
		}
		return input_pu;
	}

	/*
	 * 
	 */
	private Program_Utils_NEW apply_intro_r(Program_Utils_NEW input_pu, INTRO_R intro_r) {
		logger.debug("applying INTRO_R refactoring");
		String table_name = intro_r.getNewTableName();
		input_pu.mkTable(table_name);
		return input_pu;
	}

	/*
	 * 
	 */
	private Program_Utils_NEW apply_intro_f(Program_Utils_NEW input_pu) {
		logger.debug("applying INTRO_F refactoring");
		return input_pu;
	}

	/*
	 * 
	 */
	private Program_Utils_NEW apply_addpk(Program_Utils_NEW input_pu) {
		logger.debug("applying ADDPK refactoring");
		return input_pu;
	}

	/*
	 * 
	 */
	private Program_Utils_NEW apply_chsk(Program_Utils_NEW input_pu) {
		logger.debug("applying CHSK refactoring");
		return input_pu;
	}

	/**
	 * @param refactored_program
	 * @return
	 */
	public Program shrink(Program refactored_program) {
		// TODO Auto-generated method stub
		return null;
	}

}
