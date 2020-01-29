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
	private Update_Query old_update1;
	private Update_Query old_update2;

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
	 * 
	 * @see
	 * kiarahmani.atropos.refactoring_engine.Modifiers.TTO.Two_to_One_Query_Modifier
	 * #atIndexModification(kiarahmani.atropos.DML.query.Query,
	 * kiarahmani.atropos.DML.query.Query)
	 */
	@Override
	public Query atIndexModification(Query input_query_1, Query input_query_2) {
		old_update1 = (Update_Query) input_query_1;
		old_update2 = (Update_Query) input_query_2;
		Table old_table = pu.getTable(old_update1.getTableName().getName()); // this is stupid XXX
		WHC new_whc = mergeWHCs(old_update1.getWHC(), old_update2.getWHC());
		assert (new_whc != null && modificationIsValid()) : "requested modification cannot be done on: "
				+ input_query_1.getId() + " and " + input_query_2.getId();
		Update_Query new_update = new Update_Query(-1, pu.getNewUpdateId(txnName),
				new_whc.isAtomic(old_table.getShardKey()), old_table.getTableName(), new_whc);
		for (Tuple<FieldName, Expression> fe : old_update1.getUpdateExps())
			new_update.addUpdateExp(fe.x, fe.y);
		for (Tuple<FieldName, Expression> fe : old_update2.getUpdateExps())
			new_update.addUpdateExp(fe.x, fe.y);

		return new_update;
	}

	private boolean modificationIsValid() {
		// TODO
		return true;
	}

	WHC mergeWHCs(WHC whc1, WHC whc2) {
		return whc2;
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

}
