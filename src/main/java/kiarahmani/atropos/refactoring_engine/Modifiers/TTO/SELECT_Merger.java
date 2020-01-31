/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.Modifiers.TTO;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.refactoring_engine.Modifiers.QM_Type;
import kiarahmani.atropos.utils.Program_Utils;
import kiarahmani.atropos.utils.Tuple;

/**
 * @author Kiarash
 *
 */
public class SELECT_Merger extends Two_to_One_Query_Modifier {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private Program_Utils pu;
	private String txnName;
	private Select_Query old_select1;
	private Select_Query old_select2;
	private Variable old_var1, old_var2, new_var;

	public SELECT_Merger() {
		super.type = QM_Type.TTO;
	}

	public void set(Program_Utils pu, String txnName) {
		logger.debug("setting the SELECT_Merger");
		this.pu = pu;
		this.txnName = txnName;
		super.set();
	}

	/*
	 * 
	 */
	@Override
	public Query atIndexModification(Query input_query_1, Query input_query_2) {
		old_select1 = (Select_Query) input_query_1;
		old_select2 = (Select_Query) input_query_2;
		old_var1 = old_select1.getVariable();
		old_var2 = old_select2.getVariable();
		logger.debug("whc1: " + old_select1.getWHC());
		logger.debug("whc2: " + old_select2.getWHC());

		WHC new_whc = mergeWHCs(old_select1, old_select2);
		assert (new_whc != null && modificationIsValid()) : "requested modification cannot be done on: "
				+ input_query_1.getId() + " and " + input_query_2.getId();
		Table old_table = pu.getTable(old_select1.getTableName().getName());
		new_var = pu.mkVariable(old_table.getTableName().getName(), txnName);
		ArrayList<FieldName> new_fns = new ArrayList<>();
		for (FieldName fn : old_select1.getSelectedFieldNames())
			new_fns.add(fn);
		for (FieldName fn : old_select2.getSelectedFieldNames())
			new_fns.add(fn);
		Select_Query new_select = new Select_Query(-1, pu.getNewSelectId(txnName), old_select1.isAtomic(),
				old_table.getTableName(), new_fns, new_var, new_whc);
		return new_select;
	}

	private boolean modificationIsValid() {
		// TODO
		return true;
	}

	WHC mergeWHCs(Select_Query qry_1, Select_Query qry_2) {
		boolean contains1 = qry_1.getWHC().containsWHC(qry_2.getWHC());
		boolean contains2 = qry_2.getWHC().containsWHC(qry_1.getWHC());
		if (contains1 && contains2)
			return qry_1.getWHC();
		else {
			logger.debug("where clauses are not equal, however, "
					+ "still must check if whc2 refers to the row selected by whc1");
			if (indirectSameRecordProperty(qry_1, qry_2))
				return qry_1.getWHC();
			else
				return null;
		}
	}

	private boolean indirectSameRecordProperty(Select_Query qry1, Select_Query qry2) {
		logger.debug("Checking if the indirect same record relationship holds between " + qry1.getId() + " and "
				+ qry2.getId());
		Variable var1 = qry1.getVariable();
		WHC whc2 = qry2.getWHC();
		boolean whc2_format = true;
		ArrayList<FieldName> pot_keys = new ArrayList<>();
		// this is a property of whc2
		for (WHC_Constraint whcc : whc2.getConstraints())
			if (!whcc.isAliveConstraint()) {
				whc2_format &= whcc.getOp() == BinOp.EQ;
				whc2_format &= isAProjOnFn(whcc.getExpression(), whcc.getFieldName(), var1);
				pot_keys.add(whcc.getFieldName());
			}
		logger.debug("Making sure all constraints in where clause of query2 have proper format: " + whc2_format);
		logger.debug("The potential fields that may be uniquely identifying records: " + pot_keys);
		// this is a property of whc2 and the table
		boolean key_format = pu.checkPotKeyForFns(qry1.getTableName(), pot_keys, qry2.getSelectedFieldNames());
		logger.debug("Making sure potential fields are in fact uniquely identifying records: " + key_format);
		return whc2_format && key_format;
	}

	private boolean isAProjOnFn(Expression exp, FieldName fn, Variable var) {
		logger.debug("Checking if " + exp + " is an instance of proj("+fn+") on " + var + " or not");
		if (exp instanceof E_Proj) {
			E_Proj proj_exp = (E_Proj) exp;
			if (proj_exp.v.equals(var) && proj_exp.e.isEqual(new E_Const_Num(1)) && proj_exp.f.equals(fn))
				return true;
		}
		return false;
	}

	@Override
	public Expression propagatedExpModification(Expression input_exp) {
		for (FieldName fn : old_select1.getSelectedFieldNames())
			input_exp.redirectProjs(old_var1, fn, new_var, fn);
		for (FieldName fn : old_select2.getSelectedFieldNames())
			input_exp.redirectProjs(old_var2, fn, new_var, fn);
		return input_exp;
	}

	@Override
	public Query_Statement propagatedQueryModification(Query_Statement input_qry_stmt) {
		for (FieldName fn : old_select1.getSelectedFieldNames())
			input_qry_stmt.getQuery().redirectProjs(old_var1, fn, new_var, fn);
		for (FieldName fn : old_select2.getSelectedFieldNames())
			input_qry_stmt.getQuery().redirectProjs(old_var2, fn, new_var, fn);
		return input_qry_stmt;
	}

}
