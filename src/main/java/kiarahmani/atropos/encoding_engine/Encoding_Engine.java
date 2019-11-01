package kiarahmani.atropos.encoding_engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI_Graph;
import kiarahmani.atropos.encoding_engine.Z3.Z3Driver;
import kiarahmani.atropos.encoding_engine.Z3.Z3Logger;
import kiarahmani.atropos.program.Program;

public class Encoding_Engine {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private Program program;
	private Conflict_Graph conflict_graph;
	private Z3Driver z3_driver;

	public Encoding_Engine() {

	}

	public DAI_Graph constructInitialDAIGraph(Program program, Conflict_Graph conflict_graph) {
		logger.debug("Begin analysis to create *initial* DAI graph");
		this.conflict_graph = conflict_graph;
		this.program = program;
		new Z3Logger("smt2/z3-encoding.smt2");

		int current_cycle_length = 4;

		this.z3_driver = new Z3Driver(this.program, current_cycle_length);

		// there will be a loop here ---> similar to CLOTHO must find all bounded
		// anomlies within a given bound
		// To be more precise, each iteration will return a set of isolation
		// requirements within operations of the same transaction. We will then take
		// them and merge them into existing requirements.
		// The new set of requirements will be used for the next round of analysis (??)
		//

		return null;
	}

}
