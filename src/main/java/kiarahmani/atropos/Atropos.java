package kiarahmani.atropos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.vc.VC;
import kiarahmani.atropos.DDL.vc.VC.VC_Agg;
import kiarahmani.atropos.DDL.vc.VC.VC_Type;
import kiarahmani.atropos.dependency.DAI;
import kiarahmani.atropos.dependency.DAI_Graph;
import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.refactoring.Refactor;
import kiarahmani.atropos.refactoring.RefactorEngine;
import kiarahmani.atropos.program_generators.SmallBank.OnlineCourse;
import kiarahmani.atropos.refactoring_engine.deltas.ADDPK;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_F;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_R;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_VC;
import kiarahmani.atropos.search_engine.Optimal_search_engine_wikipedia;
import kiarahmani.atropos.utils.Constants;
import kiarahmani.atropos.utils.Program_Utils;

public class Atropos {

	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public static void main(String[] args) {
		long time_begin = System.currentTimeMillis();
		try {
			new Constants();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// initialize
		Program_Utils pu = new Program_Utils("Course");
		// get the first instance of the program
		new OnlineCourse(pu).generate();
		pu.print();
		// find anomlous access pairs in the base version
		ArrayList<DAI> anmls = analyze(pu, true).getDAIs();
		// initialize a refactoring engine
		Refactor re = new Refactor();
		// preform the pre-processing step (updates the pu and also the anmls list)
		// each query will be involved in at most one anomaly
		re.pre_process(pu, anmls);
		anmls = analyze(pu, false).getDAIs();
		re.refactor_schema_seq(pu, repair(pu));
		// System.out.println("\n\n\nPost Process \n\n\n");
		for (int i = 0; i < 10; i++)
			re.post_process(pu);
		pu.print();
		printStats(System.currentTimeMillis() - time_begin, analyze(pu, true).getDAICnt());
	}

	private static ArrayList<Delta> repair(Program_Utils pu) {
		ArrayList<Delta> result = new ArrayList<>();
		result.add(new INTRO_F("student", "st_co_avail", F_Type.NUM));
		INTRO_VC refff = new INTRO_VC(pu, "course", "student", VC_Agg.VC_ID, VC_Type.VC_OTM);
		refff.addKeyCorrespondenceToVC("co_id", "st_co_id");
		refff.addFieldTupleToVC("co_avail", "st_co_avail");
		result.add(refff);

		result.add(new INTRO_R("log", true));
		result.add(new INTRO_F("log", "id", F_Type.NUM));
		result.add(new INTRO_F("log", "counter", F_Type.NUM, true, false));
		result.add(new INTRO_F("log", "val", F_Type.NUM, false, true));

		result.add(new ADDPK(pu, "log", "id"));
		result.add(new ADDPK(pu, "log", "counter"));
		INTRO_VC x7 = new INTRO_VC(pu, "course", "log", VC_Agg.VC_SUM, VC_Type.VC_OTM);
		x7.addKeyCorrespondenceToVC("co_id", "id");
		x7.addFieldTupleToVC("co_st_cnt", "val");
		result.add(x7);

		Delta x8 = new INTRO_F("student", "st_em_addr", F_Type.TEXT);
		INTRO_VC x9 = new INTRO_VC(pu, "email", "student", VC_Agg.VC_ID, VC_Type.VC_OTM);
		x9.addKeyCorrespondenceToVC("em_id", "st_em_id");
		x9.addFieldTupleToVC("em_addr", "st_em_addr");
		result.add(x8);
		result.add(x9);
		return result;

	}

	/**
	 * @param anmls
	 * @returns a filtered set of anomalies which ensures that each query is at most
	 *          involved in a single DAI
	 */

	private static HashMap<String, HashMap<String, HashSet<VC>>> initHist(Program_Utils pu) {
		HashMap<String, HashMap<String, HashSet<VC>>> history = new HashMap<>();
		for (Table t : pu.getTables().values()) {
			HashMap<String, HashSet<VC>> newMap = new HashMap<>();
			for (Table tt : pu.getTables().values())
				newMap.put(tt.getTableName().getName(), new HashSet<>());
			history.put(t.getTableName().getName(), newMap);
		}
		return history;
	}

	private static DAI_Graph analyze(Program_Utils pu, boolean shouldPrint) {
		Encoding_Engine ee = new Encoding_Engine(pu.getProgramName());
		DAI_Graph dai_graph = ee.constructInitialDAIGraph(pu);
		if (shouldPrint)
			dai_graph.printDAIGraph();
		return dai_graph;
	}

	private static void printStats(long time, int number_of_anomalies) {

		System.out.println(
				"\n\n\n\n============================================================================================");
		System.out.println();
		System.out.println("Total Memory: "
				+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000 + " MB");
		System.out.println("Total Time:   " + time / 1000.0 + " s\n");
		try {
			Files.write(Paths.get("results.atropos"), ("\n" + number_of_anomalies + "," + (time / 1000.0)).getBytes(),
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			// exception handling left as an exercise for the reader
		}

	}
}
