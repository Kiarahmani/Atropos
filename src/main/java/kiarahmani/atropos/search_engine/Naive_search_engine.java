/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.search_engine;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.vc.VC;
import kiarahmani.atropos.DDL.vc.VC.VC_Agg;
import kiarahmani.atropos.DDL.vc.VC.VC_Type;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_F;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_VC;
import kiarahmani.atropos.utils.Program_Utils;

/**
 * @author Kiarash
 *
 */
public class Naive_search_engine extends Search_engine {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private NameGenerator ng;
	private int iter;
	private boolean curr_is_crdt;

	public Naive_search_engine(Program_Utils pu) {
		iter = 0;
		ng = new NameGenerator();
	}

	@Override
	public Delta[] nextRefactorings(Program_Utils pu) {
		Delta[] result = null;
		if (curr_is_crdt) {
			result = new Delta[2];
		} else {
			logger.debug("next refactoring is introduction of a non_crdt table");
			result = new Delta[2];
			INTRO_F new_intro_f = getRandomNonCRDTIntroF(pu);
			logger.debug("first refactoring: introduction of new field " + new_intro_f.getNewName() + " in table "
					+ new_intro_f.getTableName());
			INTRO_VC new_intro_vc = getRandomNonCRDTIntroVC(pu, new_intro_f);
			if (new_intro_vc == null)
				return null;
			result[0] = new_intro_f;
			result[1] = new_intro_vc;
			VC new_vc = new_intro_vc.getVC();
			logger.debug("second refactoring: introduction of new VC (" + new_vc.getName() + ") between "
					+ new_vc.getTableName(1) + " and " + new_vc.getTableName(2) + ": " + new_vc);
		}
		return result;
	}

	private INTRO_F getRandomNonCRDTIntroF(Program_Utils pu) {
		Table random_table = getRandomTable(pu);
		String new_fn = ng.newFieldName();
		INTRO_F result = new INTRO_F(random_table.getTableName().getName(), new_fn, F_Type.NUM);
		return result;
	}

	private INTRO_VC getRandomNonCRDTIntroVC(Program_Utils pu, INTRO_F intro_f) {
		INTRO_VC result = null;
		if (pu.getTables().size() <= 1)
			return result;
		String table_with_new_field = intro_f.getTableName();
		String src_table_name = table_with_new_field;
		while (src_table_name.equals(table_with_new_field)) {// find a random table which is != table_with_new_field
			src_table_name = getRandomTable(pu).getTableName().getName();
			logger.debug("a new source table is proposed: " + src_table_name);
		}
		logger.debug("source and target tables determined: a vc must be returned between " + src_table_name + " and "
				+ table_with_new_field);
		Table src = pu.getTable(src_table_name);
		Table dest = pu.getTable(table_with_new_field);

		VC_Type random_type = getRandomVCType();
		switch (random_type) {
		case VC_OTO:
			result = new INTRO_VC(pu, src_table_name, table_with_new_field, VC_Agg.VC_ID, VC_Type.VC_OTO);
			List<FieldName> src_pks = src.getPKFields();
			List<FieldName> dest_pks = dest.getPKFields();
			assert (src_pks.size() == dest_pks.size()) : "unexpected pks in tables when VC_OTO is chosen";
			for (int i = 0; i < src_pks.size(); i++)
				result.addKeyCorrespondenceToVC(src_pks.get(i).getName(), dest_pks.get(i).getName());
			FieldName random_fn_from_src_table = getRandomNonPKFieldName(src);
			result.addFieldTupleToVC(random_fn_from_src_table, intro_f.getNewName());
			break;

		case VC_OTM:
			// TODO
			break;
		default:
			break;
		}
		return result;
	}

	private VC_Type getRandomVCType() {
		return VC_Type.VC_OTO;
		// return (Math.random() < 0.5) ? VC_Type.VC_OTO : VC_Type.VC_OTM;
	}

	private Table getRandomTable(Program_Utils pu) {
		int table_cnt = pu.getTables().size();
		int random_index = (int) (Math.random() * table_cnt);
		return (Table) pu.getTables().values().toArray()[random_index];
	}

	private FieldName getRandomNonPKFieldName(Table t) {
		FieldName result = null;

		while (true) {
			int field_cnt = t.getFieldNames().size();
			int random_index = (int) (Math.random() * field_cnt);
			result = t.getFieldNames().get(random_index);
			if (!result.isPK() && !result.getName().contains("alive"))
				break;
		}
		return result;
	}

	@Override
	public void reset() {
		this.iter = 0;
		this.curr_is_crdt = false; // Math.random() < 0.5;
	}

}
