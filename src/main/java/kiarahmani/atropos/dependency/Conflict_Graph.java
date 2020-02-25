package kiarahmani.atropos.dependency;

import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.program.statements.If_Statement;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.utils.Util;

public class Conflict_Graph {
	private ArrayList<Conflict> conflicts;
	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public ArrayList<Conflict> getConfsFromQuery(Query q, Transaction t) {
		ArrayList<Conflict> result = new ArrayList<>();
		ArrayList<Conflict> sorted_conflicts = new ArrayList<>();
		// first add the conflicts related to t
		for (Conflict c : this.conflicts)
			if (c.getTransaction(1).is_equal(t) && c.getTransaction(2).is_equal(t))
				sorted_conflicts.add(c);
		// then add conflicts not related to t
		for (Conflict c : this.conflicts)
			if (!c.getTransaction(1).is_equal(t) || !c.getTransaction(2).is_equal(t))
				sorted_conflicts.add(c);

		// System.out.println("conflicts: "+conflicts);
		// System.out.println("soret con: "+sorted_conflicts);

		for (Conflict c : sorted_conflicts)
			if (c.getTransaction(1).is_equal(t) && c.getQuery(1).equals_ids(q))
				result.add(c);
			else if (c.getTransaction(2).is_equal(t) && c.getQuery(2).equals_ids(q))
				result.add(createReverse(c));
		return result;
	}

	private Conflict createReverse(Conflict c) {
		return new Conflict(c.getTransaction(2), c.getQuery(2), c.getTransaction(1), c.getQuery(1), c.getTableName(),
				c.getFieldNames());
	}

	public Conflict_Graph(Program program) {
		this.conflicts = new ArrayList<>();
		for (int i = 0; i < program.numberOfTransactions(); i++) {
			Transaction txn1 = program.getTransactions(i);
			logger.debug("Begin inner loop for src txn=" + txn1.getName());
			for (int j = i; j < program.numberOfTransactions(); j++) {
				if (j == i) {
					Transaction txn2 = program.getTransactions(j);
					logger.debug("Begin analysis for dst txn=" + txn2.getName());
					// iterate over all statements of txn1 and txn2
					ArrayList<Statement> stmts1 = txn1.getStatements();
					ArrayList<Statement> stmts2 = txn2.getStatements();
					for (int m = 0; m < stmts1.size(); m++) {
						for (int n = m; n < stmts2.size(); n++) {
							constructConfGraph_help(txn1, stmts1.get(m), txn2, stmts2.get(n));
						}
					}
				} else {
					Transaction txn2 = program.getTransactions(j);
					logger.debug("Begin analysis for dst txn=" + txn2.getName());
					// iterate over all statements of txn1 and txn2
					for (Statement stmt1 : txn1.getStatements()) {
						for (Statement stmt2 : txn2.getStatements()) {
							constructConfGraph_help(txn1, stmt1, txn2, stmt2);
						}
					}
				}
			}
		}
	}

	private void constructConfGraph_help(Transaction txn1, Statement stmt1, Transaction txn2, Statement stmt2) {
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
					constructConfGraph_help(txn1, stmt1_iter, txn2, stmt2_iter);
		}

		if (q1 == null && q2 != null) {
			logger.debug("Only Stmt#1 is IF: will recurse over all enclosed statements");
			for (Statement stmt1_iter : ((If_Statement) stmt1).getAllStatements())
				constructConfGraph_help(txn1, stmt1_iter, txn2, stmt2);

		}

		if (q1 != null && q2 == null) {
			logger.debug("Only Stmt#2 is IF: will recurse over all enclosed statements");
			for (Statement stmt2_iter : ((If_Statement) stmt2).getAllStatements())
				constructConfGraph_help(txn1, stmt1, txn2, stmt2_iter);
		}

		if (q1 != null && q2 != null) {
			logger.debug("Neither Stmts were If");
			Conflict c = constructConf(txn1, q1, txn2, q2);
			if (c != null)
				if (!q1.isWrite() || !q2.isWrite())
					addConflict(c);
		}
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

				ArrayList<FieldName> accessed_by_q1 = (q1.isWrite()) ? q1.getWrittenFieldNames()
						: q1.getReadFieldNames();
				ArrayList<FieldName> accessed_by_q2 = (q2.isWrite()) ? q2.getWrittenFieldNames()
						: q2.getReadFieldNames();

				logger.debug("at least one of the queries is an update: must consisder analysis");
				logger.debug(
						"accessed (written or read, depending on the type of query) fields by q1: " + accessed_by_q1);
				logger.debug(
						"accessed (written or read, depending on the type of query) fields by q2: " + accessed_by_q2);
				fns = (ArrayList<FieldName>) Util.getIntersectOfCollections(accessed_by_q1, accessed_by_q2);
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

	public void addConflict(Conflict c) {
		// Vonflict edges are undirected
		assert (c != null) : "Must NOT add a null conflict";
		for (Conflict c1 : this.conflicts)
			if (((c1.getQuery(1) == c.getQuery(1)) && (c1.getQuery(2) == c.getQuery(2)))
					|| ((c1.getQuery(1) == c.getQuery(2)) && (c1.getQuery(2) == c.getQuery(1))))
				return;
		this.conflicts.add(c);
	}

	public void printGraph() {
		System.out.println("\n\n## CONFLICT GRAPH:");
		for (Conflict c : conflicts)
			System.out.println(c.toString());
	}

}
