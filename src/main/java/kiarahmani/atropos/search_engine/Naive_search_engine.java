/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.search_engine;

import java.util.ArrayList;
import java.util.HashMap;
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
import kiarahmani.atropos.DDL.vc.VC;
import kiarahmani.atropos.DDL.vc.VC.VC_Agg;
import kiarahmani.atropos.DDL.vc.VC.VC_Type;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.refactoring_engine.deltas.ADDPK;
import kiarahmani.atropos.refactoring_engine.deltas.CHSK;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_F;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_R;
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
	private int max_iter;
	private VC_Type type;
	private VC_Agg agg;
	private Table source_table, target_table;
	private FieldName source_fn;
	private Delta[] result;
	HashMap<String, HashMap<String, HashSet<VC>>> history;

	/*
	 * Constructor
	 */
	public Naive_search_engine(HashMap<String, HashMap<String, HashSet<VC>>> history) {
		ng = new NameGenerator();
		this.history = history;
	}

	public boolean hasNext() {
		return (++iter) < max_iter;
	}

	private int eqVCCnt(Program_Utils pu, VC vc) {
		String src = vc.T_1;
		String dest = vc.T_2;
		int cnt = getVCFromHist(src, dest).stream().filter(vcc -> vcc.equalsWith(vc)).collect(Collectors.toSet())
				.size();
		return cnt;
	}

	private HashSet<VC> getVCFromHist(String src, String dest) {
		return this.history.get(src).get(dest);
	}

	/*
	 * Function called iteratively from the mainS
	 */
	@Override
	public Delta nextRefactoring(Program_Utils pu) {
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

	private Delta next_ID_OTO_refactorings(Program_Utils pu) {
		if (iter == 0) {
			String new_fn = ng.newFieldName(source_fn.getName());
			String target_table_name = target_table.getTableName().getName();
			INTRO_F intro_f = new INTRO_F(target_table_name, new_fn, F_Type.NUM);
			result[0] = intro_f;
			INTRO_VC new_intro_vc = mk_ID_OTO_INTRO_VC(pu, intro_f.getNewName());
			result[1] = new_intro_vc;
			if (eqVCCnt(pu, new_intro_vc.getVC()) > 0)
				result[1] = null;
		}
		return result[iter];
	}

	private Delta next_ID_OTM_refactorings(Program_Utils pu) {
		if (iter == 0) {
			String new_fn = ng.newFieldName(source_fn.getName());
			String target_table_name = target_table.getTableName().getName();
			INTRO_F intro_f = new INTRO_F(target_table_name, new_fn, F_Type.NUM);
			result[0] = intro_f;
			INTRO_VC new_intro_vc = mk_ID_OTM_INTRO_VC(pu, intro_f.getNewName());
			result[1] = new_intro_vc;
			if (new_intro_vc == null || eqVCCnt(pu, new_intro_vc.getVC()) > 0)
				result[1] = null;
		}
		return result[iter];
	}

	private Delta next_SUM_OTM_refactorings(Program_Utils pu) {
		if (iter == 0) {
			int pk_cnt = source_table.getPKFields().size();
			int index = 0;
			String source_table_name = source_table.getTableName().getName();
			// intro_r
			String new_table_name = ng.newRelationName(source_table_name);
			INTRO_R intro_r = new INTRO_R(new_table_name, true);
			result[index++] = intro_r;
			// intro_f (pks)
			ArrayList<INTRO_F> newly_added_introf = new ArrayList<>();
			for (int i = 0; i < pk_cnt; i++)
				newly_added_introf.add(new INTRO_F(new_table_name,
						ng.newFieldName(source_table.getPKFields().get(i).getName()), F_Type.NUM, false, false));
			// intro_f (uuid)
			newly_added_introf.add(new INTRO_F(new_table_name, ng.newUUIDName(), F_Type.NUM, true, false));
			// intro_f (delta)
			String newly_added_fn_name = ng.newFieldName(source_fn.getName());
			newly_added_introf.add(new INTRO_F(new_table_name, newly_added_fn_name, F_Type.NUM, false, true));
			// add all above to the result
			for (INTRO_F introf : newly_added_introf)
				result[index++] = introf;
			// add pks
			for (int i = 0; i < pk_cnt; i++)
				result[index++] = new ADDPK(pu, new_table_name, newly_added_introf.get(i).getNewName().getName());
			result[index++] = new ADDPK(pu, new_table_name, newly_added_introf.get(pk_cnt).getNewName().getName());
			// add shard key
			result[index++] = new CHSK(pu, new_table_name, newly_added_introf.get(0).getNewName().getName());

			// add intro_vc
			INTRO_VC intro_vc = new INTRO_VC(pu, source_table_name, new_table_name, VC_Agg.VC_SUM, VC_Type.VC_OTM);
			for (int i = 0; i < pk_cnt; i++)
				intro_vc.addKeyCorrespondenceToVC(source_table.getPKFields().get(i).getName(),
						newly_added_introf.get(i).getNewName().getName());
			intro_vc.addFieldTupleToVC(source_fn.getName(), newly_added_fn_name);
			result[index++] = intro_vc;
		}
		return result[iter];
	}

	/*
	 * Random Table Selection
	 */

	private Table getRandomTable(Program_Utils pu) {
		List<Table> filteredList = pu.getTables().values().stream().filter(t -> !t.isNew && !t.isAllPK())
				.collect(Collectors.toList());
		int table_cnt = filteredList.size();
		int random_index = (int) (Math.random() * table_cnt);
		return filteredList.get(random_index);
		// (Table) pu.getTables().values().toArray()[random_index];
	}

	private Table getRandomTable(Program_Utils pu, Table other_than_this) {
		ArrayList<Table> filtered_table_list = new ArrayList<>();
		for (Table t : pu.getTables().values())
			if (!t.is_equal(other_than_this) && !t.isNew)
				filtered_table_list.add(t);
		int filtered_table_cnt = filtered_table_list.size();
		int random_index = (int) (Math.random() * filtered_table_cnt);
		return filtered_table_list.get(random_index); // TODO IOB exception
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

	private FieldName getRandomFieldName(Program_Utils pu, Table from_this, ArrayList<FieldName> other_than_these) {
		List<FieldName> fns = from_this.getFieldNames().stream()
				.filter(fn -> (!fn.isAliveField() && !other_than_these.contains(fn))).collect(Collectors.toList());
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
		if (filtered_fns_cnt == 0)
			return null;
		return fns.get(random_index);
	}

	// returns only NUM type
	private ArrayList<FieldName> getNRandomFieldNames(Program_Utils pu, Table from_this, int n) {
		// assert (from_this.getFieldNames().size() > n) : "cannot request " + n + "
		// fieldNames from "
		// + from_this.getTableName();
		if (from_this.getFieldNames().size() <= n)
			return null;

		ArrayList<FieldName> result = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			FieldName candidate_fn = getRandomFieldName(pu, from_this, F_Type.NUM);
			while (result.contains(candidate_fn)) {
				candidate_fn = getRandomFieldName(pu, from_this, result);
			}
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
		if (target_pks == null)
			return null;
		for (int i = 0; i < source_pks.size(); i++)
			result.addKeyCorrespondenceToVC(source_pks.get(i).getName(), target_pks.get(i).getName());
		// set value correspondence
		result.addFieldTupleToVC(source_fn, new_fn);
		return result;
	}

	@Override
	// returns true if successful
	public boolean reset(Program_Utils pu) {
		this.iter = 0;
		this.source_table = getRandomTable(pu);
		this.source_fn = getRandomFieldName(pu, source_table, false, F_Type.NUM);
		if (this.source_fn == null) {
			logger.debug("#" + iter + " returning false because source fn could not be chosen");
			this.iter++;
			return false;
		}
		this.target_table = getRandomTable(pu, source_table);
		if (Math.random() < 0.3) { // CRDT or not
			// next refactoring is introduction of CRDT table and corresponding fields
			max_iter = 6 + 2 * source_table.getPKFields().size();
			this.agg = VC_Agg.VC_SUM;
			this.type = VC_Type.VC_OTM;
		} else {
			// next introduction is a non CRDT field into an existing table
			max_iter = 2;
			this.agg = VC_Agg.VC_ID;
			if (Math.random() < 0.5) { // OTO or not
				// the decided relationship between source and target table is OTO
				this.type = VC_Type.VC_OTO;
				if (target_table.getPKFields().size() != source_table.getPKFields().size())
					reset(pu);
			} else {
				// the decided relatinship between source and target table is OTM
				this.type = VC_Type.VC_OTM;
			}
		}
		result = new Delta[max_iter];
		logger.debug("#" + iter + " returning true");
		return true;
	}
}
