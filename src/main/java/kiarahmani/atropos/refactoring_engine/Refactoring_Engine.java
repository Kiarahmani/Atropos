package kiarahmani.atropos.refactoring_engine;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Query.Kind;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.program.statements.If_Statement;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_F;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_R;
import kiarahmani.atropos.refactoring_engine.deltas.Modifiers.Query_Modifier;
import kiarahmani.atropos.utils.Program_Utils;
import kiarahmani.atropos.utils.Program_Utils;

public class Refactoring_Engine {
	private static final Logger logger = LogManager.getLogger(Atropos.class);

	/*
	 * Constructor
	 */
	public Refactoring_Engine() {
	}

	public Program_Utils refactor(Program_Utils input_pu, Delta delta) {
		String delta_class = delta.getClass().getSimpleName().toString();
		switch (delta_class) {
		case "INTRO_R":
			return apply_intro_r(input_pu, (INTRO_R) delta);
		case "ADDPK":
			return apply_addpk(input_pu);
		case "CHSK":
			return apply_chsk(input_pu);
		case "INTRO_F":
			return apply_intro_f(input_pu, (INTRO_F) delta);
		default:
			assert false : "Case not catched!" + delta_class;
			break;
		}
		return input_pu;
	}

	/*
	 * 
	 */
	private Program_Utils apply_intro_r(Program_Utils input_pu, INTRO_R intro_r) {
		logger.debug("applying INTRO_R refactoring");
		String table_name = intro_r.getNewTableName();
		input_pu.mkTable(table_name);
		return input_pu;
	}

	/*
	 * 
	 */
	private Program_Utils apply_intro_f(Program_Utils input_pu, INTRO_F intro_f) {
		logger.debug("applying INTRO_F refactoring");
		input_pu.addFieldNameToTable(intro_f.getTableName(), intro_f.getNewName());
		return input_pu;
	}

	/*
	 * 
	 */
	private Program_Utils apply_addpk(Program_Utils input_pu) {
		logger.debug("applying ADDPK refactoring");
		return input_pu;
	}

	/*
	 * 
	 */
	private Program_Utils apply_chsk(Program_Utils input_pu) {
		logger.debug("applying CHSK refactoring");
		return input_pu;
	}

	/*****************************************************************************************************************/
	// SWAP
	/*****************************************************************************************************************/
	/*
	 * perform a swap in the requested transaction
	 */
	public Program_Utils swapQueries(Program_Utils input_pu, String txnName, int q1_po, int q2_po) {
		assert (q1_po < q2_po) : "invalid args: first po must be less  than the second";
		Transaction txn = (Transaction) input_pu.getTrasnsactionMap().get(txnName);
		assert (txn != null) : "swap request is made on a transaction that does not exist";
		// guard the swaps from invalid requests
		if (!swapChecks(txn, q1_po, q2_po))
			return null;
		swapQueries_rec(txn.getStatements(), q1_po, q2_po);
		return input_pu;
	}

	/*
	 * Helping function used in swapQueries. It recusrsively checks continous blocks
	 * of statements
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
	private boolean swapChecks(Transaction txn, int q1_po, int q2_po) {
		if (q1_po > q2_po)
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
			assert (qry2 != null) : "unexpected state: qry2 is not set although qry1 has been found in the current block";
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
	// Update Queries in the statement lists
	/*****************************************************************************************************************/

	public Program_Utils applyAndPropagate(Program_Utils input_pu, Query_Modifier modifier, int apply_at_po,
			String txnName) {
		applyAtIndex(input_pu, modifier, apply_at_po, txnName);
		propagateToRange(input_pu, modifier, apply_at_po, txnName);
		return input_pu;
	}

	public Program_Utils applyAtIndex(Program_Utils input_pu, Query_Modifier modifier, int apply_at_po,
			String txnName) {
		assert (modifier.isSet()) : "cannot apply the modifier since it is not set";
		logger.debug("applying the modifer " + modifier + " at index: " + apply_at_po);
		Query old_qry = input_pu.getQueryByPo(txnName, apply_at_po);
		Query new_qry = modifier.atIndexModification(old_qry);
		deleteQuery(input_pu, apply_at_po, txnName);
		InsertQueriesAtPO(input_pu, txnName, apply_at_po, new Query_Statement(apply_at_po, new_qry));
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
		logger.debug("input list sze: " + inputList.size());
		logger.debug("input list: " + inputList);

		// for (Statement stmt : inputList) {
		for (int index = 0; index < inputList.size(); index++) {
			Statement stmt = inputList.get(index);
			switch (stmt.getClass().getSimpleName()) {
			case "Query_Statement":
				Query_Statement qry_stmt = (Query_Statement) stmt;
				logger.debug("current query statement: " + qry_stmt.getQuery().getId());
				if (found_flag) {
					inputList.remove(index);
					inputList.add(index, modifier.propagatedModification(qry_stmt));
				}

				if (qry_stmt.getQuery().getPo() == po_to_apply_after)
					found_flag = true;

				break;
			case "If_Statement":
				If_Statement if_stmt = (If_Statement) stmt;
				logger.debug("current if statement: " + if_stmt);
				if (found_flag) {
					inputList.remove(index);
					inputList.add(index, modifier.propagatedModification(if_stmt));
				}
				propagateToRange_rec(input_pu, modifier, po_to_apply_after, if_stmt.getIfStatements());
				propagateToRange_rec(input_pu, modifier, po_to_apply_after, if_stmt.getElseStatements());
				break;
			}
		}
	}

	/*
	 * Delete a Query from an existing transaction Subsequent queries must also be
	 * updated
	 */

	public Program_Utils deleteQuery(Program_Utils input_pu, int to_be_deleted_qry_po, String txnName) {
		Transaction txn = (Transaction) input_pu.getTrasnsactionMap().get(txnName);
		deleteQuery_rec(input_pu, to_be_deleted_qry_po, txn.getStatements());
		return input_pu;
	}

	private void deleteQuery_rec(Program_Utils input_pu, int to_be_deleted_qry_po, ArrayList<Statement> inputList) {
		boolean deleted_flag = false;
		int index = 0;
		int remove_index = 0;
		logger.debug("input list size: " + inputList.size());
		for (Statement stmt : inputList) {
			switch (stmt.getClass().getSimpleName()) {
			case "Query_Statement":
				Query_Statement qry_stmt = (Query_Statement) stmt;
				Query qry = qry_stmt.getQuery();
				logger.debug("analyzing query: " + qry.getId());
				if (deleted_flag) {
					// update PO (because one query has been removed)
					qry_stmt.updatePO(qry.getPo() - 1);
				}
				if (!deleted_flag && qry.getPo() == to_be_deleted_qry_po) {
					// remove the query from the list
					logger.debug("query to be deleted is found at index: " + index);
					remove_index = index;
					deleted_flag = true;
				}
				break;
			case "If_Statement":
				If_Statement if_stmt = (If_Statement) stmt;
				deleteQuery_rec(input_pu, to_be_deleted_qry_po, if_stmt.getIfStatements());
				deleteQuery_rec(input_pu, to_be_deleted_qry_po, if_stmt.getElseStatements());
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
	public Program_Utils InsertQueriesAtPO(Program_Utils input_pu, String txnName, int insert_index_po,
			Query_Statement... newQueryStatements) {
		Transaction txn = (Transaction) input_pu.getTrasnsactionMap().get(txnName);
		InsertQueriesAtPO_rec(input_pu, txn.getStatements(), insert_index_po, newQueryStatements);
		return input_pu;
	}

	private Program_Utils InsertQueriesAtPO_rec(Program_Utils input_pu, ArrayList<Statement> inputList,
			int insert_index_po, Query_Statement... newQueryStatements) {
		int index = 0;
		boolean found_flag = (insert_index_po == 0) ? true : false;
		int found_index = 0;
		int speca_fix = (insert_index_po == 0) ? 0 : 1;
		int new_qry_cnt = newQueryStatements.length;
		assert (new_qry_cnt > 0) : "cannot insert an empty array";
		loop_label: for (Statement stmt : inputList) {
			switch (stmt.getClass().getSimpleName()) {
			case "Query_Statement":
				Query_Statement qry_stmt = (Query_Statement) stmt;
				Query qry = qry_stmt.getQuery();
				if (found_flag) {
					qry_stmt.updatePO(qry_stmt.getQuery().getPo() + new_qry_cnt);
				} else if (qry.getPo() >= insert_index_po - 1) {
					found_flag = true;
					found_index = index;
				}
				break;
			case "If_Statement":
				If_Statement if_stmt = (If_Statement) stmt;
				InsertQueriesAtPO_rec(input_pu, if_stmt.getIfStatements(), insert_index_po, newQueryStatements);
				InsertQueriesAtPO_rec(input_pu, if_stmt.getElseStatements(), insert_index_po, newQueryStatements);
				break;
			}
			index++;
		}
		int iter = 0;
		if (found_flag)
			for (Query_Statement stmt : newQueryStatements) {
				stmt.updatePO(insert_index_po + iter);
				inputList.add(found_index + (iter++) + speca_fix, stmt);
			}
		return input_pu;
	}

}
