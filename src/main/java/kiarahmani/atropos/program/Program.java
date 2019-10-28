package kiarahmani.atropos.program;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI_Graph;

public class Program {
	private int conflict_degree;
	private String programName;
	private ArrayList<Transaction> transactions;

	private ArrayList<Table> tables;

	// constructor
	public Program(String name) {
		this.transactions = new ArrayList<Transaction>();
		this.programName = name;
		this.tables = new ArrayList<>();
	}

	public ArrayList<Transaction> getTransactions() {
		return this.transactions;
	}

	public int numberOfTransactions() {
		return this.transactions.size();
	}

	public Transaction getTransactions(int i) {
		return this.transactions.get(i);
	}

	public void addTable(Table t) {
		this.tables.add(t);
	}

	public void addTransaction(Transaction txn) {
		this.transactions.add(txn);
	}

	public void printProgram() {
		System.out.println("\n\n### PROGRAM: " + programName.toUpperCase() + "\n");
		System.out.println("## SCHEMA:");
		for (Table t : tables)
			System.out.println(t.toString());
		System.out.println("\n\n## TRANSACTIONS:");
		for (Transaction txn : this.transactions)
			txn.printTransaction();
	}
}
