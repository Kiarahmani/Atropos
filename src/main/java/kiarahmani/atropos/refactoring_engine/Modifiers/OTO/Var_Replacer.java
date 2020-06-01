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
import kiarahmani.atropos.DDL.vc.VC.VC_Agg;
import kiarahmani.atropos.DDL.vc.VC.VC_Type;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.E_Size;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHCC;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.utils.Program_Utils;

/**
 * @author Kiarash
 *
 */
public class Var_Replacer extends One_to_One_Query_Modifier {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private Program_Utils pu;
	private String txnName;
	private ArrayList<FieldName> targeted_fns;
	private Variable oldVar, newVar;

	/*
	 * Set the stage before modifying. Function set must be called before calling
	 * modify each time.
	 */
	public void set(Program_Utils pu, String txnName, ArrayList<FieldName> targeted_fns, Variable oldVar,
			Variable newVar) {
		logger.debug("Setting the SELECT_Splitter");
		this.pu = pu;
		this.txnName = txnName;
		this.oldVar = oldVar;
		this.newVar = newVar;
		this.targeted_fns = targeted_fns;
		super.set();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * kiarahmani.atropos.refactoring_engine.Modifiers.OTO.One_to_One_Query_Modifier
	 * #atIndexModification(kiarahmani.atropos.DML.query.Query)
	 */
	@Override
	public Query atIndexModification(Query input_query) {
		logger.debug("no change is needed at the index");
		return input_query;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * kiarahmani.atropos.refactoring_engine.Modifiers.OTO.One_to_One_Query_Modifier
	 * #isValid(kiarahmani.atropos.DML.query.Query)
	 */
	@Override
	public boolean isValid(Query input_query) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.refactoring_engine.Modifiers.Query_Modifier#
	 * propagatedExpModification(kiarahmani.atropos.DML.expression.Expression)
	 */
	@Override
	public Expression propagatedExpModification(Expression input_exp) {
		for (FieldName fn : targeted_fns)
			input_exp.redirectProjs(oldVar, fn, newVar, fn);
		return input_exp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.refactoring_engine.Modifiers.Query_Modifier#
	 * propagatedQueryModification(kiarahmani.atropos.program.statements.
	 * Query_Statement)
	 */
	@Override
	public Query_Statement propagatedQueryModification(Query_Statement input_qry_stmt) {
		for (FieldName fn : targeted_fns)
			input_qry_stmt.getQuery().redirectProjs(oldVar, fn, newVar, fn);
		return input_qry_stmt;
	}

}
