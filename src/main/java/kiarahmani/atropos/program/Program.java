package kiarahmani.atropos.program;

import java.util.ArrayList;
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
	private ArrayList<Table> tables;

	// constructor
	public Program(String name) {
		this.transactions = new HashSet<Transaction>();
		this.programName = name;
		this.tables = new ArrayList<>();
	}

	public void setConflictGraph(Conflict_Graph cg) {
		this.conflict_graph = cg;
	}

	public boolean isConflictGraphInitialized() {
		return (this.conflict_graph != null);
	}

	public void addTable(Table t) {
		this.tables.add(t);
	}

	public void addTransaction(Transaction txn) {
		this.transactions.add(txn);
	}

	public void printProgram() {
		System.out.println(conflict_graph);
		System.out.println("\n\n### PROGRAM " + programName.toUpperCase() + "\n");
		System.out.println("## SCHEMA:");
		for (Table t : tables)
			System.out.println(t.toString());
		System.out.println("\n\n## TRANSACTIONS:");
		for (Transaction txn : this.transactions)
			txn.printTransaction();
		System.out.println("\n");
		// if (conflict_graph == null)
		// System.out.println("conflict graph not set yet....");
		// else
		this.conflict_graph.printGraph();
	}
}
