/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.deltas.Modifiers;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.utils.Program_Utils;

/**
 * @author Kiarash
 *
 */
public class Query_Redirector extends Query_Modifier {

	Program_Utils pu;
	Table targetTable;
	String txnName;

	// set the stage before modifying
	// set must be called before calling modify each time
	public void set(Program_Utils pu, String txnName, String targetTableName) {
		this.pu = pu;
		this.txnName = txnName;
		this.targetTable = pu.getTable(targetTableName);
		super.set();
	}

	public Query atIndexModification(Query input_query) {
		Select_Query input_select = (Select_Query) input_query;
		Variable v = input_select.getVariable();
		WHC GetAccount0_WHC = new WHC(pu.getIsAliveFieldName("accounts"), new WHC_Constraint(
				pu.getTableName("accounts"), pu.getFieldName("a_custid"), BinOp.EQ, new E_Const_Num(69)));
		ArrayList<FieldName> fns = new ArrayList<>();
		fns.add(pu.getFieldName("a_custid"));
		Select_Query q = new Select_Query(-1, pu.getNewSelectId(txnName), pu.whcIsAtomic(GetAccount0_WHC),
				pu.getTableName("accounts"), fns, v, GetAccount0_WHC);
		return q;
	}

	/*
	 * This function will perform the variable substitution(non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.refactoring_engine.deltas.Modifiers.Query_Modifier#
	 * modify_propagate(kiarahmani.atropos.program.Statement)
	 */
	@Override
	public Statement propagatedModification(Statement input_stmt) {
		// TODO Auto-generated method stub
		return input_stmt;
	}

}
