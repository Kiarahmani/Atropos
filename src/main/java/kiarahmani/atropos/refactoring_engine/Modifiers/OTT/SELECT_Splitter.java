/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.Modifiers.OTT;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.utils.Program_Utils;
import kiarahmani.atropos.utils.Tuple;

/**
 * @author Kiarash
 *
 */
public class SELECT_Splitter extends One_to_Two_Query_Modifier {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private Program_Utils pu;
	private String txnName;
	private Select_Query old_select;
	private ArrayList<FieldName> excluded_fns;
	private ArrayList<FieldName> old_selected_fieldNames;
	Variable new_var_1;
	Variable new_var_2;
	Variable old_var;

	/*
	 * Set the stage before modifying. Function set must be called before calling
	 * modify each time.
	 */
	public void set(Program_Utils pu, String txnName, ArrayList<FieldName> excluded_fns) {
		logger.debug("setting the SELECT_Splitter");
		this.pu = pu;
		this.txnName = txnName;
		this.excluded_fns = excluded_fns;
		super.set();
	}

	public Tuple<Query, Query> atIndexModification(Query input_query) {
		// extract old query's details
		old_select = (Select_Query) input_query;
		logger.debug("SELECT to be separated: " + old_select);
		boolean old_is_atomic = old_select.isAtomic();
		TableName old_tableName = old_select.getTableName();
		old_selected_fieldNames = old_select.getSelectedFieldNames();
		old_var = old_select.getVariable();
		WHC old_whc = old_select.getWHC();
		// make sure the redirection is possible
		assert (isValid(input_query)) : "requested modification cannot be done";
		//
		logger.debug("Original Set of FieldNames: " + old_selected_fieldNames);
		logger.debug("Excluded Set of FieldNames: " + excluded_fns);
		old_selected_fieldNames.removeAll(excluded_fns);
		logger.debug("Remaining Set of FieldNames: " + old_selected_fieldNames);
		new_var_1 = pu.mkVariable(old_tableName.getName(), txnName);
		Select_Query new_select_1 = new Select_Query(-1, pu.getNewSelectId(txnName), old_is_atomic, old_tableName,
				old_selected_fieldNames, new_var_1, old_whc);
		logger.debug("New SELECT (1): " + new_select_1);
		new_var_2 = pu.mkVariable(old_tableName.getName(), txnName);
		Select_Query new_select_2 = new Select_Query(-1, pu.getNewSelectId(txnName), old_is_atomic, old_tableName,
				excluded_fns, new_var_2, old_whc);
		logger.debug("New SELECT (2): " + new_select_2);
		// return
		return new Tuple<Query, Query>(new_select_1, new_select_2);
	}

	@Override
	public Expression propagatedExpModification(Expression input_exp) {
		logger.debug(
				"For all unchanged field names " + old_selected_fieldNames + " now substituting proj expressions from "
						+ old_var + " to " + new_var_1 + " in all subsequent if conditions");
		for (FieldName fn : old_selected_fieldNames)
			input_exp.redirectProjs(old_var, fn, new_var_1, fn);
		logger.debug("For all separated field names " + excluded_fns + " now substituting proj expressions from "
				+ old_var + " to " + new_var_2 + " in all subsequent if conditions");
		for (FieldName fn : excluded_fns)
			input_exp.redirectProjs(old_var, fn, new_var_2, fn);
		return input_exp;
	}

	@Override
	public Query_Statement propagatedQueryModification(Query_Statement input_qry_stmt) {
		logger.debug(
				"For all unchanged field names " + old_selected_fieldNames + " now substituting proj expressions from "
						+ old_var + " to " + new_var_1 + " in all subsequent queries");
		for (FieldName fn : old_selected_fieldNames)
			input_qry_stmt.getQuery().redirectProjs(old_var, fn, new_var_1, fn);
		logger.debug("For all separated field names " + excluded_fns + " now substituting proj expressions from "
				+ old_var + " to " + new_var_2 + " in all subsequent queries");
		for (FieldName fn : excluded_fns)
			input_qry_stmt.getQuery().redirectProjs(old_var, fn, new_var_2, fn);
		return input_qry_stmt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * kiarahmani.atropos.refactoring_engine.Modifiers.OTT.One_to_Two_Query_Modifier
	 * #isValid(kiarahmani.atropos.DML.query.Query)
	 */
	@Override
	public boolean isValid(Query input_query) {
		Select_Query input_select = null;
		if (input_query instanceof Select_Query) {
			input_select = (Select_Query) input_query;
		} else
			return false;

		boolean result = input_select.getSelectedFieldNames().containsAll(excluded_fns);
		logger.debug("old_selected_fieldNames must include all excluded_fns: " + result);
		logger.debug("old_selected_fieldNames: " + input_select.getSelectedFieldNames());
		logger.debug("excluded_fns: " + excluded_fns);
		return result;
	}

}
