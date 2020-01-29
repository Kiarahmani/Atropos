/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.Modifiers.OTO;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DDL.vc.VC;
import kiarahmani.atropos.DDL.vc.VC.VC_Type;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.utils.Program_Utils;

/**
 * @author Kiarash
 *
 */
public class Query_Redirector extends One_to_One_Query_Modifier {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private Program_Utils pu;
	private Table targetTable;
	private Table sourceTable;
	private String txnName;
	private VC vc;
	private Redirection_Type type;
	private Select_Query old_select;
	private Variable new_var;

	private enum Redirection_Type {
		T1_TO_T2, T2_TO_T1;
	}

	/*
	 * Set the stage before modifying. Function set must be called before calling
	 * modify each time.
	 */
	public void set(Program_Utils pu, String txnName, String sourceTableName, String targetTableName) {
		this.pu = pu;
		this.txnName = txnName;
		this.targetTable = pu.getTable(targetTableName);
		this.sourceTable = pu.getTable(sourceTableName);
		this.vc = pu.getVCByTables(sourceTable.getTableName(), targetTable.getTableName());
		logger.debug("Appropriate VC between old and new tables: " + vc);
		this.type = get_redirection_type();
		logger.debug("Redirection type: " + this.type + " -- " + vc.getType());
		super.set();
	}

	public Query atIndexModification(Query input_query) {
		// extract old query's details
		old_select = (Select_Query) input_query;
		int old_po = old_select.getPo();
		TableName old_tableName = old_select.getTableName();
		ArrayList<FieldName> old_selected_fieldNames = old_select.getSelectedFieldNames();
		Variable old_var = old_select.getVariable();
		WHC old_whc = old_select.getWHC();

		// make sure the redirection is possible
		assert (modificationIsValid(old_select, vc)) : "requested modification cannot be done";

		// create new query's details
		int new_select_id = pu.getNewSelectId(txnName);
		logger.debug("new select id: " + new_select_id);
		WHC new_whc = updateWHC(old_whc);
		logger.debug("new where clause: " + new_whc);
		boolean new_isAtomic = new_whc.isAtomic(targetTable.getShardKey());
		logger.debug("new is atomic: " + new_isAtomic);
		new_var = updateVar(old_var);
		logger.debug("new variable: " + new_var);
		ArrayList<FieldName> new_selected_fieldNames = updateFNs(old_tableName, old_selected_fieldNames);
		logger.debug("new selected field names: " + new_selected_fieldNames);

		// create new query
		Select_Query new_select = new Select_Query(old_po, new_select_id, new_isAtomic, targetTable.getTableName(),
				new_selected_fieldNames, new_var, new_whc);
		logger.debug("final select query to return: " + new_select);
		// return
		return new_select;
	}

	/*
	 * This function will perform the variable substitution(non-Javadoc)
	 */
	@Override
	public Expression propagatedExpModification(Expression input_exp) {
		for (FieldName old_fn : old_select.getSelectedFieldNames())
			input_exp.redirectProjs(old_select.getVariable(), old_fn, new_var, vc.getCorrespondingFN(old_fn));
		return input_exp;
	}

	@Override
	public Query_Statement propagatedQueryModification(Query_Statement input_qry) {
		for (FieldName old_fn : old_select.getSelectedFieldNames())
			input_qry.getQuery().redirectProjs(old_select.getVariable(), old_fn, new_var,
					vc.getCorrespondingFN(old_fn));
		return input_qry;
	}

	private boolean modificationIsValid(Select_Query input_query, VC vc) {
		// all keys used as the WHC of redirecting SELECT must be constrained by vc
		boolean assumption1 = vc.containsWHC(input_query.getWHC());

		// there must be a correspondence from old table to new table, for every field
		// selected by the redirecting SELCET
		boolean assumption2 = vc.corresponsAllFns(input_query.getTableName(), input_query.getSelectedFieldNames());

		// redirecting SELECTS's where clause has only = (and not other comparison
		// binary operations)
		boolean assumption3 = input_query.getWHC().hasOnlyEq();

		// if the redirecting SELECT is a range query on T2 (which may be replaced with
		// a single-row select on T1), we have to make sure that no project is called on
		// it's variable which accesses k'th row where k>1
		Transaction txn = pu.getTrasnsactionMap().get(txnName);
		boolean assumption4 = true;
		if (get_redirection_type() == Redirection_Type.T2_TO_T1 && vc.getType() == VC_Type.VC_OTM) {
			for (E_Proj exp : txn.getAllProjExps()) {
				assumption4 &= (!exp.getOrderExp().equals(new E_Const_Num(1)));
				logger.debug("E_Proj " + exp + " = 1?  " + assumption4);
			}
		}
		return assumption1 && assumption2 && assumption3 && assumption4;
	}

	private Redirection_Type get_redirection_type() {
		if (this.vc.getTableName(1).equals(this.sourceTable.getTableName()))
			return Redirection_Type.T1_TO_T2;
		else
			return Redirection_Type.T2_TO_T1;
	}

	private ArrayList<FieldName> updateFNs(TableName old_table, ArrayList<FieldName> old_fns) {
		ArrayList<FieldName> result = new ArrayList<>();
		for (FieldName old_fn : old_fns)
			result.add(vc.getCorrespondingFN(old_fn));
		return result;
	}

	private WHC updateWHC(WHC old_whc) {
		ArrayList<WHC_Constraint> new_whccs = new ArrayList<>();
		for (WHC_Constraint old_whcc : old_whc.getConstraints())
			if (!old_whcc.isAliveConstraint())
				new_whccs.add(updateWHCC(old_whcc));
		WHC new_whc = new WHC(targetTable.getIsAliveFN(), new_whccs);
		return new_whc;
	}

	private WHC_Constraint updateWHCC(WHC_Constraint old_whcc) {
		WHC_Constraint result = null;
		switch (vc.getType()) {
		case VC_OTO:
			result = new WHC_Constraint(targetTable.getTableName(), vc.getCorrespondingKey(old_whcc.getFieldName()),
					old_whcc.getOp(), old_whcc.getExpression());
			break;
		case VC_OTM:
			switch (vc.get_agg()) {
			case VC_ID:
				result = new WHC_Constraint(targetTable.getTableName(), vc.getCorrespondingKey(old_whcc.getFieldName()),
						old_whcc.getOp(), old_whcc.getExpression());
				break;
			case VC_SUM:
				result = new WHC_Constraint(targetTable.getTableName(), vc.getCorrespondingKey(old_whcc.getFieldName()),
						old_whcc.getOp(), old_whcc.getExpression());
				break;
			default:
				assert (false) : "unhandled agg function";
			}
			break;
		}
		return result;
	}

	private Variable updateVar(Variable old_var) {
		return pu.mkVariable(targetTable.getTableName().getName(), txnName);
	}

}
