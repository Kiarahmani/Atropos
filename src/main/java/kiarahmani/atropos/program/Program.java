package kiarahmani.atropos.program;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.vc.VC;

public class Program {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	private int conflict_degree;
	private int max_query_count_in_transactions;
	private String programName;
	private ArrayList<Transaction> transactions;
	private ArrayList<Table> tables;
	private ArrayList<VC> vcs;

	// constructor
	public Program(String name) {
		this.transactions = new ArrayList<Transaction>();
		this.vcs = new ArrayList<VC>();
		this.programName = name;
		setMaxQueryCount();
		this.tables = new ArrayList<>();
	}

	public void addVC(VC vc) {
		this.vcs.add(vc);
	}

	public ArrayList<Transaction> getTransactions() {
		return this.transactions;
	}

	public ArrayList<Transaction> getIncludedTransactions() {
		ArrayList<Transaction> result = new ArrayList<>();
		for (Transaction txn : this.transactions)
			if (txn.is_included)
				result.add(txn);
		return result;
	}

	public void setMaxQueryCount() {
		int result = -1;
		for (Transaction txn : this.transactions) {
			int current_size = txn.getAllStmtTypes().length;
			result = (current_size > result) ? current_size : result;
		}
		this.max_query_count_in_transactions = result;
	}

	public int getMaxQueryCount() {
		return this.max_query_count_in_transactions;
	}

	public int numberOfTransactions() {
		return this.transactions.size();
	}

	public int numberOfIncludedTransactions() {
		return getIncludedTransactions().size();
	}

	public Transaction getTransactions(int i) {
		return this.transactions.get(i);
	}

	public void addTable(Table t) {
		this.tables.add(t);
	}

	public String getName() {
		return this.programName;
	}

	public String[] getAllStmtTypes() {
		List<String> result = new ArrayList<String>();
		int size = 0;
		for (Transaction t : this.transactions)
			for (Statement stmt : t.getStatements())
				for (String s : stmt.getAllQueryIds()) {
					result.add(t.getName() + "-" + s);
					logger.debug("Query type " + s + " added to encoding from transaction " + t.getName());
					size++;
				}
		return result.toArray(new String[size]);
	}

	public String[] getAllTxnNames() {
		List<String> result = new ArrayList<String>();
		int size = 0;
		for (Transaction t : this.transactions) {
			result.add(t.getName());
			size++;
		}
		return result.toArray(new String[size]);
	}

	public String[] getAllIncludedTxnNames() {
		List<String> result = new ArrayList<String>();
		int size = 0;
		for (Transaction t : this.transactions) {
			if (t.is_included) {
				result.add(t.getName());
				size++;
			}
		}
		return result.toArray(new String[size]);
	}

	public ArrayList<Table> getTables() {
		return this.tables;
	}

	public String[] getAllTableNames() {
		List<String> result = new ArrayList<String>();
		int size = 0;
		for (Table t : this.tables) {
			result.add(t.getTableName().getName());
			size++;
		}
		return result.toArray(new String[size]);
	}

	public String[] getAllFieldNames() {
		List<String> result = new ArrayList<String>();
		int size = 0;
		for (Table t : this.tables)
			for (FieldName fn : t.getFieldNames()) {
				result.add(t.getTableName().getName() + "-" + fn.getName());
				size++;
			}
		return result.toArray(new String[size]);
	}

	public void addTransaction(Transaction txn) {
		this.transactions.add(txn);
	}

	public void printProgram() {
		System.out.println(
				"\n\n\n\n##############################################################################################################################");
		System.out.println("### " + programName.toUpperCase());
		System.out.println("## SCHEMA:");
		for (Table t : tables)
			System.out.println(t.toString());
		System.out.println("\n## VC:");
		for (VC v : vcs)
			System.out.println(v.toString());
		System.out.println("\n\n## TRANSACTIONS:");
		for (Transaction txn : this.transactions)
			txn.printTransaction();
	}
}
