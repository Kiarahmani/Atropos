/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.search_engine;

/**
 * @author Kiarash
 *
 */
public class NameGenerator {
	int r_cnt;
	int f_cnt;
	int uuid_cnt;

	public NameGenerator() {
		r_cnt = 0;
		f_cnt = 0;
		uuid_cnt = 0;
	}

	public String newRelationName() {
		return "nt_" + (r_cnt++);
	}

	public String newRelationName(String source_table) {
		return scramble(source_table) + "_crdt" + (r_cnt++);
	}

	public String newFieldName(String source_fn) {
		return "copy_of_" + scramble(source_fn) + "" + (f_cnt++);
	}

	private String scramble(String s) {
		return "X" + s.substring(2) + "X";
	}

	public String newUUIDName() {
		return "uuid_" + "" + (uuid_cnt++);
	}

}
