package kiarahmani.atropos.dependency;

import java.util.ArrayList;

public class DAI_Graph {
	private ArrayList<DAI> dais;

	public DAI_Graph() {
		this.dais = new ArrayList<>();
	}

	public void addDAI(DAI input_dai) {
		boolean contains = false;
		for (DAI dai : this.dais)
			if (input_dai.equals(dai)) {
				contains = true;
				break;
			}
		if (!contains)
			this.dais.add(input_dai);
	}

	public ArrayList<DAI> getDAIs() {
		return this.dais;
	}

	public void printDAIGraph() {
		System.out.println("\n## DAI GRAPH");
		int iter = 1;
		for (DAI dai : this.dais)
			System.out.println("(" + (iter++) + ") " + dai);
	}

	public int getDAICnt() {
		return this.dais.size();
	}

}
