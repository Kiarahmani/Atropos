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
	private Program_Utils pu;
	private FieldName old_sk, new_sk;

	public CHSK(Program_Utils pu, String tn, String new_skn) {
		this.table = pu.getTable(tn);
		this.old_sk = table.getShardKey();
		this.new_sk = pu.getFieldName(new_skn);
		assert (table.getFieldNames().contains(new_sk));
	}

	public Table getTable() {
		return this.table;
	}

	public FieldName getOldSK() {
		return this.old_sk;
	}

	public FieldName getNewSK() {
		return this.new_sk;
	}

	/*
	 * @see kiarahmani.atropos.refactoring_engine.deltas.Delta#getDesc()
	 */
	@Override
	public String getDesc() {
		String old_desc = (old_sk == null) ? "NULL" : old_sk.getName();
		return "shard key of table \"" + table.getTableName().getName() + "\" is changed from " + old_desc + " to "
				+ new_sk.getName();
	}

}
