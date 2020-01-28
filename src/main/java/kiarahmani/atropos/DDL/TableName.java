package kiarahmani.atropos.DDL;

public class TableName {
	private String name;

	public TableName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public boolean equalsWith(TableName other) {
		return this.name.equalsIgnoreCase(other.getName());
	}
	
}
