/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.Modifiers.OTZ;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.program.Block;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.utils.Program_Utils;

/**
 * @author Kiarash
 *
 */
public class Query_Deleter extends One_to_Zero_Query_Modifier {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private Program_Utils pu;
	private String txnName;
	private Query deleted_query;
	private int original_po;
	private Block originalBlock;

	public String getTxnName() {
		return txnName;
	}

	public Block getOriginalBlock() {
		return originalBlock;
	}

	public Query getDeletedQuery() {
		return this.deleted_query;
	}

	public int getOriginalPo() {
		return this.original_po;
	}

	public Query_Deleter(Query deleted_query, int original_po, String txnName, Block originalBlock) {
		this.deleted_query = deleted_query;
		this.original_po = original_po;
		this.txnName = txnName;
		this.originalBlock = originalBlock;
	}

	@Override
	public Expression propagatedExpModification(Expression input_exp) {
		return input_exp;
	}

	@Override
	public Query_Statement propagatedQueryModification(Query_Statement input_exp) {
		return input_exp;
	}

}
