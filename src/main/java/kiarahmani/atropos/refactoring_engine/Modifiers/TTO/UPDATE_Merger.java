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
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.refactoring_engine.Modifiers.QM_Type;
import kiarahmani.atropos.utils.Program_Utils;
import kiarahmani.atropos.utils.Tuple;

/**
 * @author Kiarash
 *
 */
public class UPDATE_Merger extends Two_to_One_Query_Modifier {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private Program_Utils pu;
	private String txnName;
	public Update_Query getOld_update1() {
		return old_update1;
	}

	public void setOld_update1(Update_Query old_update1) {
		this.old_update1 = old_update1;
	}

	public Update_Query getOld_update2() {
		return old_update2;
	}

	public void setOld_update2(Update_Query old_update2) {
		this.old_update2 = old_update2;
	}

	private Update_Query old_update1;
	private Update_Query old_update2;
	private int original_applied_po;

	public int getOriginal_applied_po() {
		return original_applied_po;
	}

	public void setOriginal_applied_po(int original_applied_po) {
		this.original_applied_po = original_applied_po;
	}

	public String getTxnName() {
		return txnName;
	}

	public UPDATE_Merger() {
		super.type = QM_Type.TTO;
	}

	public void set(Program_Utils pu, String txnName) {
		logger.debug("setting the UPDATE_Merger");
		this.pu = pu;
		this.txnName = txnName;
		super.set();
	}

	/*
	 * (non-Javadoc)
	 */
	@Override
	public Query atIndexModification(Query input_query_1, Query input_query_2) {
		old_update1 = (Update_Query) input_query_1;
		old_update2 = (Update_Query) input_query_2;
		logger.debug("whc1: " + old_update1.getWHC());
		logger.debug("whc2: " + old_update2.getWHC());
		Table old_table = pu.getTable(old_update1.getTableName().getName());
		WHC new_whc = mergeWHCs(old_update1.getWHC(), old_update2.getWHC());
		assert (new_whc != null && isValid(input_query_1, input_query_2)) : "requested modification cannot be done on: "
				+ input_query_1.getId() + " and " + input_query_2.getId();
		Update_Query new_update = new Update_Query(-1, pu.getNewUpdateId(txnName),
				new_whc.isAtomic(old_table.getShardKey()), old_table.getTableName(), new_whc);
		for (Tuple<FieldName, Expression> fe : old_update1.getUpdateExps())
			new_update.addUpdateExp(fe.x, fe.y);
		for (Tuple<FieldName, Expression> fe : old_update2.getUpdateExps())
			new_update.addUpdateExp(fe.x, fe.y);
		this.desc = "Old queries (" + input_query_1.getId() + ") and (" + input_query_2.getId()
				+ ") are merged into query (" + new_update.getId() + ")";
		return new_update;
	}

	WHC mergeWHCs(WHC whc1, WHC whc2) {
		logger.debug("Does whc1 contain whc2?");
		boolean contains1 = whc1.containsWHC(whc2);
		boolean contains2 = whc2.containsWHC(whc1);
		if (contains1 && contains2)
			return whc1;
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.refactoring_engine.Modifiers.Query_Modifier#
	 * propagatedExpModification(kiarahmani.atropos.DML.expression.Expression)
	 */
	@Override
	public Expression propagatedExpModification(Expression input_exp) {
		logger.debug("No propagated modification is necessary");
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
		logger.debug("No propagated modification is necessary");
		return input_qry_stmt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * kiarahmani.atropos.refactoring_engine.Modifiers.TTO.Two_to_One_Query_Modifier
	 * #isValid(kiarahmani.atropos.DML.query.Query,
	 * kiarahmani.atropos.DML.query.Query)
	 */
	@Override
	public boolean isValid(Query input_query_1, Query input_query_2) {
		Update_Query input_update_1 = null;
		if (input_query_1 instanceof Update_Query) {
			input_update_1 = (Update_Query) input_query_1;
		} else
			return false;
		Update_Query input_update_2 = null;
		if (input_query_2 instanceof Update_Query) {
			input_update_2 = (Update_Query) input_query_2;
		} else
			return false;

		boolean valid1 = input_update_1.getTableName().equalsWith(input_update_2.getTableName());
		logger.debug("valid1: " + valid1);
		boolean valid2 = input_update_1.isAtomic() == input_update_2.isAtomic();
		logger.debug("valid2: " + valid2);
		boolean valid3 = input_update_1.getPo() == input_update_2.getPo() - 1;
		logger.debug("valid3: " + valid3);

		return valid1 && valid2 && valid3;
	}

}
