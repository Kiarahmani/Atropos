package kiarahmani.atropos.program;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.E_Arg;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.query.Query;

public class Transaction {
	private String TransactionName;
	private ArrayList<Expression> assertions;
	private ArrayList<Statement> statements;
	private ArrayList<E_Arg> args;
	public boolean is_included;

	public ArrayList<E_Arg> getArgs() {
		return this.args;
	}

	public boolean is_equal(Transaction other) {
		return this.TransactionName.equals(other.getName());
	}

	public ArrayList<Expression> getAssertions() {
		return this.assertions;
	}

	public void addAssertion(Expression ass) {
		this.assertions.add(ass);
	}

	public String getName() {
		return this.TransactionName;
	}

	public Transaction(String name) {
		this.TransactionName = name;
		this.statements = new ArrayList<>();
		this.args = new ArrayList<>();
		this.assertions = new ArrayList<>();
	}

	public ArrayList<Statement> getStatements() {
		return this.statements;
	}

	public void addStatement(Statement statement) {
		this.statements.add(statement);
	}

	public ArrayList<Query> getAllQueries() {
		ArrayList<Query> result = new ArrayList<>();
		for (Statement stmt : getStatements())
			result.addAll(stmt.getAllQueries());
		return result;
	}

	public HashSet<E_Proj> getAllProjExps() {
		HashSet<E_Proj> result = new HashSet<>();
		for (Query q : this.getAllQueries())
			result.addAll(q.getAllProjExps());
		return result;
	}

	public String[] getAllStmtTypes() {
		List<String> result = new ArrayList<String>();
		int size = 0;
		for (Statement stmt : getStatements())
			for (String s : stmt.getAllQueryIds()) {
				result.add(getName() + "-" + s);
				size++;
			}
		return result.toArray(new String[size]);
	}

	public void addArg(E_Arg a) {
		this.args.add(a);
	}


	/*
	 * Returns the query with requested PO
	 */
	public Query getQueryByPo(int q_po) {
		for (Query q : this.getAllQueries())
			if (q.getPo() == q_po)
				return q;
		return null;
	}

	public void printTransaction() {
		System.out.print(TransactionName + "(");
		String delim = "";
		for (E_Arg a : this.args) {
			System.out.print(delim + a.toString());
			delim = ", ";
		}

		if (this.assertions.size() > 0) {
			System.out.print("){  [");
			delim = "";
			for (Expression exp : this.assertions) {
				System.out.print(delim + "assert:" + exp);
				delim = "	";
			}
			System.out.println("]");
		} else
			System.out.println("){  ");
		for (Statement stmt : this.statements)
			stmt.printStatemenet("  ");
		System.out.println("}");
	}

	public Transaction mkSnapshot() {
		Transaction result = new Transaction(this.getName());
		// assertions
		for (Expression exp : this.assertions)
			result.addAssertion(exp.mkSnapshot());
		// statements
		for (Statement stmt : this.statements)
			result.addStatement(stmt.mkSnapshot());
		// args
		for (E_Arg arg : this.args)
			result.addArg((E_Arg) arg.mkSnapshot());

		result.is_included = this.is_included;
		return result;
	}

}
