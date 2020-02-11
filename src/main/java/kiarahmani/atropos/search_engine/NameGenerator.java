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

	public NameGenerator() {
		r_cnt = 0;
		f_cnt = 0;
	}

	public String newRelationName() {
		return "nt_" + (r_cnt++);
	}

	public String newFieldName() {
		return "nf_" + (f_cnt++);
	}
}
