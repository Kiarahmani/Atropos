/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.Modifiers.OTO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.utils.Program_Utils;

/**
 * @author Kiarash
 *
 */
public class Query_ReAtomicizer extends One_to_One_Query_Modifier {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private Program_Utils pu;
	private String txnName;

	/*
	 * Set the stage before modifying. Function set must be called before calling
	 * modify each time.
	 */
	public void set(Program_Utils pu, String txnName) {
		this.pu = pu;
		this.txnName = txnName;
		super.set();
	}

	public Query atIndexModification(Query input_query) {
		boolean old_is_atomic = input_query.isAtomic();
		boolean new_is_atomic = input_query.getWHC()
				.isAtomic(pu.getTable(input_query.getTableName().getName()).getShardKey());
		input_query.setAtomic(new_is_atomic);
		if (old_is_atomic != new_is_atomic)
			this.desc = "Atomicity of query (" + input_query.getId() + ") is updated from " + old_is_atomic + " to "
					+ new_is_atomic;
		else
			this.desc = "Atomicity of query (" + input_query.getId() + ") did not change";
		return input_query;
	}

	/*
	 * This function will perform the variable substitution(non-Javadoc)
	 */
	@Override
	public Expression propagatedExpModification(Expression input_exp) {
		return input_exp;
	}

	@Override
	public Query_Statement propagatedQueryModification(Query_Statement input_qry) {
		return input_qry;
	}

	@Override
	public boolean isValid(Query input_query) {
		return true;
	}

}
