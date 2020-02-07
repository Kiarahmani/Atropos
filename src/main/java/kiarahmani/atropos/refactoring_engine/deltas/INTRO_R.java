/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.deltas;

/**
 * @author Kiarash
 *
 */

public class INTRO_R extends Delta {
	private String new_table_name;
	private boolean isCRDT;

	public INTRO_R(String ntn, boolean isCRDT) {
		this.new_table_name = ntn;
		this.isCRDT = isCRDT;
	}

	public String getNewTableName() {
		return this.new_table_name;
	}

	public boolean isCRDT() {
		return this.isCRDT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.refactoring_engine.deltas.Delta#getDesc()
	 */
	@Override
	public String getDesc() {
		return "table \"" + new_table_name + "\" added to the schema";
	}
}
