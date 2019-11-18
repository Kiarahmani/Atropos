package kiarahmani.atropos.refactoring_engine;

import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.dependency.Conflict;
import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.program.statements.If_Statement;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.utils.Util;

public class Refactoring_Engine {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
/*
	public Conflict_Graph constructConfGraph(Program p) {
		logger.debug("Asserted that conflict graph is initialized");
		Conflict_Graph cg = new Conflict_Graph();
		for (int i = 0; i < p.numberOfTransactions(); i++) {
			Transaction txn1 = p.getTransactions(i);
			logger.debug("Begin inner loop for src txn=" + txn1.getName());
			for (int j = i + 1; j < p.numberOfTransactions(); j++) {
				Transaction txn2 = p.getTransactions(j);
				logger.debug("Begin analysis for dst txn=" + txn2.getName());
				// iterate over all statements of txn1 and txn2
				for (Statement stmt1 : txn1.getStatements()) {
					for (Statement stmt2 : txn2.getStatements()) {
						cg = constructConfGraph_help(cg, txn1, stmt1, txn2, stmt2);

					}
				}

			}
		}
		return cg;
	}

	private Conflict_Graph constructConfGraph_help(Conflict_Graph cg, Transaction txn1, Statement stmt1,
			Transaction txn2, Statement stmt2) {
		logger.debug("Begin conflict analysis for statements: " + stmt1.getId() + " and " + stmt2.getId());
		Query q1 = null, q2 = null;
		if (stmt1.getClass().toString().contains("Query"))
			q1 = ((Query_Statement) stmt1).getQuery();
		if (stmt2.getClass().toString().contains("Query"))
			q2 = ((Query_Statement) stmt2).getQuery();

		if (q1 == null && q2 == null) {
			logger.debug(
					"Both statements are IF statements: must recursively call constructConfGraph_help on the enclosed statements");
			for (Statement stmt1_iter : ((If_Statement) stmt1).getAllStatements())
				for (Statement stmt2_iter : ((If_Statement) stmt2).getAllStatements())
					cg = constructConfGraph_help(cg, txn1, stmt1_iter, txn2, stmt2_iter);
		}

		if (q1 == null && q2 != null) {
			logger.debug("Only Stmt#1 is IF: will recurse over all enclosed statements");
			for (Statement stmt1_iter : ((If_Statement) stmt1).getAllStatements())
				cg = constructConfGraph_help(cg, txn1, stmt1_iter, txn2, stmt2);

		}

		if (q1 != null && q2 == null) {
			logger.debug("Only Stmt#2 is IF: will recurse over all enclosed statements");
			for (Statement stmt2_iter : ((If_Statement) stmt2).getAllStatements())
				cg = constructConfGraph_help(cg, txn1, stmt1, txn2, stmt2_iter);
		}

		if (q1 != null && q2 != null) {
			logger.debug("Neither Stmts were If");
			Conflict c = constructConf(txn1, q1, txn2, q2);
			if (c != null)
				cg.addConflict(c);
		}
		return cg;
	}

	// given two *queries* returns the conflict between them
	// Returns null if there is no conflict
	private Conflict constructConf(Transaction txn1, Query q1, Transaction txn2, Query q2) {
		logger.debug("constructing conflict for queries  " + "q1: " + txn1.getName() + "." + q1.getId() + " and "
				+ "q2: " + txn2.getName() + "." + q2.getId());
		assert (q1 != null && q2 != null);
		ArrayList<FieldName> fns = new ArrayList<>();
		TableName tn = null;
		Conflict c = null;
		if (q1.getTableName() == q2.getTableName()) {
			tn = q1.getTableName();
			logger.debug("both queries share the same table (" + tn.toString() + "): must consisder analysis");
			if (q1.isWrite() || q2.isWrite()) {
				logger.debug("at least one of the queries is an update: must consisder analysis");
				logger.debug("accessed fields by q1: " + q1.getAccessedFieldNames());
				logger.debug("accessed fields by q2: " + q2.getAccessedFieldNames());
				fns = (ArrayList<FieldName>) Util.getIntersectOfCollections(q1.getAccessedFieldNames(),
						q2.getAccessedFieldNames());
				if (fns.size() > 0) {
					logger.debug("queries access some common fields: " + fns);
					c = new Conflict(txn1, q1, txn2, q2, tn, fns);
					logger.debug("new conflict constructed and added: " + c);
				} else
					logger.debug("queries do not share common field names: no need for analysis");
			} else
				logger.debug("both queries are select: no need for analysis");
		} else
			logger.debug("queries do NOT share the same table: no need for analysis");
		logger.debug("");

		return c;

	}
*/
}
