/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.deltas.Modifiers;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DDL.vc.VC;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.utils.Program_Utils;

/**
 * @author Kiarash
 *
 */
public class Query_Redirector extends Query_Modifier {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private Program_Utils pu;
	private Table targetTable;
	private Table sourceTable;
	private String txnName;
	private VC vc;
	private Redirection_Type type;

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
		Select_Query old_select = (Select_Query) input_query;
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
		boolean new_isAtomic = pu.whcIsAtomic(new_whc);
		logger.debug("new is atomic: " + new_isAtomic);
		Variable new_var = updateVar(old_var);
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

	private ArrayList<FieldName> updateFNs(TableName old_table, ArrayList<FieldName> old_fns) {
		// TODO
		return old_fns;
	}

	private WHC updateWHC(WHC old_whc) {
		// TODO
		return old_whc;
	}

	private Variable updateVar(Variable old_var) {
		// TODO
		return old_var;
	}

	private boolean modificationIsValid(Select_Query input_query, VC vc) {
		// TODO
		return true;
	}

	private Redirection_Type get_redirection_type() {
		if (this.vc.getTableName(1).equals(this.sourceTable.getTableName()))
			return Redirection_Type.T1_TO_T2;
		else
			return Redirection_Type.T2_TO_T1;
	}

	/*
	 * This function will perform the variable substitution(non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.refactoring_engine.deltas.Modifiers.Query_Modifier#
	 * modify_propagate(kiarahmani.atropos.program.Statement)
	 */
	@Override
	public Expression propagatedExpModification(Expression input_exp) {
		// TODO Auto-generated method stub
		return input_exp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.refactoring_engine.deltas.Modifiers.Query_Modifier#
	 * propagatedQueryModification(kiarahmani.atropos.program.statements.
	 * Query_Statement)
	 */
	@Override
	public Query_Statement propagatedQueryModification(Query_Statement input_exp) {
		// TODO Auto-generated method stub
		return input_exp;
	}

}
