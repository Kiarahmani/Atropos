package kiarahmani.atropos.refactoring_engine;

import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.program.Program;

public class Refactoring_Engine {

	public void setInitConfGraph(Program p) {
		assert (!p.isConflictGraphInitialized()) : "conflict graph has already been set";
		Conflict_Graph cg = new Conflict_Graph();
		// create and store the conflict graph in cg
		p.setConflictGraph(cg);

	}

}
