package kiarahmani.atropos.encoding_engine;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
							// if (q1.getKind()!=q2.getKind())
							// continue;
							// create a potential DAI
							dai = new DAI(txn, q1, q1.getAccessedFieldNames(), q2, q2.getAccessedFieldNames());
							Z3Driver local_z3_driver = new Z3Driver();
							System.out.println("dai: " + dai.getTransaction().getName() + "-po"
									+ dai.getQuery(1).getPo() + "-po" + dai.getQuery(2).getPo());
							System.out.println("c1:  " + c1.getTransaction(1).getName() + ".po" + c1.getQuery(1).getPo()
									+ "--" + c1.getTransaction(2).getName() + ".po" + c1.getQuery(2).getPo());
							System.out.println("c2:  " + c2.getTransaction(1).getName() + ".po" + c2.getQuery(1).getPo()
									+ "--" + c2.getTransaction(2).getName() + ".po" + c2.getQuery(2).getPo());
		
							local_z3_driver.generateDAI(this.program, 4, dai, c1, c2);
							local_z3_driver = null;
						}
				}
		}
	}
}