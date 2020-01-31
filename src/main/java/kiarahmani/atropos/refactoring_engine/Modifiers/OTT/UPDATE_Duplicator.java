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
import kiarahmani.atropos.DDL.vc.VC;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.program.Table;
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
	private Table sourceTable;
	private Table targetTable;
	private VC vc;
	private Redirection_Type type;

	private enum Redirection_Type {
		T1_TO_T2, T2_TO_T1;
	}

	/*
	 * Set the stage before modifying. Function set must be called before calling
	 * modify each time.
	 */
	public void set(Program_Utils pu, String txnName, String sourceTableName, String targetTableName) {
		logger.debug("setting the UPDATE_Splitter");
		this.pu = pu;
		this.txnName = txnName;
		this.sourceTable = pu.getTable(sourceTableName);
		this.targetTable = pu.getTable(targetTableName);
		this.vc = pu.getVCByTables(sourceTable.getTableName(), targetTable.getTableName());
		this.type = get_redirection_type();
		super.set();
	}

	public Tuple<Query, Query> atIndexModification(Query input_query) {
		// extract old query's details
		old_update = (Update_Query) input_query;
		WHC old_whcc = old_update.getWHC();
		ArrayList<Tuple<FieldName, Expression>> old_ue = old_update.getUpdateExps();
		// make sure modification is valid
		assert (modificationIsValid()) : "requested modification cannot be done on: " + input_query;
		// generate new query's components
		WHC new_whc = updateWHC(old_whcc);
		ArrayList<Tuple<FieldName, Expression>> new_ue = updateUE(old_ue);
		boolean new_isAtomic = new_whc.isAtomic(targetTable.getShardKey());
		Update_Query new_update = new Update_Query(-1, pu.getNewUpdateId(txnName), new_isAtomic,
				targetTable.getTableName(), new_whc);
		for (Tuple<FieldName, Expression> fe : new_ue)
			new_update.addUpdateExp(fe.x, fe.y);
		return new Tuple<Query, Query>(input_query, new_update);
	}

	private ArrayList<Tuple<FieldName, Expression>> updateUE(ArrayList<Tuple<FieldName, Expression>> old_ue) {
		ArrayList<Tuple<FieldName, Expression>> result = new ArrayList<>();
		for (Tuple<FieldName, Expression> fe: old_ue)
			result.add(new Tuple<FieldName, Expression>(vc.getCorrespondingFN(fe.x),fe.y));
		return result;
	}

	private WHC updateWHC(WHC old_whc) {
		ArrayList<WHC_Constraint> new_whccs = new ArrayList<>();
		for (WHC_Constraint old_whcc : old_whc.getConstraints())
			if (!old_whcc.isAliveConstraint())
				new_whccs.add(updateWHCC(old_whcc));
		WHC new_whc = new WHC(targetTable.getIsAliveFN(), new_whccs);
		return new_whc;
	}

	private WHC_Constraint updateWHCC(WHC_Constraint old_whcc) {
		WHC_Constraint result = old_whcc;
		switch (vc.getType()) {
		case VC_OTO:
			result = new WHC_Constraint(targetTable.getTableName(), vc.getCorrespondingKey(old_whcc.getFieldName()),
					old_whcc.getOp(), old_whcc.getExpression());
			break;
		case VC_OTM:
			switch (vc.get_agg()) {
			case VC_ID:
				result = new WHC_Constraint(targetTable.getTableName(), vc.getCorrespondingKey(old_whcc.getFieldName()),
						old_whcc.getOp(), old_whcc.getExpression());
				break;
			case VC_SUM:
				assert (false) : "CRDT case is not implemented yet"; // TODO: implement CRDT case for update duplication
				break;
			default:
				assert (false) : "unhandled agg function";
			}
			break;
		}
		return result;
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

	private Redirection_Type get_redirection_type() {
		if (this.vc.getTableName(1).equals(this.sourceTable.getTableName()))
			return Redirection_Type.T1_TO_T2;
		else
			return Redirection_Type.T2_TO_T1;
	}

}
