package kiarahmani.atropos.encoding_engine;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.z3.Status;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.dependency.Conflict;
import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI;
import kiarahmani.atropos.encoding_engine.Z3.Z3Driver;
import kiarahmani.atropos.encoding_engine.Z3.Z3Logger;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Transaction;

public class Encoding_Engine {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private Program program;

	public Encoding_Engine(Program program) {
		new Z3Logger("smt2/z3-encoding.smt2");
		this.program = program;
	}

	public void constructInitialDAIGraph(Conflict_Graph cg) {
		DAI dai;
		System.out.println("\n\n\n");
		for (Transaction txn : program.getTransactions()) {
			ArrayList<Query> all_queries = txn.getAllQueries();
			for (int i = 0; i < all_queries.size(); i++)
				for (int j = i + 1; j < all_queries.size(); j++) {
					Query q1 = all_queries.get(i);
					Query q2 = all_queries.get(j);
					for (Conflict c1 : cg.getConfsFromQuery(q1))
						for (Conflict c2 : cg.getConfsFromQuery(q2)) {
							// create a potential DAI
							dai = new DAI(txn, q1, q1.getAccessedFieldNames(), q2, q2.getAccessedFieldNames());
							Z3Driver local_z3_driver = new Z3Driver();
							Status status = local_z3_driver.generateDAI(this.program, 4, dai, c1, c2);
							printBaseAnomaly(status, dai, c1, c2);
							// free memory
							local_z3_driver = null;
						}
				}
		}
	}

	private void printBaseAnomaly(Status status, DAI dai, Conflict c1, Conflict c2) {
		System.out.println("\n***********************************");
		System.out.println("[" + status + "]");
		System.out.printf("%-12s %s\n", dai.getTransaction().getName(), c1.getTransaction(2).getName());
		System.out.printf("%-12s %s\n", "----", "----");
		System.out.printf("po#%-1s ======= po#%s\n", c1.getQuery(1).getPo(), c1.getQuery(2).getPo());
		//
		System.out.println();
		//
		System.out.printf("%-12s %s\n", "", c2.getTransaction(2).getName());
		System.out.printf("%-12s %s\n", "", "----");
		System.out.printf("po#%-1s ======= po#%s\n", dai.getQuery(2).getPo(), c2.getQuery(2).getPo());
		System.out.println("\n");
	}

}