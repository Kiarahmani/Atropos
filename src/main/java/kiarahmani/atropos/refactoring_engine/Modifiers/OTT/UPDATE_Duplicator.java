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
public class UPDATE_Duplicator extends One_to_Two_Query_Modifier {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private Program_Utils pu;
	private String txnName;
	private Update_Query old_update;
	private String sourceTableName;
	private String targetTableName;

	/*
	 * Set the stage before modifying. Function set must be called before calling
	 * modify each time.
	 */
	public void set(Program_Utils pu, String txnName, String sourceTableName, String targetTableName) {
		logger.debug("setting the UPDATE_Splitter");
		this.pu = pu;
		this.txnName = txnName;
		this.sourceTableName = sourceTableName;
		this.targetTableName = targetTableName;
		super.set();
	}

	public Tuple<Query, Query> atIndexModification(Query input_query) {
		// extract old query's details
		old_update = (Update_Query) input_query;
		assert (modificationIsValid()) : "requested modification cannot be done on: " + input_query;
		// TODO
		Update_Query new_update_1 = null;
		Update_Query new_update_2 = null;

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

	private boolean modificationIsValid() {
		// TODO
		boolean result = true;
		logger.debug("modificationIsValid returned result: " + result);
		return result;
	}

}
