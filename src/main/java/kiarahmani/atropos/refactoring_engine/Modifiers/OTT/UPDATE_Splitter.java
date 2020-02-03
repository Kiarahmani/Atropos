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
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.utils.Program_Utils;
import kiarahmani.atropos.utils.Tuple;

/**
 * @author Kiarash
 *
 */
public class UPDATE_Splitter extends One_to_Two_Query_Modifier {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private Program_Utils pu;
	private String txnName;
	private Update_Query old_update;
	private ArrayList<FieldName> excluded_fns;
	private ArrayList<Tuple<FieldName, Expression>> old_update_exps;
	private ArrayList<Tuple<FieldName, Expression>> excluded_update_exps;

	/*
	 * Set the stage before modifying. Function set must be called before calling
	 * modify each time.
	 */
	public void set(Program_Utils pu, String txnName, ArrayList<FieldName> excluded_fns) {
		logger.debug("setting the UPDATE_Splitter");
		this.pu = pu;
		this.txnName = txnName;
		this.excluded_fns = excluded_fns;
		super.set();
	}

	public Tuple<Query, Query> atIndexModification(Query input_query) {
		// extract old query's details
		old_update = (Update_Query) input_query;
		boolean old_is_atomic = old_update.isAtomic();
		TableName old_table_name = old_update.getTableName();
		old_update_exps = old_update.getUpdateExps();
		logger.debug("original update expressions: " + old_update_exps);
		assert (isValid(old_update)) : "requested modification cannot be done on: " + input_query;
		// define and populate a new array list of update expressions for the new query
		excluded_update_exps = new ArrayList<>();
		for (Tuple<FieldName, Expression> fe : old_update_exps)
			if (excluded_fns.contains(fe.x))
				excluded_update_exps.add(fe);
		logger.debug("excluded update expressions: " + excluded_update_exps);
		old_update_exps.removeAll(excluded_update_exps);
		logger.debug("remaining update expressions: " + old_update_exps);
		Update_Query new_update_1 = new Update_Query(-1, pu.getNewUpdateId(txnName), old_is_atomic, old_table_name,
				old_update.getWHC());
		for (Tuple<FieldName, Expression> fe : old_update_exps)
			new_update_1.addUpdateExp(fe.x, fe.y);
		logger.debug("final query #1: " + new_update_1);
		Update_Query new_update_2 = new Update_Query(-1, pu.getNewUpdateId(txnName), old_is_atomic, old_table_name,
				old_update.getWHC());
		for (Tuple<FieldName, Expression> fe : excluded_update_exps)
			new_update_2.addUpdateExp(fe.x, fe.y);
		logger.debug("final query #2: " + new_update_2);
		this.desc = "Old query (" + input_query.getId() + ") in " + txnName + " is splitted into queries ("
				+ new_update_1.getId() + ") and (" + new_update_2.getId() + ")";
		return new Tuple<Query, Query>(new_update_1, new_update_2);
	}

	@Override
	public Expression propagatedExpModification(Expression input_exp) {
		logger.debug("No propagated modification is necessary");
		return input_exp;
	}

	@Override
	public Query_Statement propagatedQueryModification(Query_Statement input_qry_stmt) {
		logger.debug("No propagated modification is necessary");
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
		Update_Query input_update = null;
		if (input_query instanceof Update_Query) {
			input_update = (Update_Query) input_query;
		} else
			return false;

		logger.debug("set of updated fields " + input_update.getWrittenFieldNames()
				+ " must include all excluded field names " + excluded_fns + "");
		boolean result = input_update.getWrittenFieldNames().containsAll(excluded_fns);
		logger.debug("modificationIsValid returned result: " + result);
		return result;
	}

}
