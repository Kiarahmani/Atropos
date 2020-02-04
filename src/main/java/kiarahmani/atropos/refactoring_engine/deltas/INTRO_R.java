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

	public INTRO_R(String ntn) {
		this.new_table_name = ntn;
	}

	public String getNewTableName() {
		return this.new_table_name;
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
