package kiarahmani.atropos.refactoring_engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Query.Kind;
import kiarahmani.atropos.program.Block;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.program.Block.BlockType;
import kiarahmani.atropos.program.statements.If_Statement;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.refactoring_engine.Modifiers.Query_Modifier;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTO.One_to_One_Query_Modifier;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.One_to_Two_Query_Modifier;
import kiarahmani.atropos.refactoring_engine.Modifiers.TTO.Two_to_One_Query_Modifier;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_F;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_R;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_VC;
import kiarahmani.atropos.utils.Program_Utils;
import kiarahmani.atropos.utils.Tuple;

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
		case "INTRO_VC":
			return apply_intro_vc(input_pu, (INTRO_VC) delta);
		default:
			assert false : "Case not catched: " + delta_class;
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

	private Program_Utils apply_intro_vc(Program_Utils input_pu, INTRO_VC intro_f) {
		logger.debug("applying INTRO_VC refactoring");
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
		assert (fst_block.isEqual(sec_block)) : "can only apply TTO modifiers on queries in the same block";
		logger.debug("the original queries are found in block: " + fst_block);

		logger.debug(
				"Applying the modifer " + modifier + " at indexes: " + apply_at_po_fst + " and " + apply_at_po_sec);
		Query fst_qry = input_pu.getQueryByPo(txnName, apply_at_po_fst);
		Query sec_qry = input_pu.getQueryByPo(txnName, apply_at_po_sec);
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
