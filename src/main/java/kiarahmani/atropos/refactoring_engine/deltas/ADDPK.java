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
public class ADDPK extends Delta {

	private Table table;
	private Program_Utils pu;
	private FieldName new_pk;

	public Table getTable() {
		return this.table;
	}

	public FieldName getNewPK() {
		return this.new_pk;
	}

	/**
	 * 
	 */
	public ADDPK(Program_Utils pu, String tn, String new_pkn) {
		this.table = pu.getTable(tn);
		this.new_pk = pu.getFieldName(new_pkn);
		assert (table.getFieldNames().contains(new_pk));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.refactoring_engine.deltas.Delta#getDesc()
	 */
	@Override
	public String getDesc() {
		return "field " + new_pk + " is added as PK of table " + table.getTableName().getName();
	}

}
