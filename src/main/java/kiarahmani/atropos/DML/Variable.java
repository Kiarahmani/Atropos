package kiarahmani.atropos.DML;

public class Variable {
	private String name;
	private String tableName;

	public Variable(String tableName, String name) {
		this.name = name;
		this.tableName = tableName;
	}

	public String getTableName() {
		return this.tableName;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
