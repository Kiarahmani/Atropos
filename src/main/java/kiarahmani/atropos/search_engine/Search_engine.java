/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.search_engine;

import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.utils.Program_Utils;

public abstract class Search_engine {
	public abstract Delta nextIntroF();

	public abstract Delta nextIntroR();

	public abstract Delta nextIntroVC();

	public abstract boolean hasNext();

	public abstract void set(Program_Utils pu, int max_depth);
}
