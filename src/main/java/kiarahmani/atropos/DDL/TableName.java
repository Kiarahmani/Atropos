package kiarahmani.atropos.DDL;

public class TableName {
	private String name;

	public TableName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
