package kiarahmani.atropos.refactoring_engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DDL.vc.VC;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Query.Kind;
import kiarahmani.atropos.program.Block;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.program.Block.BlockType;
import kiarahmani.atropos.program.statements.If_Statement;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.refactoring_engine.Modifiers.Query_Modifier;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTO.One_to_One_Query_Modifier;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTO.Query_ReAtomicizer;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTO.SELECT_Redirector;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTO.Var_Replacer;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.One_to_Two_Query_Modifier;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.SELECT_Splitter;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.UPDATE_Duplicator;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.UPDATE_Splitter;
import kiarahmani.atropos.refactoring_engine.Modifiers.TTO.SELECT_Merger;
import kiarahmani.atropos.refactoring_engine.Modifiers.TTO.Two_to_One_Query_Modifier;
import kiarahmani.atropos.refactoring_engine.Modifiers.TTO.UPDATE_Merger;
import kiarahmani.atropos.refactoring_engine.deltas.ADDPK;
import kiarahmani.atropos.refactoring_engine.deltas.CHSK;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_F;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_R;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_VC;
import kiarahmani.atropos.utils.Program_Utils;
import kiarahmani.atropos.utils.Tuple;

public class Refactoring_Engine {
	private static final Logger logger = LogManager.getLogger(Atropos.class);

	/*****************************************************************************************************************/
	// Functions for shrinking the program
	/*****************************************************************************************************************/
	public void atomicize(Program_Utils pu) {
		decompose(pu); // split and redirect all selects to tables with lower wights
		delete_redundant(pu);
	}

	private void delete_redundant(Program_Utils pu) {
		while (delete_redundant_iter(pu))
			;
	}

	private boolean delete_redundant_iter(Program_Utils pu) {
		boolean result = false;
		HashMap<Table, HashSet<FieldName>> accessed_fn_map = mkTableMap(pu);
		result |= delete_redundant_tables(pu, accessed_fn_map);
		result |= delete_redundant_writes(pu, accessed_fn_map);
		result |= delete_redundant_reads(pu);
		return result;
	}

	private boolean delete_redundant_tables(Program_Utils pu, HashMap<Table, HashSet<FieldName>> accessed_fn_map) {
		boolean result = false;
		ArrayList<Table> tables_to_be_removed = new ArrayList<>();
		for (Table t : pu.getTables().values())
			if (accessed_fn_map.get(t).size() == 0) {
				tables_to_be_removed.add(t);
				result = true;
			}
		for (Table t : tables_to_be_removed)
			pu.rmTable(t.getTableName().getName());
		return result;
	}

	private boolean delete_redundant_writes(Program_Utils pu, HashMap<Table, HashSet<FieldName>> accessed_fn_map) {
		boolean result = false;
		for (Transaction txn : pu.getTrasnsactionMap().values())
			for (Query q : txn.getAllQueries())
				if (q.isWrite()) {
					Table curr_t = pu.getTable(q.getTableName().getName());
					if (curr_t == null) { // table has already been removed
						deleteQuery(pu, q.getPo(), txn.getName());
						result = true;
					} else {
						HashSet<FieldName> currr_accessed = accessed_fn_map.get(curr_t);
						ArrayList<FieldName> curr_written = q.getWrittenFieldNames();
						ArrayList<FieldName> excluded_fns = new ArrayList<>();
						// figure out which fns are not used elsewhere
						for (FieldName fn : curr_written)
							if (!currr_accessed.contains(fn))
								excluded_fns.add(fn);
						if (excluded_fns.size() == curr_written.size()) { // if NONE of the updated fields is used
							deleteQuery(pu, q.getPo(), txn.getName());
							result = true;
						} else if (excluded_fns.size() > 0) { // if only part of the written fns are used
							int po = q.getPo();
							split_update(pu, txn.getName(), excluded_fns, po, false);
							deleteQuery(pu, po + 1, txn.getName());
						}
					}
				}
		return result;
	}

	private boolean delete_redundant_reads(Program_Utils pu) {
		boolean result = false;
		t_loop: for (Transaction txn : pu.getTrasnsactionMap().values())
			q_loop: for (Query q : txn.getAllQueries())
				if (!q.isWrite()) {
					Select_Query sq = (Select_Query) q;
					// check if sq is implicitly used
					if (sq.getImplicitlyUsed().size() > 0)
						continue q_loop;
					// check if sq is explicitly used
					Variable v = sq.getVariable();
					if (var_is_used_in_txn(pu, txn.getName(), v))
						continue q_loop;
					// at this point we know q is not either explicitly or implicitly used
					//deleteQuery(pu, q.getPo(), txn.getName());
					result = true;
				}
		return result;
	}

	private boolean var_is_used_in_txn(Program_Utils pu, String txnName, Variable v) {
		Transaction txn = pu.getTrasnsactionMap().get(txnName);
		for (Query q : txn.getAllQueries()) {
			if (q.getAllRefferencedVars().contains(v)) {
				logger.error(v + " is used in " + q.getId());
				return true;
			} else
				logger.error(v + " is NOT used in " + q.getId());
		}
		return false;
	}

	private void decompose(Program_Utils pu) {
		while (decompose_iter(pu)) // decompose untill fixed-point is reached
			;
	}

	// single iteration of decomposition
	private boolean decompose_iter(Program_Utils pu) {
		boolean result = false;
		// iterate over all selects, if any part of it can be redirected to a newer
		// table, do it
		for (Transaction txn : pu.getTrasnsactionMap().values()) {
			String txn_name = txn.getName();
			ArrayList<Query> all_queries = txn.getAllQueries();
			for (Query q : all_queries) {
				int po = q.getPo();
				if (!q.isWrite()) {
					Select_Query sq = (Select_Query) q;
					Table t_src = pu.getTable(q.getTableName().getName());
					for (Table t_dest : pu.getTables().values()) {
						ArrayList<FieldName> must_be_redirected = must_redirect(pu, sq, t_src, t_dest);
						if (must_be_redirected != null) { // some subset of fn accesses must be redirected
							int red_size = must_be_redirected.size();
							int total_size = sq.getSelectedFieldNames().size();
							if (red_size < total_size) {
								if (red_size > 0) {
									split_select(pu, txn_name, must_be_redirected, po, false);
									redirect_select(pu, txn_name, t_src.getTableName().getName(),
											t_dest.getTableName().getName(), po + 1, false);
									result = true;
								}
							} else {
								redirect_select(pu, txn_name, t_src.getTableName().getName(),
										t_dest.getTableName().getName(), po, false);
								result = true;
							}
						}
					}
				}
			}
		}
		return result;
	}

	private ArrayList<FieldName> must_redirect(Program_Utils pu, Select_Query q, Table src, Table dest) {
		ArrayList<FieldName> result = new ArrayList<>();
		VC vc = pu.getVCByOrderedTables(src.getTableName(), dest.getTableName());
		logger.debug("vc between " + src.getTableName() + " and " + dest.getTableName() + ": " + vc);
		if (vc != null) {
			for (FieldName fn : q.getSelectedFieldNames())
				if (vc.getCorrespondingFN(fn) != null)
					result.add(fn);
		} else
			return null;
		return result;
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
	 * 
	 * Main functions called from the outside
	 */
	public void shrink(Program_Utils pu) {
		for (Transaction txn : pu.getTrasnsactionMap().values()) {
			ArrayList<Query> all_queries = txn.getAllQueries();
			int qry_cnt = all_queries.size();
			for (int i = 0; i < qry_cnt; i++)
				for (int j = i + 1; j < qry_cnt; j++) {
					Query q1 = all_queries.get(i);
					Query q2 = all_queries.get(j);
					int po1 = q1.getPo();
					int po2 = q2.getPo();
					// reorder and,
					boolean swap_success = swap_queries(pu, txn.getName(), po1 + 1, po2, false);
					// if merge is successful continue
					if (attempt_merge_query(pu, txn.getName(), po1, false))
						continue;
					// otherwise, revert the attempted reordering
					if (swap_success)
						swap_queries(pu, txn.getName(), po1 + 1, po2, true);
				}
		}
	}

	/*
	 * public void cleanUp(Program_Utils pu) { while (cleanUp_iter(pu))// clean up
	 * untill no further cleanups occur ; }
	 */
	/*
	 * public boolean cleanUp_iter(Program_Utils pu) { ArrayList<Table>
	 * tables_to_be_removed = new ArrayList<>(); boolean result = false; out_loop:
	 * for (Table t : pu.getTables().values()) { HashMap<Select_Query, String>
	 * pot_sels_map = mkQueryToTableMap(pu, t); if (pot_sels_map.size() == 0) {
	 * deleteUnreadUpdates(pu, t); tables_to_be_removed.add(t); result = true; }
	 * else { // try to make the table redundant for (Table t_dest :
	 * pu.getTables().values()) { if
	 * (t.getTableName().equalsWith(t_dest.getTableName()) ||
	 * tables_to_be_removed.contains(t_dest) || pu.getVCByTables(t.getTableName(),
	 * t_dest.getTableName()) == null) continue; if (mkTableRedundant(pu, t, t_dest,
	 * pot_sels_map) == true) { deleteUnreadUpdates(pu, t);
	 * tables_to_be_removed.add(t); result = true; continue out_loop; } } } } for
	 * (Table t : tables_to_be_removed) pu.rmTable(t.getTableName().getName());
	 * return result; }
	 */
	/*
	 * Helping functions used in above funtions
	 */
	private boolean attempt_merge_query(Program_Utils input_pu, String txn_name, int qry_po, boolean isRevert) {
		logger.debug("");
		Query q1 = input_pu.getQueryByPo(txn_name, qry_po);
		Query q2 = input_pu.getQueryByPo(txn_name, qry_po + 1);
		if (q1 == null || q2 == null)
			return false;
		if (!input_pu.getBlockByPo(txn_name, qry_po).isEqual(input_pu.getBlockByPo(txn_name, qry_po + 1)))
			return false;
		logger.debug("attempting to merge queries q1(" + q1.getId() + ") and q2(" + q2.getId() + ")");
		logger.debug("q1: " + q1);
		logger.debug("q2: " + q2);

		TableName t1 = q1.getTableName();
		TableName t2 = q2.getTableName();

		VC vc = input_pu.getVCByTables(t1, t2);
		if (q1.getKind() == Kind.UPDATE && q2.getKind() == Kind.UPDATE) {
			UPDATE_Merger success = merge_update(input_pu, txn_name, qry_po, isRevert);
			logger.debug("updates merging attempted. result: " + (success != null));
			return (success != null);

		} else if (q1.getKind() == Kind.SELECT && q2.getKind() == Kind.SELECT) {
			SELECT_Merger success = merge_select(input_pu, txn_name, qry_po, isRevert);
			logger.debug("selects merging attempted. result: " + (success != null));
			if (success != null)
				return true;
			logger.debug("merge was unsuccessful");
			// if there is vc between q1 and q2
			if (vc != null) {
				/*
				 * try redirecting q2 to q1's table
				 */
				logger.debug("VC exits between tables: " + vc);
				logger.debug("redirecting q2(" + q2.getId() + ") from " + t2 + " to " + t1);
				SELECT_Redirector x = redirect_select(input_pu, txn_name, t2.getName(), t1.getName(), q2.getPo(),
						false);
				logger.debug("redirect(1) attempted. desc: " + x.getDesc());
				logger.debug("attempt merging the redirected q2 with original q1");
				success = merge_select(input_pu, txn_name, qry_po, isRevert);
				logger.debug("selects merging attempted. result: " + (success != null));
				if (success != null)
					return true;
				logger.debug("merge was unsuccessful: revert the redirection and try the opposit direction");
				// if unsuccessful revert the redirect
				x = redirect_select(input_pu, txn_name, t1.getName(), t2.getName(), q2.getPo(), true);
				logger.debug("REVERT(1) DESC: " + x.getDesc());
				/*
				 * repeat the opposit way; try redirecting q1 to q2's table
				 */
				logger.debug("now redirecting q1(" + q1.getId() + ") from " + t1 + " to " + t2);
				x = redirect_select(input_pu, txn_name, t1.getName(), t2.getName(), q1.getPo(), false);
				logger.debug("redirect(2) attempted. desc: " + x.getDesc());
				success = merge_select(input_pu, txn_name, qry_po, isRevert);
				if (success != null)
					return true;
				logger.debug("merge was again unsuccessful: revert the last redirection and exit");
				// if unsuccessful revert the redirect
				x = redirect_select(input_pu, txn_name, t2.getName(), t1.getName(), q1.getPo(), true);
				logger.debug("REVERT(2) DESC: " + x.getDesc());
			} else
				logger.debug("no vc exits for redirection: exit.");
		}
		return false;
	}

	private void deleteUnreadUpdates(Program_Utils pu, Table t) {
		for (Transaction txn : pu.getTrasnsactionMap().values())
			for (Query q : txn.getAllQueries())
				if (q.isWrite() && q.getTableName().equalsWith(t.getTableName()))
					deleteQuery(pu, q.getPo(), txn.getName());
	}

	private boolean mkTableRedundant(Program_Utils pu, Table t, Table dest_t,
			HashMap<Select_Query, String> pot_sels_map) {
		ArrayList<SELECT_Redirector> attempted_reds = new ArrayList<>();
		logger.debug("attempting to redirect all selects from " + t.getTableName() + " to " + dest_t.getTableName());
		boolean success = true;
		for (Select_Query q : pot_sels_map.keySet()) {
			// attempt redirecting q from t to dest_t
			SELECT_Redirector attempted_red = redirect_select(pu, pot_sels_map.get(q), t.getTableName().getName(),
					dest_t.getTableName().getName(), q.getPo(), false);
			attempted_reds.add(attempted_red);
			success &= (attempted_red != null);
		}
		if (success)
			return true;
		else {// revert
			for (SELECT_Redirector attempted_red : attempted_reds) {
				if (attempted_red != null)
					revert_redirect_select(pu, attempted_red);
			}
		}
		return false;
	}

	private HashMap<Table, HashSet<FieldName>> mkTableMap(Program_Utils pu) {
		HashMap<Table, HashSet<FieldName>> touched_field_names = new HashMap<>();
		for (Table tt : pu.getTables().values())
			touched_field_names.put(tt, new HashSet<>());

		for (Transaction txn : pu.getTrasnsactionMap().values())
			for (Query q : txn.getAllQueries())
				if (!q.isWrite()) {
					Table curr_t = pu.getTable(q.getTableName().getName());
					HashSet<FieldName> old_set = touched_field_names.get(curr_t);
					old_set.addAll(q.getReadFieldNames());
					old_set.addAll(((Select_Query) q).getImplicitlyUsed());
					touched_field_names.put(curr_t, old_set);
				}

		return touched_field_names;
	}

	/*****************************************************************************************************************/
	// Functions for handling schema refactoring requests (i.e. handling
	// Delta instances)
	/*****************************************************************************************************************/

	/*
	 * Main functions called to apply updates on the *schema*
	 */

	public Program_Utils refactor_schema_seq(Program_Utils input_pu, Delta... deltas) {
		for (Delta d : deltas)
			input_pu = refactor_schema(input_pu, d);
		return input_pu;
	}

	public Program_Utils refactor_schema(Program_Utils input_pu, Delta delta) {
		input_pu.incVersion();
		input_pu.addComment(
				"\n" + input_pu.getProgramName() + "(" + input_pu.getVersion() + "):	" + delta.getDesc());
		String delta_class = delta.getClass().getSimpleName().toString();
		switch (delta_class) {
		case "INTRO_R":
			return apply_intro_r(input_pu, (INTRO_R) delta);
		case "ADDPK":
			return apply_addpk(input_pu, (ADDPK) delta);
		case "CHSK":
			return apply_chsk(input_pu, (CHSK) delta);
		case "INTRO_F":
			return apply_intro_f(input_pu, (INTRO_F) delta);
		case "INTRO_VC":
			return apply_intro_vc(input_pu, (INTRO_VC) delta);
		default:
			assert false : "Case not catched: " + delta_class;
			break;
		}
		return input_pu;
	}

	/*
	 * case: Schema Delta was INTRO_R
	 */
	private Program_Utils apply_intro_r(Program_Utils input_pu, INTRO_R intro_r) {
		logger.debug("applying INTRO_R refactoring");
		String table_name = intro_r.getNewTableName();
		if (!intro_r.isCRDT())
			input_pu.mkTable(table_name);
		else
			input_pu.mkCRDTTable(table_name);
		return input_pu;
	}

	/*
	 * case: Schema Delta was INTRO_VC
	 */
	private Program_Utils apply_intro_vc(Program_Utils input_pu, INTRO_VC intro_vc) {
		logger.debug("applying INTRO_VC refactoring");
		TableName t1 = intro_vc.getVC().getTableName(1);
		TableName t2 = intro_vc.getVC().getTableName(2);
		// vc is already generated and added to pu; must now deal with the program
		for (Transaction txn : input_pu.getTrasnsactionMap().values())
			for (Query q : txn.getAllQueries())
				if (q.isWrite())
					if (q.getTableName().equalsWith(t1)) {
						logger.debug("query " + q.getId() + " is a write on T1 (" + t1.getName()
								+ ") and must be duplicated on T2 (" + t2.getName() + ")");
						UPDATE_Duplicator ud = duplicate_update(input_pu, txn.getName(), t1.getName(), t2.getName(),
								q.getPo());
						intro_vc.addAppliedUpDup(ud);

					} else if (q.getTableName().equalsWith(intro_vc.getVC().getTableName(2))) {
						logger.debug("query " + q.getId() + " is a write on T2 (" + t1.getName()
								+ ") and must be duplicated on T1 (" + t2.getName() + ")");
						UPDATE_Duplicator ud = duplicate_update(input_pu, txn.getName(), t2.getName(), t1.getName(),
								q.getPo());
						intro_vc.addAppliedUpDup(ud);
					}
		return input_pu;
	}

	/*
	 * case: Schema Delta was INTRO_F
	 */
	private Program_Utils apply_intro_f(Program_Utils input_pu, INTRO_F intro_f) {
		logger.debug("applying INTRO_F refactoring");
		input_pu.addFieldNameToTable(intro_f.getTableName(), intro_f.getNewName());
		return input_pu;
	}

	/*
	 * case: Schema Delta was ADDPK
	 */
	private Program_Utils apply_addpk(Program_Utils input_pu, ADDPK addpk) {
		logger.debug("applying ADDPK refactoring");
		addpk.getNewPK().setPK(true);
		logger.debug("schema updated. No need to update the program");
		return input_pu;
	}

	/*
	 * case: Schema Delta was CHSK
	 */
	private Program_Utils apply_chsk(Program_Utils input_pu, CHSK chsk) {
		logger.debug("applying CHSK refactoring");
		if (chsk.getOldSK() != null)
			chsk.getOldSK().setSK(false);
		chsk.getNewSK().setSK(true);
		logger.debug("schema updated");
		for (Transaction txn : input_pu.getTrasnsactionMap().values())
			for (Query q : txn.getAllQueries())
				if (q.getTableName().equalsWith(chsk.getTable().getTableName()))
					reAtomicize_qry(input_pu, txn.getName(), q.getPo(), false);
		logger.debug("program updated");
		return input_pu;
	}

	/*****************************************************************************************************************/
	// Functions for handling schema refactoring reverts (i.e. reverting
	// Delta instances)
	/*****************************************************************************************************************/
	/*
	 * Main functions called to revert updates on the *schema*
	 */

	public Program_Utils revert_refactor_schema_seq(Program_Utils input_pu, Delta... deltas) {
		for (Delta d : deltas)
			input_pu = revert_refactor_schema(input_pu, d);
		return input_pu;
	}

	public Program_Utils revert_refactor_schema(Program_Utils input_pu, Delta delta) {
		input_pu.decVersion();
		input_pu.addComment("\n !!REVERTED!! " + delta.getDesc());
		String delta_class = delta.getClass().getSimpleName().toString();
		switch (delta_class) {
		case "INTRO_R":
			return revert_apply_intro_r(input_pu, (INTRO_R) delta);
		case "ADDPK":
			return revert_apply_addpk(input_pu, (ADDPK) delta);
		case "CHSK":
			return revert_apply_chsk(input_pu, (CHSK) delta);
		case "INTRO_F":
			return revert_apply_intro_f(input_pu, (INTRO_F) delta);
		case "INTRO_VC":
			return revert_apply_intro_vc(input_pu, (INTRO_VC) delta);
		default:
			assert false : "Case not catched: " + delta_class;
			break;
		}
		return input_pu;
	}

	/*
	 * case: Schema Delta was INTRO_R
	 */
	private Program_Utils revert_apply_intro_r(Program_Utils input_pu, INTRO_R intro_r) {
		logger.debug("reverting INTRO_R refactoring");
		String table_name = intro_r.getNewTableName();
		input_pu.rmTable(table_name);
		return input_pu;
	}

	/*
	 * case: Schema Delta was INTRO_VC
	 */
	private Program_Utils revert_apply_intro_vc(Program_Utils input_pu, INTRO_VC intro_vc) {
		logger.debug("reverting INTRO_VC refactoring");
		input_pu.rmVC(intro_vc.getVC());
		for (int i = intro_vc.getAppliedUpDup().size() - 1; i >= 0; i--) // must be traversed reversely to make sure POs
																			// are not messed up
			revert_duplicate_update(input_pu, intro_vc.getAppliedUpDup().get(i));

		return input_pu;
	}

	/*
	 * case: Schema Delta was INTRO_F
	 */
	private Program_Utils revert_apply_intro_f(Program_Utils input_pu, INTRO_F intro_f) {
		logger.debug("reverting INTRO_F refactoring");
		input_pu.removeFieldNameFromTable(intro_f.getTableName(), intro_f.getNewName());
		return input_pu;
	}

	/*
	 * case: Schema Delta was ADDPK
	 */
	private Program_Utils revert_apply_addpk(Program_Utils input_pu, ADDPK addpk) {
		logger.debug("reverting ADDPK refactoring");
		addpk.getNewPK().setPK(false);
		logger.debug("schema updated. No need to update the program");
		return input_pu;
	}

	/*
	 * case: Schema Delta was CHSK
	 */
	private Program_Utils revert_apply_chsk(Program_Utils input_pu, CHSK chsk) {
		logger.debug("reverting CHSK refactoring");
		chsk.getOldSK().setSK(true);
		chsk.getNewSK().setSK(false);
		logger.debug("schema updated");
		for (Transaction txn : input_pu.getTrasnsactionMap().values())
			for (Query q : txn.getAllQueries())
				if (q.getTableName().equalsWith(chsk.getTable().getTableName()))
					reAtomicize_qry(input_pu, txn.getName(), q.getPo(), true);
		logger.debug("program updated");
		return input_pu;
	}

	/*****************************************************************************************************************/
	// Functions for handling program refactoring reverts (i.e. taking
	// Query_Modifier instances and processing them)
	/*****************************************************************************************************************/

	/*
	 * Dispatching function for the below functions
	 */

	public void revert_refactor_program_seq(Program_Utils input_pu, Query_Modifier... qms) {
		for (Query_Modifier qm : qms)
			revert_refactor_program(input_pu, qm);
	}

	public void revert_refactor_program(Program_Utils input_pu, Query_Modifier qm) {
		if (qm instanceof UPDATE_Duplicator) {
			UPDATE_Duplicator x = (UPDATE_Duplicator) qm;
			revert_duplicate_update(input_pu, x);
		}
		if (qm instanceof SELECT_Redirector) {
			SELECT_Redirector x = (SELECT_Redirector) qm;
			revert_redirect_select(input_pu, x);
		}
		if (qm instanceof SELECT_Merger) {
			SELECT_Merger x = (SELECT_Merger) qm;
			revert_merge_select(input_pu, x);
		}
		if (qm instanceof SELECT_Splitter) {
			SELECT_Splitter x = (SELECT_Splitter) qm;
			revert_split_select(input_pu, x);
		}
		if (qm instanceof UPDATE_Merger) {
			UPDATE_Merger x = (UPDATE_Merger) qm;
			revert_merge_update(input_pu, x);
		}
		if (qm instanceof UPDATE_Splitter) {
			UPDATE_Splitter x = (UPDATE_Splitter) qm;
			revert_split_update(input_pu, x);
		}
	}

	public void revert_duplicate_update(Program_Utils input_pu, UPDATE_Duplicator ud) {
		input_pu.decVersion();
		input_pu.addComment("\n !!REVERTED!! " + ud.getDesc());
		deleteQuery(input_pu, ud.getOrgDupPo() + 1, ud.getTxnName());
	}

	public void revert_redirect_select(Program_Utils input_pu, SELECT_Redirector select_red) {
		input_pu.decVersion();
		input_pu.addComment("\n !!REVERTED!! " + select_red.getDesc());
		String original_txn_name = select_red.getTxnName();
		String original_src_table = select_red.getSourceTable().getTableName().getName();
		String original_target_table = select_red.getTargetTable().getTableName().getName();
		redirect_select(input_pu, original_txn_name, original_target_table, original_src_table,
				select_red.getApplied_po(), true);
	}

	public void revert_merge_select(Program_Utils input_pu, SELECT_Merger select_merger) {
		input_pu.decVersion();
		input_pu.addComment("\n !!REVERTED!! " + select_merger.getDesc());

		String txnName = select_merger.getTxnName();
		int originalPO = select_merger.getOriginal_applied_po();
		Select_Query original_q1 = select_merger.getOld_select1();
		Select_Query original_q2 = select_merger.getOld_select2();
		Block originalBlock = select_merger.getOriginal_block();

		deleteQuery(input_pu, originalPO, txnName);
		InsertQueriesAtPO(originalBlock, input_pu, txnName, originalPO, new Query_Statement(originalPO, original_q1),
				new Query_Statement(originalPO + 1, original_q2));

		Var_Replacer vr = new Var_Replacer();
		// replace old var1
		vr.set(input_pu, txnName, original_q1.getSelectedFieldNames(), select_merger.getNewVar(),
				original_q1.getVariable());
		applyAndPropagate(input_pu, vr, originalPO + 1, txnName);
		// replace old var2
		vr.set(input_pu, txnName, original_q2.getSelectedFieldNames(), select_merger.getNewVar(),
				original_q2.getVariable());
		applyAndPropagate(input_pu, vr, originalPO + 1, txnName);

	}

	public void revert_split_select(Program_Utils input_pu, SELECT_Splitter select_splt) {
		input_pu.decVersion();
		input_pu.addComment("\n !!REVERTED!! " + select_splt.getDesc());
		merge_select(input_pu, select_splt.getTxnName(), select_splt.getOriginal_applied_po(), true);
	}

	public void revert_merge_update(Program_Utils input_pu, UPDATE_Merger upd_merger) {
		input_pu.decVersion();
		input_pu.addComment("\n !!REVERTING!! " + upd_merger.getDesc());
		split_update(input_pu, upd_merger.getTxnName(), upd_merger.getOld_update2().getWrittenFieldNames(),
				upd_merger.getOriginal_applied_po(), true);
	}

	public void revert_split_update(Program_Utils input_pu, UPDATE_Splitter upd_splt) {
		input_pu.decVersion();
		input_pu.addComment("\n !!REVERTED!! " + upd_splt.getDesc());
		merge_update(input_pu, upd_splt.getTxnName(), upd_splt.getOriginal_applied_po(), true);
	}

	/*****************************************************************************************************************/
	// Functions for handling program refactoring requests (i.e. generating
	// Query_Modifier instances and processing them)
	/*****************************************************************************************************************/

	public SELECT_Redirector redirect_select(Program_Utils input_pu, String txn_name, String src_table,
			String dest_table, int qry_po, boolean isRevert) {
		SELECT_Redirector select_red = new SELECT_Redirector();
		select_red.set(input_pu, txn_name, src_table, dest_table);
		if (select_red.isValid(input_pu.getQueryByPo(txn_name, qry_po))) {
			applyAndPropagate(input_pu, select_red, qry_po, txn_name);
			String begin;
			if (isRevert) {
				input_pu.decVersion();
				begin = "              ";
			} else {
				input_pu.incVersion();
				begin = input_pu.getProgramName() + "(" + input_pu.getVersion() + "):	";
			}
			input_pu.addComment("\n" + begin + select_red.getDesc());

			select_red.setApplied_po(qry_po);
			return select_red;
		} else
			return null;
	}

	public Query_ReAtomicizer reAtomicize_qry(Program_Utils input_pu, String txn_name, int qry_po, boolean isRevert) {
		Query_ReAtomicizer qry_atom = new Query_ReAtomicizer();
		qry_atom.set(input_pu, txn_name);
		if (qry_atom.isValid(input_pu.getQueryByPo(txn_name, qry_po))) {
			applyAndPropagate(input_pu, qry_atom, qry_po, txn_name);
			String begin;
			if (isRevert) {
				input_pu.decVersion();
				begin = "              ";
			} else {
				input_pu.incVersion();
				begin = input_pu.getProgramName() + "(" + input_pu.getVersion() + "):	";
			}
			input_pu.addComment("\n" + begin + qry_atom.getDesc());
			return qry_atom;
		} else
			return null;
	}

	public SELECT_Merger merge_select(Program_Utils input_pu, String txn_name, int qry_po, boolean isRevert) {
		SELECT_Merger select_merger = new SELECT_Merger();
		select_merger.set(input_pu, txn_name);
		if (select_merger.isValid(input_pu.getQueryByPo(txn_name, qry_po),
				input_pu.getQueryByPo(txn_name, qry_po + 1))) {
			applyAndPropagate(input_pu, select_merger, qry_po, txn_name);
			String begin;
			if (isRevert) {
				input_pu.decVersion();
				begin = "              ";
			} else {
				input_pu.incVersion();
				begin = input_pu.getProgramName() + "(" + input_pu.getVersion() + "):	";
			}
			input_pu.addComment("\n" + begin + select_merger.getDesc());
			select_merger.setOriginal_applied_po(qry_po);
			select_merger.setOriginal_block(input_pu.getBlockByPo(txn_name, qry_po));
			return select_merger;
		} else
			return null;
	}

	public SELECT_Splitter split_select(Program_Utils input_pu, String txn_name, ArrayList<FieldName> excluded_fns,
			int qry_po, boolean isRevert) {
		SELECT_Splitter select_splt = new SELECT_Splitter();
		select_splt.set(input_pu, txn_name, excluded_fns);
		if (select_splt.isValid(input_pu.getQueryByPo(txn_name, qry_po))) {
			applyAndPropagate(input_pu, select_splt, qry_po, txn_name);
			String begin;
			if (isRevert) {
				input_pu.decVersion();
				begin = "              ";
			} else {
				input_pu.incVersion();
				begin = input_pu.getProgramName() + "(" + input_pu.getVersion() + "):	";
			}
			input_pu.addComment("\n" + begin + select_splt.getDesc());
			select_splt.setOriginal_applied_po(qry_po);
			return select_splt;
		} else
			return null;

	}

	public UPDATE_Merger merge_update(Program_Utils input_pu, String txn_name, int qry_po, boolean isRevert) {
		UPDATE_Merger upd_merger = new UPDATE_Merger();
		upd_merger.set(input_pu, txn_name);
		if (upd_merger.isValid(input_pu.getQueryByPo(txn_name, qry_po), input_pu.getQueryByPo(txn_name, qry_po + 1))) {
			applyAndPropagate(input_pu, upd_merger, qry_po, txn_name);
			String begin;
			if (isRevert) {
				input_pu.decVersion();
				begin = "              ";
			} else {
				input_pu.incVersion();
				begin = input_pu.getProgramName() + "(" + input_pu.getVersion() + "):	";
			}
			input_pu.addComment("\n" + begin + upd_merger.getDesc());
			upd_merger.setOriginal_applied_po(qry_po);
			return upd_merger;
		} else
			return null;

	}

	public UPDATE_Splitter split_update(Program_Utils input_pu, String txn_name, ArrayList<FieldName> excluded_fns_upd,
			int qry_po, boolean isRevert) {
		UPDATE_Splitter upd_splt = new UPDATE_Splitter();
		upd_splt.set(input_pu, txn_name, excluded_fns_upd);
		if (upd_splt.isValid(input_pu.getQueryByPo(txn_name, qry_po))) {
			applyAndPropagate(input_pu, upd_splt, qry_po, txn_name);
			String begin;
			if (isRevert) {
				input_pu.decVersion();
				begin = "              ";
			} else {
				input_pu.incVersion();
				begin = input_pu.getProgramName() + "(" + input_pu.getVersion() + "):	";
			}
			input_pu.addComment("\n" + begin + upd_splt.getDesc());
			upd_splt.setOriginal_applied_po(qry_po);

			return upd_splt;
		} else
			return null;

	}

	public UPDATE_Duplicator duplicate_update(Program_Utils input_pu, String txn_name, String source_table,
			String target_table, int qry_po) {
		UPDATE_Duplicator upd_dup = new UPDATE_Duplicator();
		upd_dup.set(input_pu, txn_name, source_table, target_table);
		if (upd_dup.isValid(input_pu.getQueryByPo(txn_name, qry_po))) {
			applyAndPropagate(input_pu, upd_dup, qry_po, txn_name);
			input_pu.incVersion();
			input_pu.addComment(
					"\n" + input_pu.getProgramName() + "(" + input_pu.getVersion() + "):	" + upd_dup.getDesc());
			upd_dup.setOrgDupPo(qry_po);
			return upd_dup;
		} else {
			logger.debug("attempted duplication of po#" + qry_po + "from " + source_table + " to " + target_table
					+ " but failed");
			return null;
		}
	}

	public boolean swap_queries(Program_Utils input_pu, String txnName, int q1_po, int q2_po, boolean isRevert) {
		Transaction txn = (Transaction) input_pu.getTrasnsactionMap().get(txnName);
		assert (txn != null) : "swap request is made on a transaction that does not exist";
		// guard the swaps from invalid requests
		if (q1_po > q2_po || !swapChecks(input_pu, txn, q1_po, q2_po))
			return false;
		swapQueries_rec(txn.getStatements(), q1_po, q2_po);
		return true;
	}

	/*****************************************************************************************************************/
	// Helping functions for SWAP
	/*****************************************************************************************************************/

	/*
	 * Helping function used in swap_queries. It recusrsively checks continous
	 * blocks of statements
	 */
	private void swapQueries_rec(ArrayList<Statement> inputList, int q1_po, int q2_po) {
		int iter = 0;
		int index_po1 = -1, index_po2 = -1;
		loop_label: for (Statement stmt : inputList) {
			switch (stmt.getClass().getSimpleName()) {
			case "Query_Statement":
				Query_Statement qry_stmt = (Query_Statement) stmt;
				Query qry = qry_stmt.getQuery();
				if (qry.getPo() == q1_po) {
					qry.updatePO(q2_po);
					index_po1 = iter;
					break;
				}
				if (qry.getPo() == q2_po) {
					assert (index_po1 != -1) : "unexpected state: q2 is found in the current block but"
							+ " q1 belongs to another (possibly outer) block";
					qry.updatePO(q1_po);
					index_po2 = iter;
					break loop_label;
				}
				break;
			case "If_Statement":
				If_Statement if_stmt = (If_Statement) stmt;
				swapQueries_rec(if_stmt.getIfStatements(), q1_po, q2_po);
				swapQueries_rec(if_stmt.getElseStatements(), q1_po, q2_po);
				break;
			default:
				break;
			}
			iter++;
		}
		if (index_po1 != -1 && index_po2 != -1)
			Collections.swap(inputList, index_po1, index_po2);
	}

	/*
	 * Check if the requested swap is valid or not
	 */
	private boolean swapChecks(Program_Utils pu, Transaction txn, int q1_po, int q2_po) {
		if (q1_po >= q2_po)
			return false;
		if (!pu.getBlockByPo(txn.getName(), q1_po).isEqual(pu.getBlockByPo(txn.getName(), q2_po)))
			return false;
		Boolean result = swapChecks_rec(txn.getStatements(), q1_po, q2_po);
		assert (result != null) : "the requested swap is invalid and cannot be checked";
		return result;
	}

	/*
	 * Helping function used in swapChecks. Recursively analyzes the contineous
	 * blocks of statements
	 */
	private Boolean swapChecks_rec(ArrayList<Statement> inputList, int q1_po, int q2_po) {
		ArrayList<Query> pot_dep_qries = new ArrayList<>();
		Query qry1 = null;
		Query qry2 = null;
		boolean q1_seen_flag = false;
		loop_label: for (Statement stmt : inputList) {
			switch (stmt.getClass().getSimpleName()) {
			case "Query_Statement":
				Query_Statement qry_stmt = (Query_Statement) stmt;
				Query qry = qry_stmt.getQuery();
				if (qry.getPo() == q1_po) {
					q1_seen_flag = true;
					qry1 = qry;
					pot_dep_qries.add(qry);
					break;
				}
				if (qry.getPo() == q2_po) {
					assert (q1_seen_flag) : "unexpected state: q2 is found in the current block but"
							+ " q1 belongs to another (possibly outer) block";
					qry2 = qry;
					break loop_label;
				}
				if (q1_seen_flag && qryAreDep(qry1, qry))
					pot_dep_qries.add(qry);
				break;
			case "If_Statement":
				If_Statement if_stmt = (If_Statement) stmt;
				Boolean if_result = swapChecks_rec(if_stmt.getIfStatements(), q1_po, q2_po);
				if (if_result != null)
					return if_result;
				Boolean else_result = swapChecks_rec(if_stmt.getElseStatements(), q1_po, q2_po);
				if (else_result != null)
					return else_result;
				break;
			default:
				break;
			}
		}
		if (q1_seen_flag) {
			assert (qry2 != null) : "unexpected state: qry2 is not set although qry1 has been found in the current block po1= "
					+ q1_po + "   po2= " + q2_po;
			for (Query q : pot_dep_qries)
				if (qryAreDep(q, qry2))
					return false;
			return true;
		} else
			return null;
	}

	/*
	 * check if two queries are dependent on each other or not
	 */
	private boolean qryAreDep(Query qry1, Query qry2) {
		assert (qry1.getPo() < qry2
				.getPo()) : "unexpected state: algorithm assumes this function is only called for po1<po2 -> po1:"
						+ qry1.getPo() + " po2:" + qry2.getPo();
		if (qry1.getKind() == Kind.SELECT) {
			Select_Query slct_qry = (Select_Query) qry1;
			Variable var1 = slct_qry.getVariable();
			return qry2.getAllRefferencedVars().contains(var1);
			// the only case where true (identifying a dependency) is returned is when the
			// first query is a select and
			// the second query has a reference to the variable created by the first one
		}
		return false;
	}
	/*****************************************************************************************************************/

	/*****************************************************************************************************************/
	// Helping functions to update Queries in the statement lists (called by the
	// above dispatcher functions)
	/*****************************************************************************************************************/

	/*
	 * Main function called to apply updates on the *prorgam*
	 */

	public Program_Utils applyAndPropagate(Program_Utils input_pu, Query_Modifier modifier, int apply_at_po,
			String txnName) {
		switch (modifier.type) {
		case OTO:
			One_to_One_Query_Modifier otoqm = (One_to_One_Query_Modifier) modifier;
			logger.debug("First calling applyAtIndex to apply the desired modification at index");
			applyAtIndex(input_pu, otoqm, apply_at_po, txnName);
			logger.debug(
					"Now calling propagateToRange to apply the deisred modifications at all subsequent statements");
			propagateToRange(input_pu, modifier, apply_at_po, txnName);
			break;

		case OTT:
			One_to_Two_Query_Modifier ottqm = (One_to_Two_Query_Modifier) modifier;
			logger.debug("First calling applyAtIndex to apply the desired modification at index");
			applyAtIndex(input_pu, ottqm, apply_at_po, txnName);
			logger.debug(
					"Now calling propagateToRange to apply the deisred modifications at all subsequent statements");
			propagateToRange(input_pu, modifier, apply_at_po, txnName);
			break;

		case TTO:
			Two_to_One_Query_Modifier ttoqm = (Two_to_One_Query_Modifier) modifier;
			logger.debug("First calling applyAtIndex to apply the desired modification at index");
			applyAtIndex(input_pu, ttoqm, apply_at_po, apply_at_po + 1, txnName);
			logger.debug(
					"Now calling propagateToRange to apply the deisred modifications at all subsequent statements");
			propagateToRange(input_pu, modifier, apply_at_po, txnName);
			break;
		default:
			assert (false) : "unexpected state: Query_Modifier has an unknown type: " + modifier.type;
			break;
		}

		return input_pu;
	}

	/*
	 * 
	 * 1-1 At Index Modifications
	 * 
	 */

	public Program_Utils applyAtIndex(Program_Utils input_pu, One_to_One_Query_Modifier modifier, int apply_at_po,
			String txnName) {
		assert (modifier.isSet()) : "cannot apply the modifier since it is not set";
		logger.debug("Applying the modifer " + modifier + " at index: " + apply_at_po);
		Query old_qry = input_pu.getQueryByPo(txnName, apply_at_po);
		Query new_qry = modifier.atIndexModification(old_qry);
		logger.debug(
				"old query (" + old_qry.getId() + ") is going to be replaced with new query (" + new_qry.getId() + ")");
		Block orig_block = input_pu.getBlockByPo(txnName, apply_at_po);
		logger.debug("the old query was found in block: " + orig_block);
		logger.debug("applyAtIndex: new Calling deleteQuery to remove the old query from the transaction");
		deleteQuery(input_pu, apply_at_po, txnName);
		logger.debug("new Calling InsertQueriesAtPO to add the new query to the transaction");
		InsertQueriesAtPO(orig_block, input_pu, txnName, apply_at_po, new Query_Statement(apply_at_po, new_qry));
		return input_pu;
	}

	/*
	 * 
	 * 2-1 At Index Modifications
	 * 
	 */

	public Program_Utils applyAtIndex(Program_Utils input_pu, Two_to_One_Query_Modifier modifier, int apply_at_po_fst,
			int apply_at_po_sec, String txnName) {
		assert (apply_at_po_sec == apply_at_po_fst + 1) : "can only apply TTO modifiers on adjacent queries";
		assert (modifier.isSet()) : "cannot apply the modifier since it is not set";
		Block fst_block = input_pu.getBlockByPo(txnName, apply_at_po_fst);
		Block sec_block = input_pu.getBlockByPo(txnName, apply_at_po_sec);
		logger.debug(
				"Applying the modifer " + modifier + " at indexes: " + apply_at_po_fst + " and " + apply_at_po_sec);
		Query fst_qry = input_pu.getQueryByPo(txnName, apply_at_po_fst);
		Query sec_qry = input_pu.getQueryByPo(txnName, apply_at_po_sec);
		assert (fst_block.isEqual(sec_block)) : "can only apply TTO modifiers on queries in the same block (" + fst_qry
				+ ") and (" + sec_qry + ")";
		logger.debug("the original queries are found in block: " + fst_block);

		// define new query
		Query new_qry = modifier.atIndexModification(fst_qry, sec_qry);
		logger.debug("old queries (" + fst_qry.getId() + ") and (" + sec_qry.getId()
				+ ") are going to be replaced with new query (" + new_qry.getId() + ")");

		logger.debug("now calling deleteQuery (twice) to remove the old queries from the transaction");
		deleteQuery(input_pu, apply_at_po_fst, txnName);
		logger.debug("po=" + apply_at_po_fst + " is removed");
		deleteQuery(input_pu, apply_at_po_fst, txnName); // BE CAREFUL! SINCE apply_at_po_fst is already deleted,
															// now what used to be at apply_at_po_sec resides at
															// po=apply_at_po_fst and hence the function is called on
															// apply_at_po_fst again
		logger.debug("po=" + apply_at_po_sec + " is removed");

		logger.debug("now calling InsertQueriesAtPO to add the new query to the transaction");
		InsertQueriesAtPO(fst_block, input_pu, txnName, apply_at_po_fst, new Query_Statement(apply_at_po_fst, new_qry));

		// return
		return input_pu;
	}

	/*
	 * 
	 * 1-2 At Index Modifications
	 * 
	 */

	public Program_Utils applyAtIndex(Program_Utils input_pu, One_to_Two_Query_Modifier modifier, int apply_at_po,
			String txnName) {
		assert (modifier.isSet()) : "cannot apply the modifier since it is not set";
		logger.debug("Applying the modifer " + modifier + " at index: " + apply_at_po);
		Query old_qry = input_pu.getQueryByPo(txnName, apply_at_po);
		Tuple<Query, Query> new_qry_tuple = modifier.atIndexModification(old_qry);
		logger.debug("old query (" + old_qry.getId() + ") is going to be replaced with new queries ("
				+ new_qry_tuple.x.getId() + ") and (" + new_qry_tuple.y.getId() + ")");
		Block orig_block = input_pu.getBlockByPo(txnName, apply_at_po);
		logger.debug("the old query was found in block: " + orig_block);
		logger.debug("applyAtIndex: new Calling deleteQuery to remove the old query from the transaction");
		deleteQuery(input_pu, apply_at_po, txnName);
		logger.debug("new Calling InsertQueriesAtPO to add the new query to the transaction");
		InsertQueriesAtPO(orig_block, input_pu, txnName, apply_at_po, new Query_Statement(apply_at_po, new_qry_tuple.x),
				new Query_Statement(apply_at_po, new_qry_tuple.y));

		return input_pu;
	}

	/*
	 * 
	 * Applies the given modifier's propagate function on all statements starting
	 * with the first statement after po=first_po_to_apply
	 * 
	 */
	public Program_Utils propagateToRange(Program_Utils input_pu, Query_Modifier modifier, int po_to_apply_after,
			String txnName) {
		Transaction txn = (Transaction) input_pu.getTrasnsactionMap().get(txnName);
		propagateToRange_rec(input_pu, modifier, po_to_apply_after, txn.getStatements());
		return input_pu;
	}

	private void propagateToRange_rec(Program_Utils input_pu, Query_Modifier modifier, int po_to_apply_after,
			ArrayList<Statement> inputList) {
		boolean found_flag = false;
		logger.debug("propagateToRange_rec: input list: "
				+ inputList.stream().map(stmt -> stmt.getSimpleName()).collect(Collectors.toList()));
		for (int index = 0; index < inputList.size(); index++) {
			Statement stmt = inputList.get(index);
			switch (stmt.getClass().getSimpleName()) {
			case "Query_Statement":
				Query_Statement qry_stmt = (Query_Statement) stmt;
				logger.debug("analyzing query statement: " + qry_stmt.getSimpleName());
				if (qry_stmt.getQuery().getPo() > po_to_apply_after) {
					logger.debug("Current query must be updated");
					inputList.remove(index);
					Query_Statement new_query = modifier.propagatedQueryModification(qry_stmt);
					new_query.updatePO(qry_stmt.getQuery().getPo());
					inputList.add(index, new_query);
					logger.debug("updated query: " + new_query.getSimpleName());
					found_flag = true;
				} else {
					logger.debug("Current query was not affected");
				}
				if (qry_stmt.getQuery().getPo() == po_to_apply_after)
					found_flag = true;

				break;
			case "If_Statement":
				If_Statement if_stmt = (If_Statement) stmt;
				logger.debug("Current if statement: " + if_stmt.getSimpleName());
				if (!found_flag) {
					logger.debug("found_flag=false: " + "calling self on if statements (of size: "
							+ if_stmt.getIfStatements().size() + ")");

					propagateToRange_rec(input_pu, modifier, po_to_apply_after, if_stmt.getIfStatements());
					logger.debug("found_flag=false: " + "calling self on else statements *of size: "
							+ if_stmt.getElseStatements().size() + ")");
					propagateToRange_rec(input_pu, modifier, po_to_apply_after, if_stmt.getElseStatements());
				} else {
					logger.debug("found_flag=true: the if condition must be updated: "
							+ "first calling self on if statements of size: " + if_stmt.getIfStatements().size());
					propagateToRange_rec(input_pu, modifier, po_to_apply_after, if_stmt.getIfStatements());
					logger.debug("found_flag=true: " + "first calling self on else statements of size: "
							+ if_stmt.getElseStatements().size());
					propagateToRange_rec(input_pu, modifier, po_to_apply_after, if_stmt.getElseStatements());
					logger.debug("now removing old if statement");
					inputList.remove(index);
					If_Statement new_if_stmt = new If_Statement(if_stmt.getIntId(),
							modifier.propagatedExpModification(if_stmt.getCondition()));
					logger.debug("new if statement is created: " + new_if_stmt.getSimpleName());
					for (Statement updated_stmt : if_stmt.getIfStatements()) {
						logger.debug("adding statement (" + updated_stmt.getSimpleName() + ") in new if");
						new_if_stmt.addStatementInIf(updated_stmt);
					}
					for (Statement updated_stmt : if_stmt.getElseStatements()) {
						logger.debug("adding statement (" + updated_stmt.getSimpleName() + ") in new else");
						new_if_stmt.addStatementInElse(updated_stmt);
					}
					inputList.add(index, new_if_stmt);
				}
				break;
			}
		}
	}

	/*
	 * Delete a Query from an existing transaction Subsequent queries must also be
	 * updated
	 */

	public void deleteQuery(Program_Utils input_pu, int to_be_deleted_qry_po, String txnName) {
		Transaction txn = (Transaction) input_pu.getTrasnsactionMap().get(txnName);
		deleteQuery_rec(new Block(BlockType.INIT, 0, -1), false, input_pu, to_be_deleted_qry_po, txn.getStatements());

	}

	private void deleteQuery_rec(Block current_block, boolean is_found, Program_Utils input_pu,
			int to_be_deleted_qry_po, ArrayList<Statement> inputList) {
		logger.debug("current block: " + current_block);
		boolean deleted_flag = false;
		int index = 0;
		int remove_index = 0;
		logger.debug("input list size: " + inputList.size());
		for (Statement stmt : inputList) {
			switch (stmt.getClass().getSimpleName()) {
			case "Query_Statement":
				Query_Statement qry_stmt = (Query_Statement) stmt;
				Query qry = qry_stmt.getQuery();
				if (is_found) {
					// update PO (because one query has been removed)
					logger.debug("analyzing query: " + qry.getId() + "(po:" + qry.getPo() + ")"
							+ "---> po will be decremented since the deleted query has already been found");
					qry_stmt.updatePO(qry.getPo() - 1);
					logger.debug("deleteQuery_rec: new PO: " + qry.getPo());
				}
				if (!is_found && !deleted_flag && qry.getPo() == to_be_deleted_qry_po) {
					// remove the query from the list
					logger.debug("query to be deleted (" + qry.getId() + ") is found at index: " + index);
					remove_index = index;
					deleted_flag = true;
					is_found = true;
				}
				break;
			case "If_Statement":
				If_Statement if_stmt = (If_Statement) stmt;
				logger.debug("analyzing if: " + if_stmt.getSimpleName());
				deleteQuery_rec(new Block(BlockType.IF, current_block.getDepth() + 1, if_stmt.getIntId()), is_found,
						input_pu, to_be_deleted_qry_po, if_stmt.getIfStatements());
				deleteQuery_rec(new Block(BlockType.ELSE, current_block.getDepth() + 1, if_stmt.getIntId()), is_found,
						input_pu, to_be_deleted_qry_po, if_stmt.getElseStatements());
				break;
			}
			index++;
		}
		if (deleted_flag)
			inputList.remove(remove_index);
	}

	/*
	 * Inserts a sequence of new queries into an existing transaction
	 * insert_index_po specifies the final PO assigned to the first newly added
	 * query
	 */
	public Program_Utils InsertQueriesAtPO(Block desired_block, Program_Utils input_pu, String txnName,
			int insert_index_po, Query_Statement... newQueryStatements) {
		Transaction txn = (Transaction) input_pu.getTrasnsactionMap().get(txnName);
		InsertQueriesAtPO_rec((insert_index_po == 0), desired_block, new Block(BlockType.INIT, 0, -1),
				txn.getStatements(), input_pu, insert_index_po, newQueryStatements);
		return input_pu;
	}

	private boolean InsertQueriesAtPO_rec(boolean must_inc_po, Block desired_block, Block current_block,
			ArrayList<Statement> inputList, Program_Utils input_pu, int insert_index_po,
			Query_Statement... newQueryStatements) {
		logger.debug(
				"input list: " + inputList.stream().map(stmt -> stmt.getSimpleName()).collect(Collectors.toList()));
		int new_qry_cnt = newQueryStatements.length;
		logger.debug("new query count: " + new_qry_cnt);
		assert (new_qry_cnt > 0) : "cannot insert an empty array";
		int found_index = 0;
		for (int index = 0; index < inputList.size(); index++) {
			Statement stmt = inputList.get(index);
			switch (stmt.getClass().getSimpleName()) {
			case "Query_Statement":
				Query_Statement qry_stmt = (Query_Statement) stmt;
				Query qry = qry_stmt.getQuery();

				if (must_inc_po) {
					logger.debug("analyzing query: " + qry.getId() + "(po:" + qry.getPo() + ")"
							+ "---> po will be increased since insert has already been occured");
					qry_stmt.updatePO(qry_stmt.getQuery().getPo() + new_qry_cnt);
					logger.debug("new PO:" + qry.getPo());
				}
				if (qry.getPo() == insert_index_po - 1) {
					logger.debug("found the query right before where the new queries will be added: " + qry.getId());
					must_inc_po = true;
					found_index = index + 1;
				}
				break;
			case "If_Statement":
				If_Statement if_stmt = (If_Statement) stmt;
				InsertQueriesAtPO_rec(must_inc_po, desired_block,
						new Block(BlockType.IF, current_block.getDepth() + 1, if_stmt.getIntId()),
						if_stmt.getIfStatements(), input_pu, insert_index_po, newQueryStatements);
				InsertQueriesAtPO_rec(must_inc_po, desired_block,
						new Block(BlockType.ELSE, current_block.getDepth() + 1, if_stmt.getIntId()),
						if_stmt.getElseStatements(), input_pu, insert_index_po, newQueryStatements);

				break;
			}
		}

		if (current_block.isEqual(desired_block)) {
			logger.debug("injecting the new queries to the current list of statements");
			int iter = 0;
			for (Query_Statement stmt : newQueryStatements) {
				stmt.updatePO(insert_index_po + iter);
				inputList.add(found_index + (iter++), stmt);
			}
		}
		return false;
	}

}
