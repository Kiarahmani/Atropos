/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.deltas.Modifiers;

import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.statements.If_Statement;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.utils.Program_Utils;

/**
 * @author Kiarash
 *
 */
public class Test_Modifier extends Query_Modifier {

	Program_Utils pu;
	String txnName;

	public void set(Program_Utils pu, String txnName) {
		this.pu = pu;
		this.txnName = txnName;
		super.set();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.refactoring_engine.deltas.Modifiers.Query_Modifier#
	 * atIndexModification(kiarahmani.atropos.DML.query.Query)
	 */
	@Override
	public Query atIndexModification(Query input_query) {
		Query_Statement q_stmt = pu.mkTestQryStmt(txnName);
		return q_stmt.getQuery();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.refactoring_engine.deltas.Modifiers.Query_Modifier#
	 * propagatedModification(kiarahmani.atropos.program.Statement)
	 */
	@Override
	public Statement propagatedModification(Statement input_stmt) {
		switch (input_stmt.getClass().getSimpleName()) {
		case "If_Statement":
			return input_stmt;
		case "Query_Statement":
			Query_Statement q_stmt = pu.mkTestQryStmt_6(txnName);
			return q_stmt;
		}
		return null;
	}

}
