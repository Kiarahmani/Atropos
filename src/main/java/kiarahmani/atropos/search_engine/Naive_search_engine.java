/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.search_engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

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
	private NameGenerator ng;
	private int iter;
	private VC_Type type;
	private VC_Agg agg;
	private Table source_table, target_table;
	private FieldName source_fn;

	/*
	 * Constructor
	 */
	public Naive_search_engine(Program_Utils pu) {
		ng = new NameGenerator();
		reset(pu);
	}

	/*
	 * Function called iteratively from the mainS
	 */
	@Override
	public Delta[] nextRefactorings(Program_Utils pu) {
		reset(pu);
		switch (agg) {
		case VC_SUM:
			return next_SUM_OTM_refactorings(pu);
		case VC_ID:
			switch (type) {
			case VC_OTO:
				return next_ID_OTO_refactorings(pu);
			case VC_OTM:
				return next_ID_OTM_refactorings(pu);
			default:
				break;
			}
		default:
			break;
		}
		return null;
	}

	/*
	 * Helping functions that are called in different cases above
	 */

	private Delta[] next_ID_OTO_refactorings(Program_Utils pu) {
		Delta[] result = new Delta[2];
		String new_fn = ng.newFieldName();
		String target_table_name = target_table.getTableName().getName();
		INTRO_F intro_f = new INTRO_F(target_table_name, new_fn, F_Type.NUM);
		result[0] = intro_f;
		result[1] = mk_ID_OTO_INTRO_VC(pu, intro_f.getNewName());
		return result;
	}

	private Delta[] next_ID_OTM_refactorings(Program_Utils pu) {
		Delta[] result = new Delta[2];
		String new_fn = ng.newFieldName();
		String target_table_name = target_table.getTableName().getName();
		INTRO_F intro_f = new INTRO_F(target_table_name, new_fn, F_Type.NUM);
		result[0] = intro_f;
		result[1] = mk_ID_OTM_INTRO_VC(pu, intro_f.getNewName());
		return result;
	}

	private Delta[] next_SUM_OTM_refactorings(Program_Utils pu) {
		// TODO
		return null;
	}

	/*
	 * Random Table Selection
	 */

	private Table getRandomTable(Program_Utils pu) {
		int table_cnt = pu.getTables().size();
		int random_index = (int) (Math.random() * table_cnt);
		return (Table) pu.getTables().values().toArray()[random_index];
	}

	private Table getRandomTable(Program_Utils pu, Table other_than_this) {
		ArrayList<Table> filtered_table_list = new ArrayList<>();
		for (Table t : pu.getTables().values())
			if (!t.is_equal(other_than_this))
				filtered_table_list.add(t);
		int filtered_table_cnt = filtered_table_list.size();
		int random_index = (int) (Math.random() * filtered_table_cnt);
		return filtered_table_list.get(random_index);
	}

	/*
	 * Random FieldName Selection
	 */

	private FieldName getRandomFieldName(Program_Utils pu, Table from_this) {
		List<FieldName> fns = from_this.getFieldNames().stream().filter(fn -> (!fn.isAliveField()))
				.collect(Collectors.toList());
		int filtered_fns_cnt = fns.size();
		int random_index = (int) (Math.random() * filtered_fns_cnt);
		return fns.get(random_index);
	}

	private FieldName getRandomFieldName(Program_Utils pu, Table from_this, F_Type tp) {
		List<FieldName> fns = from_this.getFieldNames().stream()
				.filter(fn -> (!fn.isAliveField() && fn.getType() == tp)).collect(Collectors.toList());
		int filtered_fns_cnt = fns.size();
		int random_index = (int) (Math.random() * filtered_fns_cnt);
		return fns.get(random_index);
	}

	private FieldName getRandomFieldName(Program_Utils pu, Table from_this, boolean pk) {
		List<FieldName> fns = from_this.getFieldNames().stream().filter(fn -> (!fn.isAliveField() && fn.isPK() == pk))
				.collect(Collectors.toList());
		int filtered_fns_cnt = fns.size();
		int random_index = (int) (Math.random() * filtered_fns_cnt);
		return fns.get(random_index);
	}

	private FieldName getRandomFieldName(Program_Utils pu, Table from_this, boolean pk, F_Type tp) {
		List<FieldName> fns = from_this.getFieldNames().stream()
				.filter(fn -> (!fn.isAliveField() && fn.isPK() == pk && fn.getType() == tp))
				.collect(Collectors.toList());
		int filtered_fns_cnt = fns.size();
		int random_index = (int) (Math.random() * filtered_fns_cnt);
		return fns.get(random_index);
	}

	// returns only NUM type
	private ArrayList<FieldName> getNRandomFieldNames(Program_Utils pu, Table from_this, int n) {
		assert (from_this.getFieldNames().size() > n) : "cannot request n>number_of_fields";
		ArrayList<FieldName> result = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			FieldName candidate_fn = getRandomFieldName(pu, from_this, F_Type.NUM);
			while (result.contains(candidate_fn))
				candidate_fn = getRandomFieldName(pu, from_this);
			result.add(candidate_fn);
		}
		return result;
	}

	/*
	 * INTRO_VC builders
	 */

	private INTRO_VC mk_ID_OTO_INTRO_VC(Program_Utils pu, FieldName new_fn) {
		String source_table_name = source_table.getTableName().getName();
		String target_table_name = target_table.getTableName().getName();
		INTRO_VC result = new INTRO_VC(pu, source_table_name, target_table_name, VC_Agg.VC_ID, VC_Type.VC_OTO);
		// set key correspondence
		List<FieldName> source_pks = source_table.getPKFields();
		List<FieldName> target_pks = target_table.getPKFields();
		assert (source_pks.size() == target_pks.size()) : "unexpected pks in tables when VC_OTO is chosen";
		for (int i = 0; i < source_pks.size(); i++)
			result.addKeyCorrespondenceToVC(source_pks.get(i).getName(), target_pks.get(i).getName());
		// set value correspondence
		result.addFieldTupleToVC(source_fn, new_fn);
		return result;
	}

	private INTRO_VC mk_ID_OTM_INTRO_VC(Program_Utils pu, FieldName new_fn) {
		String source_table_name = source_table.getTableName().getName();
		String target_table_name = target_table.getTableName().getName();
		INTRO_VC result = new INTRO_VC(pu, source_table_name, target_table_name, VC_Agg.VC_ID, VC_Type.VC_OTM);
		// set key correspondence
		List<FieldName> source_pks = source_table.getPKFields();
		ArrayList<FieldName> target_pks = getNRandomFieldNames(pu, target_table, source_pks.size());
		for (int i = 0; i < source_pks.size(); i++)
			result.addKeyCorrespondenceToVC(source_pks.get(i).getName(), target_pks.get(i).getName());
		// set value correspondence
		result.addFieldTupleToVC(source_fn, new_fn);
		return result;
	}

	private INTRO_VC mk_SUM_OTM_INTRO_VC(Program_Utils pu) {
		// TODO
		return null;
	}

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	@Override
	// returns true if successful
	public boolean reset(Program_Utils pu) {
		this.iter = 0;
		this.source_table = getRandomTable(pu);
		this.source_fn = getRandomFieldName(pu, source_table, false);
		this.target_table = getRandomTable(pu, source_table);
		if (Math.random() < -100) { // CRDT or not
			// next refactoring is introduction of CRDT table and corresponding fields
			this.agg = VC_Agg.VC_SUM;
		} else {
			// next introduction is a non CRDT field into an existing table
			this.agg = VC_Agg.VC_ID;
			if (Math.random() < 0.5) { // OTO or not
				// the decided relationship between source and target table is OTO
				this.type = VC_Type.VC_OTO;
			} else {
				// the decided relatinship between source and target table is OTM
				this.type = VC_Type.VC_OTM;
			}
		}
		return true;
	}

}
