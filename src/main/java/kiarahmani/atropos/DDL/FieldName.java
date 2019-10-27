package kiarahmani.atropos.DDL;

public class FieldName {

	private F_Type f_type;
	private String name;
	private boolean isPK;
	private boolean isSK;

	public boolean isPK() {
		return this.isPK;
	}

	public FieldName(String name, boolean isPK, boolean isSK, F_Type f_type) {
		this.name = name;
		this.isPK = isPK;
		this.isSK = isSK;
		this.f_type = f_type;
	}
	
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public String toStringWithType() {
		return "(" + this.name + ":" + this.f_type + ")";
	}
}
