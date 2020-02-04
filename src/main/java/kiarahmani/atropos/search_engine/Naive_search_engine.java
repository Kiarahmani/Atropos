/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.search_engine;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_F;
import kiarahmani.atropos.utils.Program_Utils;

/**
 * @author Kiarash
 *
 */
public class Naive_search_engine extends Search_engine {
	private int current_depth;
	private int max_depth;

	public Naive_search_engine() {
		current_depth = 0;
	}

	@Override
	public Delta nextRefactoring() {
		current_depth++;
		return new INTRO_F("car", "kir_" + current_depth, F_Type.NUM);
	}

	@Override
	public Delta nextIntroduction() {
		return new INTRO_F("car", "kir_" + current_depth, F_Type.NUM);
	}

	@Override
	public boolean hasNextRef() {
		return (current_depth <= max_depth);
	}

	@Override
	public void set(Program_Utils pu, int k) {
		max_depth = k;

	}

}
