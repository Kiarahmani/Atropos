package kiarahmani.atropos.refactoring_engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Table;
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
			INTRO_R delta_cast = (INTRO_R) delta;
			return apply_intro_r(input_p, delta_cast.getNewTableName());
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
	private Program apply_intro_r(Program input_p, String table_name) {
		logger.debug("applying INTRO_R refactoring");
		TableName tn = new TableName(table_name);
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
