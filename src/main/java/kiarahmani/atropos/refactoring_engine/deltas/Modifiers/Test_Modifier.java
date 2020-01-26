/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.deltas.Modifiers;

import kiarahmani.atropos.DML.expression.Expression;
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
	 * propagatedExpModification(kiarahmani.atropos.DML.expression.Expression)
	 */
	@Override
	public Expression propagatedExpModification(Expression input_exp) {
		return input_exp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.refactoring_engine.deltas.Modifiers.Query_Modifier#
	 * propagatedQueryModification(kiarahmani.atropos.program.statements.
	 * Query_Statement)
	 */
	@Override
	public Query_Statement propagatedQueryModification(Query_Statement input_exp) {
		Query_Statement q_stmt = pu.mkTestQryStmt_6(txnName);
		return q_stmt;
	}

}
