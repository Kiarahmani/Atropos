/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.Modifiers.OTT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHCC;
import kiarahmani.atropos.program.Table;
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

	private int original_applied_po;

	public int getOriginal_applied_po() {
		return original_applied_po;
	}

	public void setOriginal_applied_po(int original_applied_po) {
		this.original_applied_po = original_applied_po;
	}

	public String getTxnName() {
		return txnName;
	}

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
		new_select_1.setPathCondition(old_select.getPathCondition());
		logger.debug("New SELECT (1): " + new_select_1);
		new_var_2 = pu.mkVariable(old_tableName.getName(), txnName);
		WHC new_whc = mk_old_whc_to_all_pk(old_whc, old_select);
		Select_Query new_select_2 = new Select_Query(-1, pu.getNewSelectId(txnName), old_is_atomic, old_tableName,
				excluded_fns, new_var_2, new_whc);
		new_select_2.setPathCondition(old_select.getPathCondition());
		logger.debug("New SELECT (2): " + new_select_2);
		this.desc = "Old query (" + input_query.getId() + ") in " + txnName + " is splitted into queries ("
				+ new_select_1.getId() + ") and (" + new_select_2.getId() + ")";
		// set the implicitly read fields for the new queries
		HashSet<FieldName> new_implicit_fns_1 = new HashSet<>();
		HashSet<FieldName> new_implicit_fns_2 = new HashSet<>();
		new_implicit_fns_1.addAll(old_select.getImplicitlyUsed());
		new_implicit_fns_2.addAll(old_select.getImplicitlyUsed());
		new_implicit_fns_1.removeAll(excluded_fns);
		new_implicit_fns_2.stream().filter(fn -> excluded_fns.contains(fn)).collect(Collectors.toSet());
		new_select_1.setImplicitlyUsed(new_implicit_fns_1);
		new_select_2.setImplicitlyUsed(new_implicit_fns_2);
		new_select_1.isImp = old_select.isImp;
		new_select_2.isImp = old_select.isImp;
		// return
		return new Tuple<Query, Query>(new_select_1, new_select_2);
	}

	private WHC mk_old_whc_to_all_pk(WHC old_whc, Select_Query old_select) {
		logger.debug("attempting to make " + old_whc + " to an all-pk whc");
		Table table = pu.getTable(old_select.getTableName());
		ArrayList<WHCC> whc_constraints = new ArrayList<>();
		for (FieldName pk : table.getPKFields()) {
			logger.debug("checking if pk " + pk + " is bound by the whc: ");
			if (old_whc.getConstraintByFieldName(pk) == null) {
				logger.debug("pk " + pk + " is not bound by whc. attempting to replace it with proj");
				whc_constraints.add(new WHCC(table.getTableName(), pk, BinOp.EQ,
						new E_Proj(old_select.getVariable(), pk, new E_Const_Num(1))));
			} else
				whc_constraints.add(old_whc.getConstraintByFieldName(pk));

		}
		return new WHC(table.getIsAliveFN(), whc_constraints);
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
