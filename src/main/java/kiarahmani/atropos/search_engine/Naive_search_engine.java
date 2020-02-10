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
	Program_Utils pu;
	private NameGenerator ng;
	private int iter;
	private boolean curr_is_crdt;

	public Naive_search_engine(Program_Utils pu) {
		this.pu = pu;
		iter = 0;
		ng = new NameGenerator();
	}

	@Override
	public Delta[] nextRefactorings() {
		Delta[] result = null;
		if (curr_is_crdt) {
			result = new Delta[2];
		} else {
			result = new Delta[2];
			INTRO_F new_intro_f = getRandomNonCRDTIntroF();
			result[0] = new_intro_f;
			result[1] = getRandomNonCRDTIntroVC(new_intro_f);
		}
		return result;
	}

	private INTRO_F getRandomNonCRDTIntroF() {
		Table random_table = getRandomTable();
		String new_fn = ng.newFieldName();
		INTRO_F result = new INTRO_F(random_table.getTableName().getName(), new_fn, F_Type.NUM);
		logger.error("Randomly generated (non-crdt) INTRO_F: " + new_fn + " on " + random_table.getTableName());
		return result;
	}

	private INTRO_VC getRandomNonCRDTIntroVC(INTRO_F intro_f) {
		INTRO_VC result = null;
		String table_with_new_field = intro_f.getTableName();
		String src_table_name = table_with_new_field;
		while (src_table_name.equals(table_with_new_field))// find a random table which is != table_with_new_field
			src_table_name = getRandomTable().getTableName().getName();
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
			FieldName random_fn_from_src_table = getRandomFieldName(src);
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

	private Table getRandomTable() {
		int table_cnt = pu.getTables().size();
		int random_index = (int) (Math.random() * table_cnt);
		return (Table) pu.getTables().values().toArray()[random_index];
	}

	private FieldName getRandomFieldName(Table t) {
		int field_cnt = t.getFieldNames().size();
		int random_index = (int) (Math.random() * field_cnt);
		return t.getFieldNames().get(random_index);
	}

	@Override
	public void reset() {
		this.iter = 0;
		this.curr_is_crdt = false; // Math.random() < 0.5;
	}

}
