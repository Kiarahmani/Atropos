package kiarahmani.atropos.dependency;

import java.util.ArrayList;

public class Conflict_Graph {
	private ArrayList<Conflict> conflicts;

	public Conflict_Graph() {
		this.conflicts = new ArrayList<>();
	}

	public void addConflict(Conflict c) {
		this.conflicts.add(c);
	}

	public void printGraph() {
		System.out.println("## CONFLICT GRAPH:\n");
		for (Conflict c : conflicts)
			System.out.println(c.toString());
	}
}
