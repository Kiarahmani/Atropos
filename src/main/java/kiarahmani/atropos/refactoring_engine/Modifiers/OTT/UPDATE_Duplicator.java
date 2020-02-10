/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.Modifiers.OTT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.z3.BitVecNum;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.vc.VC;
import kiarahmani.atropos.DDL.vc.VC.VC_Agg;
import kiarahmani.atropos.DDL.vc.VC.VC_Type;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_BinOp;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.E_UUID;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.query.Insert_Query;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Select_Query;
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
	private int original_duplicated_po;

	public int getOrgDupPo() {
		return this.original_duplicated_po;
	}

	public void setOrgDupPo(int po) {
		this.original_duplicated_po = po;
	}

	private enum Redirection_Type {
		T1_TO_T2, T2_TO_T1;
	}

	public String getTxnName() {
		return this.txnName;
	}

	/*
	 * Set the stage before modifying. Function set must be called before calling
	 * modify each time.
	 */
	public void set(Program_Utils pu, String txnName, String sourceTableName, String targetTableName) {
		logger.debug("setting the UPDATE_Duplicator from " + sourceTableName + " to " + targetTableName);
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
		WHC old_whc = old_update.getWHC();
		ArrayList<Tuple<FieldName, Expression>> old_ue = old_update.getUpdateExps();
		logger.debug("Query to be duplicated: " + old_update.getPo());
		// make sure modification is valid
		assert (isValid(input_query)) : "requested modification cannot be done on: " + input_query;

		Query new_qry = null;
		// handle the CRDT case
		if (vc.getType() == VC_Type.VC_OTM && vc.get_agg() == VC_Agg.VC_SUM) {
			WHC_Constraint[] whcc_array = mkInsert(old_update);
			new_qry = new Insert_Query(-1, pu.getNewUpdateId(txnName), targetTable, targetTable.getIsAliveFN());
			((Insert_Query) new_qry).addPKExp(whcc_array);
			for (WHC_Constraint pk : whcc_array)
				((Insert_Query) new_qry).addInsertExp(pk.getFieldName(), pk.getExpression());
			new_qry.setcanBeRemoved(false);

		} else {// handle other (non-CRDT) cases
			// generate new query's components
			WHC new_whc = updateWHC(old_whc);
			logger.debug("where clause of the duplicated query: " + new_whc);
			ArrayList<Tuple<FieldName, Expression>> new_ue = updateUE(old_ue);
			logger.debug("update expressions of the duplicated query: " + new_ue);
			boolean new_isAtomic = new_whc.isAtomic(targetTable.getShardKey());
			logger.debug("duplicated query is atomic? " + new_isAtomic);
			new_qry = new Update_Query(-1, pu.getNewUpdateId(txnName), new_isAtomic, targetTable.getTableName(),
					new_whc);
			for (Tuple<FieldName, Expression> fe : new_ue)
				((Update_Query) new_qry).addUpdateExp(fe.x, fe.y);
		}
		new_qry.setPathCondition(old_update.getPathCondition());
		this.desc = "Old query (" + input_query.getId() + ") in " + txnName + " is duplicated in query("
				+ new_qry.getId() + ")";
		return new Tuple<Query, Query>(input_query, new_qry);
	}

	/**
	 * @return
	 */
	private WHC_Constraint[] mkInsert(Update_Query old_update) {
		if (!targetTable.isCrdt())
			return null;
		List<FieldName> pk_fields = sourceTable.getPKFields();
		logger.debug("PK fields of the source table: " + pk_fields);
		int pk_fields_size = pk_fields.size();
		WHC_Constraint[] result = new WHC_Constraint[pk_fields_size + 2];
		// set pk fields
		int index = 0;
		logger.debug("For each PK a new expression will be added to the beginning of the ISNERT:");
		for (FieldName pk_fn : pk_fields) {
			result[index] = new WHC_Constraint(targetTable.getTableName(), targetTable.getPKFields().get(index),
					BinOp.EQ, old_update.getWHC().getConstraintByFieldName(pk_fn).getExpression());
			index++;
		}
		logger.debug("Insert Exps after adding PKs: " + Arrays.toString(result));
		// set uuid field
		result[pk_fields_size] = new WHC_Constraint(targetTable.getTableName(), targetTable.getUUIDField(), BinOp.EQ,
				new E_UUID());
		logger.debug("Insert Exps after adding UUID: " + Arrays.toString(result));
		// set delta field
		Expression delta_exp = extractDeltaExp(old_update);
		if (delta_exp == null)
			return null;
		result[pk_fields_size + 1] = new WHC_Constraint(targetTable.getTableName(), targetTable.getDeltaField(),
				BinOp.EQ, delta_exp);
		logger.debug("Final insert Exps: " + Arrays.toString(result));
		return result;
	}

	private Expression extractDeltaExp(Update_Query old_update) {
		Expression result = null;
		ArrayList<Tuple<FieldName, Expression>> old_exps = old_update.getUpdateExps();
		assert (old_exps
				.size() == 1) : "assumption failed: CRDT duplication can only be called on single field updates";
		logger.debug("Extracting delta from: " + old_exps);
		FieldName fn = old_exps.get(0).x;
		Expression exp = old_exps.get(0).y;
		if (exp instanceof E_BinOp) {
			// TODO: Generalize CRDT tables and supported operations
			E_BinOp bin_exp = (E_BinOp) exp;
			if (bin_exp.op == BinOp.PLUS)
				if (bin_exp.oper1 instanceof E_Proj) {
					result = bin_exp.oper2;
				}
			if (bin_exp.op == BinOp.MINUS)
				if (bin_exp.oper1 instanceof E_Proj) {
					result = new E_BinOp(BinOp.MULT, new E_Const_Num(-1), bin_exp.oper2);
				}
		}
		logger.debug("Final extracted delta: " + result);
		return result;
	}

	private ArrayList<Tuple<FieldName, Expression>> updateUE(ArrayList<Tuple<FieldName, Expression>> old_ue) {
		ArrayList<Tuple<FieldName, Expression>> result = new ArrayList<>();
		for (Tuple<FieldName, Expression> fe : old_ue)
			result.add(new Tuple<FieldName, Expression>(vc.getCorrespondingFN(fe.x), fe.y));
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
				assert (false) : "unexpected state";
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

	private Redirection_Type get_redirection_type() {
		if (this.vc.getTableName(1).equals(this.sourceTable.getTableName()))
			return Redirection_Type.T1_TO_T2;
		else
			return Redirection_Type.T2_TO_T1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * kiarahmani.atropos.refactoring_engine.Modifiers.OTT.One_to_Two_Query_Modifier
	 * #isValid(kiarahmani.atropos.DML.query.Query)
	 */
	@Override
	public boolean isValid(Query input_query) {
		Update_Query input_update = null;
		if (input_query instanceof Update_Query) {
			input_update = (Update_Query) input_query;
		} else
			return false;

		boolean assumption0 = !(vc.getType() == VC_Type.VC_OTM && vc.get_agg() == VC_Agg.VC_SUM)
				|| (mkInsert(input_update) != null);

		// all keys used as the WHC of duplicating UPDATE must be constrained by vc
		boolean assumption1 = vc.containsWHC(input_update.getWHC());
		// there must be a correspondence from src table to target table, for every
		// field
		// updated by the duplicating UPDATE
		boolean assumption2 = vc.correspondsAllFns(input_update.getTableName(), input_update.getWrittenFieldNames());
		// redirecting UPDATE's where clause has only = (and not other comparison
		// binary operations)
		boolean assumption3 = input_update.getWHC().hasOnlyEq();

		boolean result = assumption0 && assumption1 && assumption2 && assumption3;
		logger.debug("modificationIsValid returned result: " + result);
		return result;
	}

}
