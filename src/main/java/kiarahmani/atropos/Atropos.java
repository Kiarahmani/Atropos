package kiarahmani.atropos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DDL.vc.VC;
import kiarahmani.atropos.DDL.vc.VC.VC_Agg;
import kiarahmani.atropos.DDL.vc.VC.VC_Type;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Query.Kind;
import kiarahmani.atropos.DML.where_clause.WHCC;
import kiarahmani.atropos.dependency.DAI;
import kiarahmani.atropos.dependency.DAI_Graph;
import kiarahmani.atropos.encoding_engine.Encoding_Engine;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.refactoring.Refactor;
import kiarahmani.atropos.program_generators.SmallBank.OnlineCourse;
import kiarahmani.atropos.refactoring_engine.deltas.Delta;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_F;
import kiarahmani.atropos.refactoring_engine.deltas.INTRO_VC;
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
		// new TPCCProgramGenerator(pu).generate("newOrder", "payment", "stockLevel",
		// "orderStatus", "delivery");
		pu.print();
		// find anomlous access pairs in the base version
		ArrayList<DAI> anmls = analyze(pu, false).getDAIs();
		// initialize a refactoring engine
		Refactor re = new Refactor();
		// preform the pre-processing step (updates the pu and also the anmls list)
		// each query will be involved in at most one anomaly
		re.pre_process(pu, anmls);
		System.out.println("\n\n>>pre-processed");
		pu.print();
		anmls = analyze(pu, true).getDAIs();
		re.refactor_schema_seq(pu, repair(pu, anmls));
		System.out.println("\n\n>>repaired");
		pu.print();
		// System.out.println("\n\n\nPost Process \n\n\n");
		for (int i = 0; i < 10; i++)
			re.post_process(pu);
		System.out.println("\n\n>>post-processed");
		pu.print();
		printStats(System.currentTimeMillis() - time_begin, analyze(pu, true).getDAICnt());
	}

	private static ArrayList<Delta> repair(Program_Utils pu, ArrayList<DAI> anmls) {
		ArrayList<Delta> result = new ArrayList<>();
		for (DAI anml : anmls) {
			result.addAll(try_repair(pu, anml));
		}

		/*
		 * result.add(new INTRO_F("student", "st_co_avail", F_Type.NUM)); INTRO_VC refff
		 * = new INTRO_VC(pu, "course", "student", VC_Agg.VC_ID, VC_Type.VC_OTM);
		 * refff.addKeyCorrespondenceToVC("co_id", "st_co_id");
		 * refff.addFieldTupleToVC("co_avail", "st_co_avail"); result.add(refff);
		 * 
		 * Delta x8 = new INTRO_F("student", "st_em_addr", F_Type.TEXT); INTRO_VC x9 =
		 * new INTRO_VC(pu, "email", "student", VC_Agg.VC_ID, VC_Type.VC_OTM);
		 * x9.addKeyCorrespondenceToVC("em_id", "st_em_id");
		 * x9.addFieldTupleToVC("em_addr", "st_em_addr"); result.add(x8);
		 * result.add(x9);
		 * 
		 * 
		 * result.add(new INTRO_R("log", true)); result.add(new INTRO_F("log", "id",
		 * F_Type.NUM)); result.add(new INTRO_F("log", "counter", F_Type.NUM, true,
		 * false)); result.add(new INTRO_F("log", "val", F_Type.NUM, false, true));
		 * result.add(new ADDPK(pu, "log", "id")); result.add(new ADDPK(pu, "log",
		 * "counter")); INTRO_VC x7 = new INTRO_VC(pu, "course", "log", VC_Agg.VC_SUM,
		 * VC_Type.VC_OTM); x7.addKeyCorrespondenceToVC("co_id", "id");
		 * x7.addFieldTupleToVC("co_st_cnt", "val"); result.add(x7);
		 * 
		 * 
		 */
		return result;

	}

	private static ArrayList<Delta> try_repair(Program_Utils pu, DAI anml) {
		ArrayList<Delta> result = new ArrayList<>();
		Query q1 = anml.getQuery(1);
		Query q2 = anml.getQuery(2);
		TableName tn1 = q1.getTableName();
		TableName tn2 = q2.getTableName();

		if (q1.getKind() == Kind.SELECT && q2.getKind() == Kind.SELECT) {
			if (!tn1.equalsWith(tn2)) {
				ArrayList<INTRO_F> new_intro_fs = new ArrayList<>(); // to be added to the result
				for (FieldName fn : q2.getAccessedFieldNames())
					if (!fn.isAliveField())
						new_intro_fs.add(new INTRO_F(tn1.getName(), tn1.getName() + "_" + fn.getName(), fn.getType()));
				List<FieldName> pk_fns_2 = pu.getTable(tn2).getPKFields();
				List<FieldName> pk_fns_1 = pu.getTable(tn1).getPKFields();
				Map<FieldName, FieldName> vc_map = new HashMap<FieldName, FieldName>();
				boolean success = true;
				for (FieldName pk : pk_fns_2) {
					WHCC current_whcc = q2.getWHC().getConstraintByFieldName(pk);
					if (current_whcc != null) {
						try {
							E_Proj ep = (E_Proj) current_whcc.getExpression();
							if (ep.v.getTableName().equals(tn1.getName()))
								vc_map.put(pk, ep.f);
							else
								success = false;
						} catch (Exception e) {
							success = false;
						}
					} else
						success = false;
				}

				if (success) {
					result.addAll(new_intro_fs);
					boolean oto = vc_map.values().containsAll(pk_fns_1);
					if (oto)
						result.add(mk_ID_OTO_INTRO_VC(pu, pu.getTable(tn2), pu.getTable(tn1),
								q2.getAccessedFieldNames(), new_intro_fs));
					else
						result.add(mk_ID_OTM_INTRO_VC(pu, pu.getTable(tn2), pu.getTable(tn1),
								q2.getAccessedFieldNames().stream().filter(fn -> !fn.isAliveField())
										.collect(Collectors.toList()),
								new_intro_fs, vc_map.values().stream().collect(Collectors.toList())));
				}
			}
		}

		if (q1.getKind() == Kind.SELECT && q2.getKind() == Kind.UPDATE) {

		}

		return result;
	}

	private static INTRO_VC mk_ID_OTO_INTRO_VC(Program_Utils pu, Table source_table, Table target_table,
			ArrayList<FieldName> source_fns, ArrayList<INTRO_F> new_fns) {
		String source_table_name = source_table.getTableName().getName();
		String target_table_name = target_table.getTableName().getName();
		INTRO_VC result = new INTRO_VC(pu, source_table_name, target_table_name, VC_Agg.VC_ID, VC_Type.VC_OTO);
		// set key correspondence
		List<FieldName> source_pks = source_table.getPKFields();
		List<FieldName> target_pks = target_table.getPKFields();
		assert (source_pks.size() == target_pks.size()) : "unexpected pks in tables when VC_OTO is chosen";
		for (int i = 0; i < source_pks.size(); i++)
			result.addKeyCorrespondenceToVC(source_pks.get(i).getName(), target_pks.get(i).getName());
		// set value correspondence
		assert (source_fns.size() == new_fns.size());
		for (int i = 0; i < source_fns.size(); i++)
			result.addFieldTupleToVC(source_fns.get(i), new_fns.get(i).getNewName());
		return result;
	}

	private static INTRO_VC mk_ID_OTM_INTRO_VC(Program_Utils pu, Table source_table, Table target_table,
			List<FieldName> source_fns, List<INTRO_F> new_fns, List<FieldName> list) {
		String source_table_name = source_table.getTableName().getName();
		String target_table_name = target_table.getTableName().getName();
		INTRO_VC result = new INTRO_VC(pu, source_table_name, target_table_name, VC_Agg.VC_ID, VC_Type.VC_OTM);
		// set key correspondence
		List<FieldName> source_pks = source_table.getPKFields();
		for (int i = 0; i < source_pks.size(); i++)
			result.addKeyCorrespondenceToVC(source_pks.get(i).getName(), list.get(i).getName());
		// set value correspondence
		assert (source_fns.size() == new_fns.size());
		for (int i = 0; i < source_fns.size(); i++)
			result.addFieldTupleToVC(source_fns.get(i), new_fns.get(i).getNewName());
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
