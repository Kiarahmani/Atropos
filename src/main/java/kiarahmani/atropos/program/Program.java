package kiarahmani.atropos.program;

import java.util.HashSet;
import java.util.Set;

import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI_Graph;

public class Program {
	private int conflict_degree;
	private String programName;
	private Set<Transaction> transactions;
	private DAI_Graph dai_graph;
	private Conflict_Graph conflict_graph;

	// constructor
	public Program(String name) {
		this.transactions = new HashSet<Transaction>();
		this.programName = name;
	}

	public void addTransaction(Transaction txn) {
		this.transactions.add(txn);
	}

	public void printProgram() {
		System.out.println("Program " + programName);
		for (Transaction txn : this.transactions)
			txn.printTransaction();
	}
}
