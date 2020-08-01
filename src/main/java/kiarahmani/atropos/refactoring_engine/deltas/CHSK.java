/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.deltas;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.utils.Program_Utils;

/**
 * @author Kiarash
 *
 */
public class CHSK extends Delta {

	private Table table;
	private FieldName old_sk, new_sk;
	private String new_table_name, new_sk_name;

	public CHSK(Program_Utils pu, String tn, String new_skn) {
		assert (false) : "don't call this";
		this.table = pu.getTable(tn);
		if (table != null) {
			this.old_sk = table.getShardKey();
			this.new_sk = pu.getFieldName(new_skn);
			assert (table.getFieldNames().contains(new_sk));
		} else {
			new_table_name = tn;
			new_sk_name = new_skn;
		}

	}

	public Table getTable(Program_Utils pu) {
		return (this.table != null) ? this.table : pu.getTable(new_table_name);
	}

	public FieldName getOldSK() {
		return this.old_sk;
	}

	public FieldName getNewSK(Program_Utils pu) {
		return (this.new_sk != null) ? this.new_sk : pu.getFieldName(new_sk_name);
	}

	/*
	 * @see kiarahmani.atropos.refactoring_engine.deltas.Delta#getDesc()
	 */
	@Override
	public String getDesc() {
		String old_desc = (old_sk == null) ? "NULL" : old_sk.getName();
		return "shard key of table \"" + new_table_name + "\" is changed from " + old_desc + " to " + new_sk_name;
	}

}
