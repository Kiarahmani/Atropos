package kiarahmani.atropos.refactoring_engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_R;
import kiarahmani.atropos.utils.Program_Utils;

public class Refactoring_Engine {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private Program_Utils pu;

	public Refactoring_Engine(Program_Utils pu) {
		this.pu = pu;
	}

	public Program refactor(Program input_p, Delta delta) {
		String delta_class = delta.getClass().getSimpleName().toString();
		switch (delta_class) {
		case "INTRO_R":
			return apply_intro_r(input_p, (INTRO_R) delta);
		case "ADDPK":
			return apply_addpk(input_p);
		case "CHSK":
			return apply_chsk(input_p);
		case "INTRO_F":
			return apply_intro_f(input_p);
		default:
			assert false : "Case not catched!" + delta_class;
			break;
		}
		return input_p;
	}

	/*
	 * 
	 */
	private Program apply_intro_r(Program input_p, INTRO_R intro_r) {
		logger.debug("applying INTRO_R refactoring");
		String table_name = intro_r.getNewTableName();
		Table t = pu.addTable(table_name);
		input_p.addTable(t);
		return input_p;
	}

	/*
	 * 
	 */
	private Program apply_intro_f(Program input_p) {
		logger.debug("applying INTRO_F refactoring");
		return input_p;
	}

	/*
	 * 
	 */
	private Program apply_addpk(Program input_p) {
		logger.debug("applying ADDPK refactoring");
		return input_p;
	}

	/*
	 * 
	 */
	private Program apply_chsk(Program input_p) {
		logger.debug("applying CHSK refactoring");
		return input_p;
	}

}
