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
}
