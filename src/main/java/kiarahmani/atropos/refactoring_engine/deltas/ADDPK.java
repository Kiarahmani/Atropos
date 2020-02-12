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

	private String table_name;
	private Program_Utils pu;
	private String new_pk;

	public String getTable() {
		return this.table_name;
	}

	public String getNewPK() {
		return this.new_pk;
	}

	/**
	 * 
	 */
	public ADDPK(Program_Utils pu, String tn, String new_pkn) {
		this.table_name = tn;
		this.new_pk = new_pkn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.refactoring_engine.deltas.Delta#getDesc()
	 */
	@Override
	public String getDesc() {
		return "field " + new_pk + " is added as PK of table " + table_name;
	}

}
