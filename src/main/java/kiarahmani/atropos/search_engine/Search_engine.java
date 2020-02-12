/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.search_engine;

import java.util.ArrayList;

import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.utils.Program_Utils;

public abstract class Search_engine {
	public abstract Delta[] nextRefactorings(Program_Utils pu, ArrayList<Delta> history);

	public abstract boolean reset(Program_Utils pu);

}
