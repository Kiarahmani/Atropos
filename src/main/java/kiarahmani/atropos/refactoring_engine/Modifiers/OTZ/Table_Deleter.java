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
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.utils.Program_Utils;

/**
 * @author Kiarash
 *
 */
public class Table_Deleter extends One_to_Zero_Query_Modifier {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private Program_Utils pu;
	private String txnName;
	private Table deleted_table;

	public Table getDeletedTable() {
		return this.deleted_table;
	}

	public Table_Deleter(Table deleted_table) {
		this.deleted_table = deleted_table;
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
